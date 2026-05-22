<template>
  <n-card title="企业信息">
    <n-descriptions v-if="info" :column="1" label-placement="left" bordered>
      <n-descriptions-item label="企业号">{{ info.inviteCode }}</n-descriptions-item>
      <n-descriptions-item label="企业名称">{{ info.tenantName }}</n-descriptions-item>
      <n-descriptions-item label="工商执照注册号/统一社会信用代码">{{ info.creditCode || '—' }}</n-descriptions-item>
      <n-descriptions-item label="注册人数数量">
        {{ info.registeredCount }} / {{ info.accountLimit || '不限' }}
        <n-button text type="primary" class="ml-8px">扩容 &gt;</n-button>
      </n-descriptions-item>
      <n-descriptions-item label="服务到期时间">
        {{ info.expireTime || '—' }}
        <n-button text type="primary" class="ml-8px">续费 &gt;</n-button>
      </n-descriptions-item>
      <n-descriptions-item label="App下载地址">{{ info.appDownloadUrl || '—' }}</n-descriptions-item>
      <n-descriptions-item label="服务器地址">
        <n-space>
          <span>{{ info.serverUrl || '—' }}</span>
          <n-button size="small" @click="copyServer">复制</n-button>
        </n-space>
        <div class="hint">（客户端登录时可输入服务器地址加入企业）</div>
      </n-descriptions-item>
      <n-descriptions-item label="企业二维码">
        <n-qr-code :value="qrValue" :size="160" />
      </n-descriptions-item>
    </n-descriptions>
  </n-card>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useMessage } from 'naive-ui'
import { api, type TenantInfo } from '../../api'

const message = useMessage()
const info = ref<TenantInfo | null>(null)

const qrValue = computed(() => {
  if (!info.value) return ''
  return JSON.stringify({ code: info.value.inviteCode, server: info.value.serverUrl })
})

function copyServer() {
  if (!info.value?.serverUrl) return
  navigator.clipboard.writeText(info.value.serverUrl)
  message.success('已复制服务器地址')
}

onMounted(async () => {
  info.value = await api<TenantInfo>('/im/admin/tenant/info')
})
</script>

<style scoped>
.hint {
  color: #999;
  font-size: 12px;
  margin-top: 4px;
}
</style>
