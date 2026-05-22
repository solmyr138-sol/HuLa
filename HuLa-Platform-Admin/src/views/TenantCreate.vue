<template>
  <n-card title="创建企业">
    <n-form label-width="120">
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
      <n-button type="primary" :loading="saving" @click="submit">创建并生成企业号</n-button>
    </n-form>
    <n-alert v-if="createdCode" type="success" class="mt-16px" title="创建成功">
      企业邀请码：<strong>{{ createdCode }}</strong>（请交付租户用于客户端注册第一步）
    </n-alert>
  </n-card>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useMessage } from 'naive-ui'
import { api, type DefTenant } from '../api'

const message = useMessage()
const saving = ref(false)
const createdCode = ref('')
const form = ref({
  name: '',
  creditCode: '',
  contactPerson: '',
  contactPhone: ''
})

async function submit() {
  if (!form.value.name.trim()) {
    message.warning('请填写企业名称')
    return
  }
  saving.value = true
  try {
    const tenant = await api<DefTenant>('/base/platform/tenant', {
      method: 'POST',
      body: JSON.stringify(form.value)
    })
    createdCode.value = tenant.inviteCode || ''
    message.success('企业已创建')
  } finally {
    saving.value = false
  }
}
</script>
