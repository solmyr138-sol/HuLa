<template>

  <div class="login-wrap">

    <n-card title="企业传书管理后台" style="width: 400px">

      <n-form>

        <n-form-item label="手机号">

          <n-input v-model:value="account" placeholder="如 13275346112（IM 注册手机号）" @keyup.enter="enter" />

        </n-form-item>

        <n-form-item label="密码">

          <n-input v-model:value="password" type="password" show-password-on="click" @keyup.enter="enter" />

        </n-form-item>

        <n-button type="primary" block :loading="loading" @click="enter">登录</n-button>

      </n-form>

      <p class="hint">与 HuLa 客户端相同：填 IM 注册手机号 + 密码。不要填 admin / Nacos 账号。</p>

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

    if (result.uid) localStorage.setItem('adminUid', String(result.uid))

    localStorage.setItem('tenantId', '1')

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

