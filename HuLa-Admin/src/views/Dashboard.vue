<template>
  <n-card title="仪表盘">
    <n-grid :cols="3" :x-gap="12">
      <n-gi><n-statistic label="今日活跃" :value="stats?.todayActiveUser ?? '-'" /></n-gi>
      <n-gi><n-statistic label="群聊总数" :value="stats?.totalGroup ?? '-'" /></n-gi>
      <n-gi><n-statistic label="黑名单" :value="stats?.blackStats?.blackCount ?? '-'" /></n-gi>
    </n-grid>
  </n-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { api } from '../api'

const stats = ref<any>(null)
onMounted(async () => {
  stats.value = await api('/im/admin/stats/home')
})
</script>
