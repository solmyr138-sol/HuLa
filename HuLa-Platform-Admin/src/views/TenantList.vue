<template>
  <n-card title="企业管理">
    <n-space class="mb-12px">
      <n-input v-model:value="keyword" placeholder="企业名称" clearable style="width: 200px" />
      <n-button type="primary" @click="load">搜索</n-button>
      <n-button @click="router.push('/tenants/create')">创建企业</n-button>
    </n-space>
    <n-data-table :columns="columns" :data="rows" :loading="loading" :pagination="pagination" />

    <n-modal v-model:show="editVisible" preset="card" title="编辑企业" style="width: 520px">
      <n-form v-if="editForm" label-width="120">
        <n-form-item label="企业名称" required>
          <n-input v-model:value="editForm.name" />
        </n-form-item>
        <n-form-item label="联系人">
          <n-input v-model:value="editForm.contactPerson" />
        </n-form-item>
        <n-form-item label="联系电话">
          <n-input v-model:value="editForm.contactPhone" />
        </n-form-item>
        <n-form-item label="注册人数上限">
          <n-input-number v-model:value="editForm.accountLimit" :min="1" :max="999999" style="width: 200px" />
        </n-form-item>
        <n-form-item label="状态">
          <n-switch v-model:value="editForm.state" />
          <span class="ml-8px">{{ editForm.state ? '启用' : '停用' }}</span>
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="editVisible = false">取消</n-button>
          <n-button type="primary" :loading="editSaving" @click="saveEdit">保存</n-button>
        </n-space>
      </template>
    </n-modal>
  </n-card>
</template>

<script setup lang="ts">
import { h, onMounted, reactive, ref } from 'vue'
import { NButton, NSpace, useDialog, useMessage, type DataTableColumns } from 'naive-ui'
import { useRouter } from 'vue-router'
import { api, type DefTenant } from '../api'

const router = useRouter()
const message = useMessage()
const dialog = useDialog()
const keyword = ref('')
const loading = ref(false)
const rows = ref<DefTenant[]>([])
const editVisible = ref(false)
const editSaving = ref(false)
const editForm = ref<{
  id: number
  name: string
  contactPerson: string
  contactPhone: string
  accountLimit: number
  state: boolean
} | null>(null)

const pagination = reactive({
  page: 1,
  pageSize: 20,
  itemCount: 0,
  onChange: (p: number) => {
    pagination.page = p
    load()
  }
})

function formatAccountLimit(limit?: number) {
  if (!limit || limit <= 0) return '不限'
  return String(limit)
}

const columns: DataTableColumns<DefTenant> = [
  { title: 'ID', key: 'id', width: 80 },
  { title: '企业名称', key: 'name' },
  { title: '企业号', key: 'inviteCode', render: (r) => r.inviteCode || '—' },
  {
    title: '注册人数',
    key: 'account',
    render: (r) => `${r.accountCount ?? 0} / ${formatAccountLimit(r.accountLimit)}`
  },
  { title: '状态', key: 'state', render: (r) => (r.state ? '启用' : '停用') },
  { title: '创建时间', key: 'createTime' },
  {
    title: '操作',
    key: 'op',
    width: 260,
    render: (row) =>
      h(NSpace, null, {
        default: () => [
          h(
            NButton,
            {
              text: true,
              type: 'primary',
              onClick: () => openEdit(row)
            },
            { default: () => '编辑' }
          ),
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
          ),
          h(
            NButton,
            {
              text: true,
              type: 'error',
              onClick: () => removeTenant(row)
            },
            { default: () => '删除企业' }
          )
        ]
      })
  }
]

function removeTenant(row: DefTenant) {
  dialog.warning({
    title: '确认删除企业',
    content: `企业「${row.name}」将被删除，删除后不可恢复。是否继续？`,
    positiveText: '确认删除',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await api<boolean>(`/base/platform/tenant/${row.id}`, { method: 'DELETE' })
        message.success('企业已删除')
        if (rows.value.length === 1 && pagination.page > 1) {
          pagination.page -= 1
        }
        load()
        return true
      } catch (e: unknown) {
        const err = e instanceof Error ? e : new Error('删除失败')
        message.error(err.message || '删除企业失败')
        return false
      }
    }
  })
}

function openEdit(row: DefTenant) {
  editForm.value = {
    id: row.id,
    name: row.name,
    contactPerson: row.contactName || '',
    contactPhone: row.contactMobile || '',
    accountLimit: row.accountLimit && row.accountLimit > 0 ? row.accountLimit : 500,
    state: row.state !== false
  }
  editVisible.value = true
}

async function saveEdit() {
  if (!editForm.value?.name.trim()) {
    message.warning('请填写企业名称')
    return
  }
  editSaving.value = true
  try {
    await api<DefTenant>('/base/platform/tenant', {
      method: 'PUT',
      body: JSON.stringify({
        id: editForm.value.id,
        name: editForm.value.name.trim(),
        contactPerson: editForm.value.contactPerson || undefined,
        contactPhone: editForm.value.contactPhone || undefined,
        accountLimit: editForm.value.accountLimit,
        state: editForm.value.state
      })
    })
    message.success('已保存')
    editVisible.value = false
    load()
  } finally {
    editSaving.value = false
  }
}

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

<style scoped>
.mb-12px {
  margin-bottom: 12px;
}
.ml-8px {
  margin-left: 8px;
}
</style>
