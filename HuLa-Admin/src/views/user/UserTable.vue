<template>
  <n-card :title="title">
    <n-space class="mb-12px" wrap>
      <n-input v-model:value="keyword" placeholder="账号 / 昵称 / 邮箱" clearable style="width: 220px" />
      <n-button type="primary" @click="load">搜索</n-button>
      <n-button @click="reset">重置</n-button>
      <n-button type="error" ghost :disabled="!selected.length" @click="ban">封禁</n-button>
      <n-button ghost :disabled="!selected.length" @click="unban">解封</n-button>
    </n-space>
    <n-data-table
      :columns="columns"
      :data="rows"
      :loading="loading"
      :pagination="pagination"
      :row-key="(r: ImUserRow) => r.uid"
      @update:checked-row-keys="onSelect" />
  </n-card>
</template>

<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue'
import { NButton, useMessage, type DataTableColumns } from 'naive-ui'
import { api, type ImUserRow, type PageResp } from '../../api'

const props = defineProps<{ title: string; deletedOnly?: boolean }>()
const message = useMessage()

const keyword = ref('')
const loading = ref(false)
const rows = ref<ImUserRow[]>([])
const selected = ref<number[]>([])

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

const columns: DataTableColumns<ImUserRow> = [
  { type: 'selection' },
  { title: '账号', key: 'account' },
  { title: '手机号码', key: 'mobile', render: (r) => r.mobile || '—' },
  { title: '昵称', key: 'nickname' },
  {
    title: '状态',
    key: 'state',
    render: (r) => (r.state === 1 ? '封禁' : props.deletedOnly ? '已注销' : '正常')
  },
  { title: '注册时间', key: 'registerTime', render: (r) => r.registerTime || '—' },
  ...(props.deletedOnly
    ? [{ title: '注销时间', key: 'deletedTime', render: (r: ImUserRow) => r.deletedTime || '—' }]
    : [])
]

async function load() {
  loading.value = true
  try {
    const q = new URLSearchParams({
      pageNo: String(pagination.page),
      pageSize: String(pagination.pageSize),
      keyword: keyword.value,
      deletedOnly: String(!!props.deletedOnly)
    })
    const res = await api<PageResp<ImUserRow>>(`/im/admin/user/page?${q}`)
    rows.value = res.list
    pagination.itemCount = res.totalRecords
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

onMounted(load)
</script>
