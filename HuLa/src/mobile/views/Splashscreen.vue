<template>
  <!-- 🚀 加载页 DOM -->
  <div id="loading-page" class="h-100vh" :style="splashStyle"></div>
</template>

<script setup lang="ts">
import splashBg from '@/assets/mobile/2.svg'
import { useSettingStore } from '@/stores/setting'
import { useLogin } from '@/hooks/useLogin'
import { invoke } from '@tauri-apps/api/core'

const settingStore = useSettingStore()
const router = useRouter()
const { normalLogin } = useLogin()
const splashStyle = {
  backgroundImage: `url(${splashBg})`
}

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
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  opacity: 1;
}
</style>
