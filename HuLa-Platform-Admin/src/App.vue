<template>
  <n-config-provider :locale="zhCN">
    <n-message-provider>
      <router-view />
    </n-message-provider>
  </n-config-provider>
</template>

<script setup lang="ts">
import { zhCN } from 'naive-ui'
</script>
