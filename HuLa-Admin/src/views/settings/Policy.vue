<template>
  <n-card title="功能配置">
    <n-space vertical :size="20">
      <div class="logo-block">
        <div class="logo-title">企业 LOGO</div>
        <div class="sub mb-8px">用于 APP 社区页顶部展示；未上传时显示企业名称</div>
        <n-space align="center" :size="16">
          <div v-if="tenantLogo" class="logo-preview">
            <img :src="tenantLogo" alt="企业LOGO" />
          </div>
          <div v-else class="logo-preview logo-placeholder">{{ tenantName || '企业名称' }}</div>
          <n-space vertical :size="8">
            <n-upload :show-file-list="false" accept="image/*" :custom-request="handleLogoUpload">
              <n-button type="primary" ghost :loading="logoUploading">上传 LOGO</n-button>
            </n-upload>
            <n-button v-if="tenantLogo" text type="error" @click="clearLogo">清除 LOGO</n-button>
          </n-space>
        </n-space>
      </div>

      <n-divider />

      <n-checkbox v-model:checked="policy.forbidMemberAddFriend">禁止成员互加好友</n-checkbox>
      <n-checkbox v-model:checked="policy.forbidCreateGroup">禁止成员创建群</n-checkbox>
      <n-checkbox v-model:checked="policy.forbidBroadcast">禁止成员群发消息</n-checkbox>
      <n-button type="primary" :loading="saving" @click="savePolicy">保存更改</n-button>

      <n-divider />
      <div class="whitelist-title">允许名单 <span class="sub">（名单成员不受以上功能限制）</span></div>
      <n-button text type="primary" @click="showAdd = true">+ 添加成员</n-button>
      <n-data-table :columns="wlColumns" :data="whitelist" :bordered="false" />

      <n-modal v-model:show="showAdd" preset="dialog" title="添加白名单成员">
        <n-input
          v-model:value="addKey"
          placeholder="注册用户列表中的账号或手机号"
          clearable
          class="w-full" />
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
import {
  api,
  getTenantInfo,
  updateTenantLogo,
  uploadTenantLogoFile,
  type TenantPolicy,
  type WhitelistUser
} from '../../api'
import type { UploadCustomRequestOptions } from 'naive-ui'

const message = useMessage()
const tenantLogo = ref('')
const tenantName = ref('')
const logoUploading = ref(false)
const policy = ref<TenantPolicy>({})
const whitelist = ref<WhitelistUser[]>([])
const saving = ref(false)
const showAdd = ref(false)
const addKey = ref('')

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

async function loadTenantBranding() {
  const info = await getTenantInfo()
  tenantLogo.value = info.logo || ''
  tenantName.value = info.tenantName || ''
}

async function handleLogoUpload({ file, onFinish, onError }: UploadCustomRequestOptions) {
  const raw = file.file
  if (!raw) {
    onError()
    return
  }
  logoUploading.value = true
  try {
    const url = await uploadTenantLogoFile(raw)
    await updateTenantLogo(url)
    tenantLogo.value = url
    message.success('企业 LOGO 已更新')
    onFinish()
  } catch (e) {
    message.error(e instanceof Error ? e.message : '上传失败')
    onError()
  } finally {
    logoUploading.value = false
  }
}

async function clearLogo() {
  await updateTenantLogo('')
  tenantLogo.value = ''
  message.success('已清除企业 LOGO')
}

async function load() {
  await loadTenantBranding()
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
  const key = addKey.value.trim()
  if (!key) {
    message.warning('请输入账号或手机号')
    return
  }
  try {
    await api('/im/admin/tenant/policy/whitelist', {
      method: 'POST',
      body: JSON.stringify({ accountOrMobile: key })
    })
    showAdd.value = false
    addKey.value = ''
    await load()
    message.success('已添加')
  } catch (e) {
    message.error(e instanceof Error ? e.message : '添加失败')
  }
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
.logo-title {
  font-weight: 600;
  margin-bottom: 4px;
}
.logo-preview {
  width: 120px;
  height: 64px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #eee;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}
.logo-preview img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}
.logo-placeholder {
  font-size: 12px;
  color: #666;
  padding: 8px;
  text-align: center;
  line-height: 1.3;
}
</style>

