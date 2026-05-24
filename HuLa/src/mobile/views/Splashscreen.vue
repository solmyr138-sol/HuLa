<template>
  <div id="loading-page" class="h-100vh"></div>
</template>

<script setup lang="ts">
import { useSettingStore } from '@/stores/setting'
import { useLogin } from '@/hooks/useLogin'
import { invoke } from '@tauri-apps/api/core'

const settingStore = useSettingStore()
const router = useRouter()
const { normalLogin } = useLogin()

const init = async () => {
  if (settingStore.login.autoLogin) {
    normalLogin('MOBILE', true, true)
  } else {
    router.push('/mobile/login')
    await invoke('hide_splash_screen')
  }
}

onMounted(() => {
  init()
})
</script>

<style scoped lang="scss">
#loading-page {
  z-index: 9999;
  background-color: #fff;
}
</style>
