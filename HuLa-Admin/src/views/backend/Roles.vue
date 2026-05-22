<template>
  <n-card title="后台角色管理">
    <n-data-table :columns="columns" :data="[{ name: '超级管理员', code: 'super_admin' }]" />
  </n-card>
</template>

<script setup lang="ts">
import type { DataTableColumns } from 'naive-ui'

const columns: DataTableColumns = [
  { title: '角色名称', key: 'name' },
  { title: '角色编码', key: 'code' }
]
</script>
