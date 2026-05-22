<template>
  <n-card title="租户全局配置">
    <n-form v-if="policy" label-placement="left" label-width="200">
      <n-form-item label="禁止成员建群">
        <n-switch v-model:value="policy.forbidCreateGroup" />
      </n-form-item>
      <n-form-item label="禁止群发(转发/@所有人/群通知)">
        <n-switch v-model:value="policy.forbidBroadcast" />
      </n-form-item>
      <n-form-item label="禁止成员互加好友">
        <n-switch v-model:value="policy.forbidMemberAddFriend" />
      </n-form-item>
      <n-form-item label="允许跨租户加好友">
        <n-switch v-model:value="policy.allowCrossTenantFriend" />
      </n-form-item>
      <n-form-item>
        <n-button type="primary" @click="save">保存</n-button>
      </n-form-item>
    </n-form>
  </n-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { api } from '../api'

const policy = ref<any>(null)
onMounted(async () => {
  policy.value = await api('/im/admin/tenant/policy')
})
const save = async () => {
  await api('/im/admin/tenant/policy', { method: 'PUT', body: JSON.stringify(policy.value) })
}
</script>
