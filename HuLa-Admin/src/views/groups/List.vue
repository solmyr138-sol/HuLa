<template>
  <n-card title="群聊列表">
    <n-space class="mb-12px">
      <n-input v-model:value="keyword" placeholder="群名称" clearable style="width: 200px" @keyup.enter="load" />
      <n-button type="primary" @click="load">搜索</n-button>
      <n-button @click="reset">重置</n-button>
    </n-space>
    <n-data-table
      :columns="columns"
      :data="rows"
      :loading="loading"
      :pagination="pagination"
      :row-key="(r: GroupChatRow) => r.groupId" />
  </n-card>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useMessage, type DataTableColumns } from 'naive-ui'
import { listGroupChats, type GroupChatRow } from '../../api'

const message = useMessage()
const keyword = ref('')
const loading = ref(false)
const rows = ref<GroupChatRow[]>([])

const pagination = reactive({
  page: 1,
  pageSize: 20,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
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
  if (!Number.isNaN(n)) {
    const ms = n > 1e12 ? n : n * 1000
    return new Date(ms).toLocaleString('zh-CN')
  }
  const d = new Date(String(v))
  return Number.isNaN(d.getTime()) ? String(v) : d.toLocaleString('zh-CN')
}

const columns: DataTableColumns<GroupChatRow> = [
  { title: '群名称', key: 'groupName', render: (r) => r.groupName || '—' },
  { title: '群主', key: 'ownerName', render: (r) => r.ownerName || '—' },
  { title: '成员数', key: 'memberNum', render: (r) => (r.memberNum != null ? String(r.memberNum) : '—') },
  { title: '创建时间', key: 'createTime', render: (r) => formatTime(r.createTime) }
]

async function load() {
  loading.value = true
  try {
    const res = await listGroupChats({
      pageNo: pagination.page,
      pageSize: pagination.pageSize,
      groupNameKeyword: keyword.value
    })
    rows.value = res.list
    pagination.itemCount = res.totalRecords
  } catch (e) {
    message.error(e instanceof Error ? e.message : '加载群聊列表失败')
    rows.value = []
    pagination.itemCount = 0
  } finally {
    loading.value = false
  }
}

function reset() {
  keyword.value = ''
  pagination.page = 1
  load()
}

onMounted(load)
</script>
