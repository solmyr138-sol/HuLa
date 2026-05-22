<template>
  <n-card title="后台用户管理">
    <n-space class="mb-12px">
      <n-input placeholder="请输入用户名称" style="width: 200px" />
      <n-button type="primary">搜索</n-button>
      <n-button>重置</n-button>
      <n-button type="info">+ 新增</n-button>
    </n-space>
    <n-data-table
      :columns="columns"
      :data="[{ username: 'admin', role: '超级管理员', createTime: '2023-05-09 23:37:30' }]" />
  </n-card>
</template>

<script setup lang="ts">
import type { DataTableColumns } from 'naive-ui'

const columns: DataTableColumns = [
  { type: 'selection' },
  { title: '用户名称', key: 'username' },
  { title: '角色', key: 'role' },
  { title: '创建时间', key: 'createTime' },
  { title: '操作', key: 'action' }
]
</script>
