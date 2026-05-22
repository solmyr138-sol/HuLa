<template>
  <div class="login-wrap">
    <n-card title="企业传书管理后台" style="width: 400px">
      <n-form>
        <n-form-item label="账号">
          <n-input v-model:value="account" placeholder="admin" />
        </n-form-item>
        <n-form-item label="密码">
          <n-input v-model:value="password" type="password" />
        </n-form-item>
        <n-button type="primary" block @click="enter">进入后台</n-button>
      </n-form>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const account = ref('admin')
const password = ref('')

function enter() {
  localStorage.setItem('token', 'dev')
  router.push('/enterprise')
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
</style>
