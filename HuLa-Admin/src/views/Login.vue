<template>

  <div class="login-wrap">

    <n-card title="企业传书管理后台" style="width: 400px">

      <n-form>

        <n-form-item label="账号">

          <n-input v-model:value="account" placeholder="IM 手机号，或 admin_邀请码（企业管理员）" @keyup.enter="enter" />

        </n-form-item>

        <n-form-item label="密码">

          <n-input v-model:value="password" type="password" show-password-on="click" @keyup.enter="enter" />

        </n-form-item>

        <n-button type="primary" block :loading="loading" @click="enter">登录</n-button>

      </n-form>

      <p class="hint">仅需账号+密码。若账号是 admin_邀请码，系统会自动识别企业，无需手动输入企业 ID。</p>

    </n-card>

  </div>

</template>



<script setup lang="ts">

import { ref } from 'vue'

import { useRouter } from 'vue-router'

import { useMessage } from 'naive-ui'

import { loginAdmin } from '../api'



const router = useRouter()

const message = useMessage()

const account = ref('')

const password = ref('')

const loading = ref(false)



async function enter() {

  if (!account.value.trim() || !password.value) {

    message.warning('请输入账号和密码')

    return

  }

  loading.value = true

  try {

    const result = await loginAdmin(account.value.trim(), password.value)

    localStorage.setItem('token', result.token)

    localStorage.setItem('tenantId', String(result.tenantId ?? localStorage.getItem('tenantId') ?? '1'))

    if (result.uid) localStorage.setItem('adminUid', String(result.uid))

    router.push('/enterprise')

  } catch (e) {

    message.error(e instanceof Error ? e.message : '登录失败')

  } finally {

    loading.value = false

  }

}

</script>



<style scoped>

.login-wrap {

  min-height: 100vh;

  display: flex;

  align-items: center;

  justify-content: center;

  background: #f0f2f5;

}

.hint {

  margin: 12px 0 0;

  font-size: 12px;

  color: #888;

  line-height: 1.5;

}

</style>

