<template>
  <n-card title="功能配置">
    <n-space vertical :size="20">
      <n-checkbox v-model:checked="policy.forbidMemberAddFriend">禁止成员互加好友</n-checkbox>
      <n-checkbox v-model:checked="policy.forbidCreateGroup">禁止成员创建群</n-checkbox>
      <n-checkbox v-model:checked="policy.forbidBroadcast">禁止成员群发消息</n-checkbox>
      <n-button type="primary" :loading="saving" @click="savePolicy">保存更改</n-button>

      <n-divider />
      <div class="whitelist-title">允许名单 <span class="sub">（名单成员不受以上功能限制）</span></div>
      <n-button text type="primary" @click="showAdd = true">+ 添加成员</n-button>
      <n-data-table :columns="wlColumns" :data="whitelist" :bordered="false" />

      <n-modal v-model:show="showAdd" preset="dialog" title="添加白名单成员">
        <n-input-number v-model:value="addUid" placeholder="IM用户 UID" class="w-full" />
        <template #action>
          <n-button @click="showAdd = false">取消</n-button>
          <n-button type="primary" @click="addWhitelist">确定</n-button>
        </template>
      </n-modal>
    </n-space>
  </n-card>
</template>

<script setup lang="ts">
import { h, onMounted, ref } from 'vue'
import { NButton, useMessage, type DataTableColumns } from 'naive-ui'
import { api, type TenantPolicy, type WhitelistUser } from '../../api'

const message = useMessage()
const policy = ref<TenantPolicy>({})
const whitelist = ref<WhitelistUser[]>([])
const saving = ref(false)
const showAdd = ref(false)
const addUid = ref<number | null>(null)

const wlColumns: DataTableColumns<WhitelistUser> = [
  { title: '账号', key: 'account' },
  { title: '手机号码', key: 'mobile' },
  { title: '昵称', key: 'nickname' },
  {
    title: '操作',
    key: 'op',
    render: (row) =>
      h(
        NButton,
        { text: true, type: 'error', onClick: () => removeWhitelist(row.id) },
        { default: () => '移除' }
      )
  }
]

async function load() {
  policy.value = await api<TenantPolicy>('/im/admin/tenant/policy')
  whitelist.value = await api<WhitelistUser[]>('/im/admin/tenant/policy/whitelist')
}

async function savePolicy() {
  saving.value = true
  try {
    await api('/im/admin/tenant/policy', { method: 'PUT', body: JSON.stringify(policy.value) })
    message.success('已保存')
  } finally {
    saving.value = false
  }
}

async function addWhitelist() {
  if (!addUid.value) return
  await api('/im/admin/tenant/policy/whitelist', {
    method: 'POST',
    body: JSON.stringify({ imUid: addUid.value })
  })
  showAdd.value = false
  addUid.value = null
  await load()
  message.success('已添加')
}

async function removeWhitelist(id: number) {
  await api(`/im/admin/tenant/policy/whitelist/${id}`, { method: 'DELETE' })
  await load()
  message.success('已移除')
}

onMounted(load)
</script>

<style scoped>
.whitelist-title {
  font-weight: 600;
}
.sub {
  font-weight: normal;
  color: #999;
  font-size: 12px;
}
</style>
