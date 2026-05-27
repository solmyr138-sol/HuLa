<template>
  <n-card title="创建企业">
    <n-form label-width="140">
      <n-form-item label="企业名称" required>
        <n-input v-model:value="form.name" placeholder="海口智迈网络科技有限公司" />
      </n-form-item>
      <n-form-item label="统一社会信用代码">
        <n-input v-model:value="form.creditCode" />
      </n-form-item>
      <n-form-item label="联系人">
        <n-input v-model:value="form.contactPerson" />
      </n-form-item>
      <n-form-item label="联系电话">
        <n-input v-model:value="form.contactPhone" />
      </n-form-item>
      <n-form-item label="注册人数上限">
        <n-input-number v-model:value="form.accountLimit" :min="1" :max="999999" style="width: 200px" />
        <span class="field-hint">该企业最多可注册的 IM 用户数，默认 500</span>
      </n-form-item>
      <n-button type="primary" :loading="saving" @click="submit">创建并生成企业号</n-button>
    </n-form>
    <n-alert v-if="created" type="success" class="mt-16px" title="创建成功">
      <p>企业邀请码：<strong>{{ created.inviteCode }}</strong>（客户端注册第一步）</p>
      <p>企业 ID：<strong>{{ created.tenantId }}</strong>（企业后台登录请求头 tenant-id）</p>
      <p>管理后台账号：<strong>{{ created.adminUsername }}</strong></p>
      <p>初始密码：<strong>{{ created.adminPassword }}</strong>（请妥善保存，仅显示一次）</p>
      <p v-if="created.adminLoginHint" class="hint">{{ created.adminLoginHint }}</p>
      <ul v-if="created.warnings?.length" class="warn-list">
        <li v-for="(w, i) in created.warnings" :key="i">{{ w }}</li>
      </ul>
    </n-alert>
  </n-card>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useMessage } from 'naive-ui'
import { api, type DefTenant, type PlatformTenantCreateResp } from '../api'

const message = useMessage()
const saving = ref(false)
const created = ref<{
  inviteCode: string
  tenantId: number
  adminUsername: string
  adminPassword: string
  adminLoginHint?: string
  warnings?: string[]
} | null>(null)

const form = ref({
  name: '',
  creditCode: '',
  contactPerson: '',
  contactPhone: '',
  accountLimit: 500 as number | null
})

async function submit() {
  if (!form.value.name.trim()) {
    message.warning('请填写企业名称')
    return
  }
  saving.value = true
  created.value = null
  try {
    const result = await api<PlatformTenantCreateResp | DefTenant>('/base/platform/tenant', {
      method: 'POST',
      body: JSON.stringify({
        name: form.value.name.trim(),
        creditCode: form.value.creditCode || undefined,
        contactPerson: form.value.contactPerson || undefined,
        contactPhone: form.value.contactPhone || undefined,
        accountLimit: form.value.accountLimit ?? 500
      })
    })
    const hasNewResp = typeof (result as PlatformTenantCreateResp).tenant !== 'undefined'
    const tenant = hasNewResp ? (result as PlatformTenantCreateResp).tenant : (result as DefTenant)
    const resp = hasNewResp ? (result as PlatformTenantCreateResp) : null
    const adminUsername = resp?.adminUsername || ''
    const adminPassword = resp?.adminPassword || ''
    created.value = {
      inviteCode: tenant?.inviteCode || '',
      tenantId: tenant?.id || 0,
      adminUsername,
      adminPassword,
      adminLoginHint: resp?.adminLoginHint,
      warnings: resp?.warnings
    }
    if (!adminPassword) {
      message.warning('企业已创建，但后端未返回管理员初始密码，请检查 luohuo-base-server 是否已更新并重启')
    } else if (resp?.warnings?.length) {
      message.warning('企业已创建，部分 IM 能力需启动 luohuo-im-server 后补全')
    } else {
      message.success('企业已创建')
    }
  } catch (e: unknown) {
    const err = e instanceof Error ? e : new Error('创建失败')
    message.error(err.message || '创建企业失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.mt-16px {
  margin-top: 16px;
}
.field-hint {
  margin-left: 12px;
  color: #888;
  font-size: 12px;
}
.hint {
  margin-top: 8px;
  font-size: 12px;
  color: #666;
}
.warn-list {
  margin: 8px 0 0;
  padding-left: 18px;
  color: #b8860b;
  font-size: 12px;
}
</style>
