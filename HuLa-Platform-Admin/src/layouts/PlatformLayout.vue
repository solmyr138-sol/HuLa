<template>
  <n-layout has-sider style="height: 100vh">
    <n-layout-sider bordered :width="220" style="background: #1a2332">
      <div class="brand">
        <div class="title">企业传书平台总后台</div>
        <div class="sub">devOperation · 跨租户</div>
      </div>
      <n-menu
        inverted
        :value="route.path"
        :options="menu"
        @update:value="(k) => router.push(k)" />
    </n-layout-sider>
    <n-layout>
      <n-layout-header bordered class="header">平台运营</n-layout-header>
      <n-layout-content class="main"><router-view /></n-layout-content>
    </n-layout>
  </n-layout>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const menu = [
  { label: '平台概览', key: '/dashboard' },
  { label: '企业管理', key: '/tenants' },
  { label: '创建企业', key: '/tenants/create' }
]
</script>

<style scoped>
.brand {
  padding: 16px;
  color: #fff;
}
.title {
  font-weight: 600;
}
.sub {
  font-size: 12px;
  opacity: 0.7;
  margin-top: 6px;
}
.header {
  padding: 0 16px;
  height: 48px;
  display: flex;
  align-items: center;
}
.main {
  padding: 16px;
  background: #f0f2f5;
}
</style>
