<template>
  <n-card title="群聊列表">
    <n-space class="mb-12px">
      <n-input v-model:value="keyword" placeholder="群名称" clearable style="width: 200px" />
      <n-button type="primary">搜索</n-button>
      <n-button>重置</n-button>
    </n-space>
    <n-data-table :columns="columns" :data="rows" :loading="loading" />
  </n-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import type { DataTableColumns } from 'naive-ui'
import { api } from '../../api'

const keyword = ref('')
const loading = ref(false)
const rows = ref<any[]>([])

const columns: DataTableColumns = [
  { title: '群名称', key: 'name' },
  { title: '群主', key: 'owner' },
  { title: '成员数', key: 'count' },
  { title: '创建时间', key: 'createTime' }
]

onMounted(async () => {
  loading.value = true
  try {
    const stats = await api<any>('/im/admin/stats/home')
    rows.value = []
    if (stats?.totalGroup) {
      /* 群列表专用接口待扩展，先展示统计占位 */
    }
  } finally {
    loading.value = false
  }
})
</script>
