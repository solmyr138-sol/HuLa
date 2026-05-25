<template>
  <MobileScaffold :show-footer="false">
    <template #header>
      <HeaderBar border :isOfficial="false" :hidden-right="true" :room-name="t('mobile_setting.title')" />
    </template>

    <template #container>
      <div class="flex flex-col overflow-auto h-full">
        <div class="flex flex-col p-20px gap-20px">
          <div class="flex justify-between items-center bg-card text-card-foreground ring-1 p-12px rounded-lg shadow-sm">
            <div class="text-base">{{ t('mobile_setting.silent_label') }}</div>
            <n-switch v-model:value="messageSoundEnabled" />
          </div>

          <div
            v-for="item in settings"
            :key="item.key"
            class="flex justify-between items-center bg-card text-card-foreground ring-1 p-12px rounded-lg shadow-sm">
            <div class="text-base">{{ item.label }}</div>
            <div>
              <n-input v-if="item.type === 'input'" v-model:value="item.value" placeholder="请输入" class="w-40" />
              <n-select
                v-else-if="item.type === 'select'"
                v-model:value="item.value"
                :options="item.options"
                placeholder="请选择"
                class="w-40" />
            </div>
          </div>

          <div class="mt-auto flex justify-center mb-20px">
            <n-button type="error" @click="handleLogout" :disabled="isLoggingOut" :loading="isLoggingOut">
              {{ t('mobile_setting.button.logout') }}
            </n-button>
          </div>
        </div>
      </div>
    </template>
  </MobileScaffold>
</template>

<script setup lang="ts">
import { info } from '@tauri-apps/plugin-log'
import { isPermissionGranted, requestPermission } from '@tauri-apps/plugin-notification'
import { ThemeEnum } from '@/enums'
import { useGlobalStore } from '@/stores/global'
import { useSettingStore } from '@/stores/setting.ts'
import { useUserStore } from '@/stores/user'
import { useLogin } from '@/hooks/useLogin'
import { showDialog } from 'vant'
import 'vant/es/dialog/style'
import * as ImRequestUtils from '@/utils/ImRequestUtils'
import router from '@/router'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const globalStore = useGlobalStore()
const { isTrayMenuShow } = storeToRefs(globalStore)
const settingStore = useSettingStore()
const userStore = useUserStore()

const messageSoundEnabled = computed({
  get: () => settingStore.notification?.messageSound ?? true,
  set: async (enabled: boolean) => {
    if (enabled) {
      try {
        const granted = await isPermissionGranted()
        if (!granted) {
          const permission = await requestPermission()
          if (permission !== 'granted') {
            window.$message.warning('请在系统设置中允许通知权限')
            return
          }
        }
      } catch (error) {
        console.warn('[MobileSettings] 检查通知权限失败:', error)
      }
    }
    settingStore.setMessageSoundEnabled(enabled)
  }
})

const settings = reactive([
  {
    key: 'username',
    label: computed(() => t('mobile_setting.nickname')),
    type: 'input',
    value: computed({
      get: () => userStore.userInfo?.name || '',
      set: () => {}
    })
  },
  {
    key: 'theme',
    label: computed(() => t('mobile_setting.theme')),
    type: 'select',
    value: computed({
      get: () => settingStore.themes.content,
      set: (val) => settingStore.toggleTheme(val)
    }),
    options: [
      { label: computed(() => t('mobile_setting.themes.light')), value: ThemeEnum.LIGHT },
      { label: computed(() => t('mobile_setting.themes.dark')), value: ThemeEnum.DARK }
    ]
  },
  {
    key: 'language',
    label: computed(() => t('mobile_setting.language')),
    type: 'select',
    value: computed({
      get: () => settingStore.page.lang,
      set: (v) => {
        settingStore.page.lang = v
      }
    }),
    options: [
      {
        label: 'Automatic',
        value: 'AUTO'
      },
      {
        label: '简体中文',
        value: 'zh-CN'
      },
      {
        label: 'English',
        value: 'en'
      }
    ]
  }
])

const { logout, resetLoginState } = useLogin()

const isLoggingOut = ref(false)

async function handleLogout() {
  if (isLoggingOut.value) return
  isLoggingOut.value = true

  let logoutSuccess = false

  showDialog({
    title: '退出登录',
    message: '确定要退出登录吗？',
    showCancelButton: true,
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  })
    .then(async () => {
      try {
        await ImRequestUtils.logout({ autoLogin: true })
        logoutSuccess = true
      } catch (error) {
        console.error('服务器登出失败：', error)
      }

      try {
        await resetLoginState()
        await logout()

        settingStore.toggleLogin(false, false)
        info('登出账号')
        isTrayMenuShow.value = false

        if (logoutSuccess) {
          window.$message.success('登出成功')
        }
        await router.push('/mobile/login')
      } catch (localError) {
        console.error('本地登出清理失败：', localError)
        window.$message.error('本地登出清理失败，请重启应用')
      }
    })
    .catch(() => {
      info('用户点击取消')
    })
    .finally(() => {
      isLoggingOut.value = false
    })
}
</script>

<style lang="scss" scoped></style>
