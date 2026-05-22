<template>
  <n-card title="企业管理">
    <n-space class="mb-12px">
      <n-input v-model:value="keyword" placeholder="企业名称" clearable style="width: 200px" />
      <n-button type="primary" @click="load">搜索</n-button>
      <n-button @click="router.push('/tenants/create')">创建企业</n-button>
    </n-space>
    <n-data-table :columns="columns" :data="rows" :loading="loading" :pagination="pagination" />
  </n-card>
</template>

<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue'
import { NButton, useMessage, type DataTableColumns } from 'naive-ui'
import { useRouter } from 'vue-router'
import { api, type DefTenant } from '../api'

const router = useRouter()
const message = useMessage()
const keyword = ref('')
const loading = ref(false)
const rows = ref<DefTenant[]>([])

const pagination = reactive({
  page: 1,
  pageSize: 20,
  itemCount: 0,
  onChange: (p: number) => {
    pagination.page = p
    load()
  }
})

const columns: DataTableColumns<DefTenant> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '企业名称', key: 'name' },
  { title: '企业号', key: 'inviteCode', render: (r) => r.inviteCode || '—' },
  { title: '状态', key: 'state', render: (r) => (r.state ? '启用' : '停用') },
  { title: '创建时间', key: 'createTime' },
  {
    title: '操作',
    key: 'op',
    render: (row) =>
      h(
        NButton,
        {
          text: true,
          type: 'primary',
          onClick: async () => {
            const code = await api<string>(`/base/platform/tenant/${row.id}/invite-code`, { method: 'PUT' })
            message.success(`新邀请码：${code}`)
            load()
          }
        },
        { default: () => '重置邀请码' }
      )
  }
]

async function load() {
  loading.value = true
  try {
    const body = {
      pageNo: pagination.page,
      pageSize: pagination.pageSize,
      model: { name: keyword.value || undefined }
    }
    const page = await api<{ records: DefTenant[]; total: number }>('/base/platform/tenant/page', {
      method: 'POST',
      body: JSON.stringify(body)
    })
    rows.value = page.records || []
    pagination.itemCount = page.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>
