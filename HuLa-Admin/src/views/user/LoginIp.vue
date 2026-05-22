<template>
  <n-card title="登录IP管理">
    <n-space class="mb-12px" wrap>
      <n-input v-model:value="keyword" placeholder="账号" clearable style="width: 160px" />
      <n-input v-model:value="ip" placeholder="IP" clearable style="width: 160px" />
      <n-button type="primary">搜索</n-button>
      <n-button>重置</n-button>
    </n-space>
    <n-data-table :columns="columns" :data="[]" />
    <n-empty class="mt-24px" description="暂无数据" />
  </n-card>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { DataTableColumns } from 'naive-ui'

const keyword = ref('')
const ip = ref('')

const columns: DataTableColumns = [
  { title: '账号', key: 'account' },
  { title: '手机号码', key: 'mobile' },
  { title: '昵称', key: 'nickname' },
  { title: '登录IP', key: 'ip' },
  { title: 'IP状态', key: 'ipState' },
  { title: '操作时间', key: 'time' },
  { title: '操作结果', key: 'result' },
  { title: '操作', key: 'action' }
]
</script>
