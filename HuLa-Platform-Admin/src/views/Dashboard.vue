<template>
  <n-card title="平台概览">
    <n-grid :cols="2" :x-gap="12">
      <n-gi><n-statistic label="企业总数" :value="stats?.tenantTotal ?? '-'" /></n-gi>
      <n-gi><n-statistic label="启用企业" :value="stats?.tenantActive ?? '-'" /></n-gi>
    </n-grid>
    <p class="hint">对应架构中的 devOperation 应用；HuLa-Admin 为单租户运营后台。</p>
  </n-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { api } from '../api'

const stats = ref<{ tenantTotal: number; tenantActive: number } | null>(null)
onMounted(async () => {
  try {
    stats.value = await api('/base/platform/tenant/stats')
  } catch {
    stats.value = { tenantTotal: 0, tenantActive: 0 }
  }
})
</script>

<style scoped>
.hint {
  margin-top: 16px;
  color: #888;
  font-size: 13px;
}
</style>
