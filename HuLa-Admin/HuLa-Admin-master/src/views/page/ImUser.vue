<template>
  <div class="p-4 space-y-4">
    <n-alert type="info" :bordered="false" title="说明">
      本页展示当前企业下通过客户端注册的 IM 用户（含仅注册、尚未登录的用户）。「活跃用户」菜单仅统计时间范围内有登录记录的用户。
    </n-alert>
    <n-card title="IM 注册用户" :bordered="false" size="small">
      <div class="mb-4">
        <n-form inline :model="query">
          <n-form-item label="关键词">
            <n-input
              v-model:value="query.keyword"
              class="w-52"
              clearable
              placeholder="账号 / 昵称 / 手机号"
            />
          </n-form-item>
          <n-form-item label="状态">
            <n-select
              v-model:value="query.state"
              :options="stateOptions"
              class="w-40"
              clearable
              placeholder="全部"
            />
          </n-form-item>
          <n-form-item>
            <n-space>
              <n-button type="primary" @click="handleSearch">查询</n-button>
              <n-button @click="handleReset">重置</n-button>
            </n-space>
          </n-form-item>
        </n-form>
      </div>
      <n-data-table
        :columns="columns"
        :data="rows"
        :loading="loading"
        :bordered="false"
        :pagination="pagination"
        remote
      />
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, h, reactive } from 'vue'
import { NTag } from 'naive-ui'
import { getAdminImUserPage, type AdminImUserItem } from '@/api/imUser'

const query = reactive<{ pageNo: number; pageSize: number; keyword?: string; state?: number }>({
  pageNo: 1,
  pageSize: 10
})
const rows = ref<AdminImUserItem[]>([])
const loading = ref(false)
const totalCount = ref(0)

const stateOptions = [
  { label: '正常', value: 0 },
  { label: '禁用', value: 1 }
]

const pagination = reactive({
  page: 1,
  pageSize: 10,
  pageSizes: [10, 20, 50],
  showSizePicker: true,
  showQuickJumper: true,
  itemCount: 0,
  onUpdatePage: (p: number) => {
    pagination.page = p
    query.pageNo = p
    load()
  },
  onUpdatePageSize: (s: number) => {
    pagination.pageSize = s
    query.pageSize = s
    query.pageNo = 1
    pagination.page = 1
    load()
  }
})

const columns = [
  { title: 'UID', key: 'uid', width: 100 },
  { title: '账号', key: 'account' },
  { title: '昵称', key: 'nickname', render: (row: AdminImUserItem) => row.nickname || '-' },
  { title: '手机号', key: 'mobile', render: (row: AdminImUserItem) => row.mobile || '-' },
  {
    title: '状态',
    key: 'state',
    width: 90,
    render: (row: AdminImUserItem) =>
      h(
        NTag,
        { size: 'small', type: row.state === 0 ? 'success' : 'error', bordered: false },
        { default: () => (row.state === 0 ? '正常' : '禁用') }
      )
  },
  {
    title: '注册时间',
    key: 'registerTime',
    render: (row: AdminImUserItem) => formatTime(row.registerTime)
  }
]

function formatTime(value?: string) {
  if (!value) return '-'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return value
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

async function load() {
  loading.value = true
  try {
    const data = await getAdminImUserPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
      state: query.state
    })
    rows.value = data?.list || []
    totalCount.value = Number(data?.totalRecords || 0)
    pagination.itemCount = totalCount.value
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.pageNo = 1
  pagination.page = 1
  load()
}

const handleReset = () => {
  query.keyword = ''
  query.state = undefined
  query.pageNo = 1
  pagination.page = 1
  load()
}

onMounted(() => load())
</script>
