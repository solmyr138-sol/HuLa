<template>
  <n-card :title="title">
    <n-space class="mb-12px" wrap>
      <n-input v-model:value="keyword" placeholder="账号 / 昵称 / 手机号" clearable style="width: 220px" />
      <n-button type="primary" @click="load">搜索</n-button>
      <n-button @click="reset">重置</n-button>
      <template v-if="!deletedOnly">
        <n-button type="error" ghost :disabled="!selected.length" @click="ban">封禁</n-button>
        <n-button ghost :disabled="!selected.length" @click="unban">解封</n-button>
      </template>
    </n-space>
    <n-data-table
      :columns="columns"
      :data="rows"
      :loading="loading"
      :pagination="pagination"
      :row-key="(r: ImUserRow) => r.uid"
      @update:checked-row-keys="onSelect" />
    <UserEditModal v-model:show="editVisible" :user="editing" @saved="load" />
  </n-card>
</template>

<script setup lang="ts">
import { h, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { NButton, useMessage, type DataTableColumns } from 'naive-ui'
import UserEditModal from '../../components/UserEditModal.vue'
import { api, listImUsers, type ImUserRow } from '../../api'

const props = defineProps<{ title: string; deletedOnly?: boolean }>()
const route = useRoute()
const message = useMessage()

const keyword = ref('')
const loading = ref(false)
const rows = ref<ImUserRow[]>([])
const selected = ref<number[]>([])
const editVisible = ref(false)
const editing = ref<ImUserRow | null>(null)

const pagination = reactive({
  page: 1,
  pageSize: 50,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [20, 50, 100],
  onChange: (p: number) => {
    pagination.page = p
    load()
  },
  onUpdatePageSize: (s: number) => {
    pagination.pageSize = s
    pagination.page = 1
    load()
  }
})

function formatTime(v?: string | number) {
  if (v == null || v === '') return '—'
  const n = Number(v)
  if (Number.isNaN(n)) return String(v)
  const ms = n > 1e12 ? n : n * 1000
  return new Date(ms).toLocaleString('zh-CN')
}

function stateLabel(row: ImUserRow) {
  if (props.deletedOnly || row.state === -1) return '已注销'
  if (row.state === 1) return '封禁'
  return '正常'
}

const columns: DataTableColumns<ImUserRow> = [
  { type: 'selection' },
  { title: '账号', key: 'account' },
  { title: '手机号', key: 'mobile', render: (r) => r.mobile || '—' },
  { title: '昵称', key: 'nickname' },
  { title: '登录IP', key: 'lastLoginIp', render: (r) => r.lastLoginIp || '—' },
  {
    title: '状态',
    key: 'state',
    render: (r) => stateLabel(r)
  },
  {
    title: props.deletedOnly ? '注销时间' : '注册时间',
    key: 'time',
    render: (r) => formatTime(props.deletedOnly ? r.deletedTime : r.registerTime)
  },
  ...(!props.deletedOnly
    ? [
        {
          title: '操作',
          key: 'actions',
          render: (r: ImUserRow) =>
            h(
              NButton,
              { text: true, type: 'primary', onClick: () => openEdit(r) },
              { default: () => '编辑' }
            )
        }
      ]
    : [])
]

function openEdit(row: ImUserRow) {
  editing.value = row
  editVisible.value = true
}

async function load() {
  loading.value = true
  try {
    const res = await listImUsers({
      pageNo: pagination.page,
      pageSize: pagination.pageSize,
      keyword: keyword.value,
      deletedOnly: props.deletedOnly
    })
    rows.value = res.list ?? []
    pagination.itemCount = Number(res.totalRecords) || 0
    if (props.deletedOnly && pagination.itemCount === 0 && !keyword.value) {
      message.info('暂无注销记录；注销用户来自客户端注销审计，与注册用户列表互斥')
    }
  } catch (e) {
    rows.value = []
    pagination.itemCount = 0
    message.error(e instanceof Error ? e.message : '加载用户列表失败')
  } finally {
    loading.value = false
  }
}

function reset() {
  keyword.value = ''
  pagination.page = 1
  load()
}

function onSelect(keys: Array<string | number>) {
  selected.value = keys as number[]
}

async function ban() {
  for (const uid of selected.value) {
    await api('/im/admin/user/ban', { method: 'PUT', body: JSON.stringify({ uid }) })
  }
  message.success('已封禁')
  load()
}

async function unban() {
  for (const uid of selected.value) {
    await api('/im/admin/user/unban', { method: 'PUT', body: JSON.stringify({ uid }) })
  }
  message.success('已解封')
  load()
}

watch(
  () => [props.deletedOnly, route.path] as const,
  () => {
    selected.value = []
    pagination.page = 1
    load()
  }
)

onMounted(load)
</script>
