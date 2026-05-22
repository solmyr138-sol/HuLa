<template>
  <div class="wrap">
    <n-card title="平台总后台登录" style="width: 400px">
      <p class="mb-12px text-#666">使用 devOperation 管理员账号登录（与租户后台分离）</p>
      <n-button type="primary" block @click="enter">进入（开发模式）</n-button>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
function enter() {
  localStorage.setItem('token', 'dev-platform')
  router.push('/dashboard')
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
</style>
