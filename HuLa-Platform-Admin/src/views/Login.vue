<template>
  <div class="wrap">
    <n-card title="企业传书平台总后台" style="width: 400px">
      <n-form ref="formRef" :model="form" :rules="rules">
        <n-form-item label="用户名" path="username">
          <n-input v-model:value="form.username" placeholder="平台管理员账号（systemType=1）" @keyup.enter="submit" />
        </n-form-item>
        <n-form-item label="密码" path="password">
          <n-input v-model:value="form.password" type="password" show-password-on="click" placeholder="请输入密码" @keyup.enter="submit" />
        </n-form-item>
        <n-button type="primary" block :loading="loading" @click="submit">登 录</n-button>
      </n-form>
      <p class="hint">总后台使用平台管理员账号（非企业后台 IM 手机号）。新建企业的管理员账号为 admin_企业邀请码。</p>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage, type FormInst } from 'naive-ui'
import { api } from '../api'

const router = useRouter()
const message = useMessage()
const formRef = ref<FormInst | null>(null)
const loading = ref(false)

const form = ref({ username: '', password: '' })
const rules = {
  username: { required: true, message: '请输入用户名', trigger: 'blur' },
  password: { required: true, message: '请输入密码', trigger: 'blur' }
}

async function submit() {
  await formRef.value?.validate()
  loading.value = true
  try {
    const res = await api<{ token: string; nickName: string }>('/base/anyTenant/platformLogin', {
      method: 'POST',
      body: JSON.stringify(form.value)
    })
    localStorage.setItem('token', res.token)
    localStorage.setItem('nickName', res.nickName || '')
    message.success('登录成功')
    router.push('/dashboard')
  } catch (e: any) {
    message.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.wrap {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #eef1f5;
}
.hint {
  margin-top: 12px;
  font-size: 12px;
  color: #888;
  line-height: 1.5;
}
</style>
