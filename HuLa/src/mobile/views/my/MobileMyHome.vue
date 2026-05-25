<template>
  <MobileScaffold :show-footer="false">
    <template #container>
      <div class="my-home">
        <div class="header-row">
          <h1 class="title">{{ t('enterprise.my_title') }}</h1>
          <div v-if="enterprise" class="enterprise-meta" @click="router.push('/mobile/mobileMy/editProfile')">
            <div class="meta-line">
              <span class="label">{{ t('enterprise.enterprise_code') }}</span>
              <span class="value">{{ enterprise.inviteCode }}</span>
              <svg class="w-14px h-14px"><use href="#right"></use></svg>
            </div>
            <div class="meta-name">{{ enterprise.tenantName }}</div>
          </div>
        </div>

        <div class="profile-card" @click="router.push('/mobile/mobileMy/editProfile')">
          <div class="card-top">
            <div>
              <div class="nick">{{ userStore.userInfo?.name || t('enterprise.my_title') }}</div>
              <div class="account">{{ t('mobile_personal_info.account') }}: {{ userStore.userInfo?.account }}</div>
            </div>
            <n-avatar :size="56" :src="AvatarUtils.getAvatarUrl(userStore.userInfo?.avatar ?? '')" round />
          </div>
          <div class="card-divider" />
          <div class="card-bottom" @click.stop="router.push({ path: '/mobile/myQRCode', query: { from: 'my' } })">
            <span>{{ t('enterprise.qr_card') }}</span>
            <svg class="w-18px h-18px"><use href="#right"></use></svg>
          </div>
        </div>

        <div class="menu-list">
          <div class="menu-item" @click="router.push('/mobile/mobileMy/settings')">
            <span class="menu-icon notify">🔔</span>
            <span class="flex-1">{{ t('enterprise.notify_settings') }}</span>
            <span :class="notifyFullyEnabled ? 'ok' : 'warn'">{{ notifyStatusText }}</span>
            <svg class="w-14px h-14px"><use href="#right"></use></svg>
          </div>
          <div class="menu-item">
            <span class="menu-icon version">⬆</span>
            <span class="flex-1">{{ t('enterprise.version_upgrade') }}</span>
            <span class="muted">当前 1.14.0</span>
            <svg class="w-14px h-14px"><use href="#right"></use></svg>
          </div>
          <div class="menu-item" @click="router.push('/mobile/mobileMy/settings')">
            <span class="menu-icon settings">⚙</span>
            <span class="flex-1">{{ t('enterprise.settings') }}</span>
            <svg class="w-14px h-14px"><use href="#right"></use></svg>
          </div>
        </div>

        <div class="menu-list mt-20px">
          <div class="menu-item" @click="restartApp">
            <span class="menu-icon restart">⏻</span>
            <span class="flex-1">{{ t('enterprise.restart_app') }}</span>
          </div>
        </div>
      </div>
    </template>
  </MobileScaffold>
</template>

<script setup lang="ts">
import { onActivated, onMounted, ref } from 'vue'
import { isPermissionGranted } from '@tauri-apps/plugin-notification'
import { useI18n } from 'vue-i18n'
import router from '@/router'
import { useUserStore } from '@/stores/user'
import { useSettingStore } from '@/stores/setting'
import { AvatarUtils } from '@/utils/AvatarUtils'
import { fetchEnterpriseProfile, type EnterpriseProfile } from '@/services/enterprise'

const { t } = useI18n()
const userStore = useUserStore()
const settingStore = useSettingStore()
const enterprise = ref<EnterpriseProfile | null>(null)
const systemNotifyGranted = ref(true)

const notifyFullyEnabled = computed(
  () => settingStore.notification?.messageSound !== false && systemNotifyGranted.value
)

const notifyStatusText = computed(() =>
  notifyFullyEnabled.value ? t('enterprise.notify_enabled') : t('enterprise.notify_partial')
)

const refreshNotifyStatus = async () => {
  try {
    systemNotifyGranted.value = await isPermissionGranted()
  } catch {
    systemNotifyGranted.value = true
  }
}

onMounted(async () => {
  userStore.getUserDetailAction()
  await refreshNotifyStatus()
  try {
    enterprise.value = await fetchEnterpriseProfile()
  } catch {
    enterprise.value = null
  }
})

onActivated(() => {
  void refreshNotifyStatus()
})

function restartApp() {
  window.location.reload()
}
</script>

<style scoped lang="scss">
.my-home {
  padding: 16px;
  background: var(--center-bg-color);
  min-height: 100%;
}
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}
.title {
  font-size: 22px;
  font-weight: 700;
  margin: 0;
}
.enterprise-meta {
  text-align: right;
  font-size: 12px;
  color: #666;
  max-width: 55%;
}
.meta-line {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 4px;
}
.meta-name {
  margin-top: 4px;
  color: #333;
}
.profile-card {
  background: #fff;
  border-radius: 12px;
  color: #333;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}
.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.nick {
  font-size: 18px;
  font-weight: 600;
}
.account {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
.card-divider {
  height: 1px;
  background: #f0f0f0;
  margin: 12px 0;
}
.card-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #666;
}
.menu-list {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}
.menu-item {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  gap: 10px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 15px;
}
.menu-item:last-child {
  border-bottom: none;
}
.warn {
  color: #c14053;
  font-size: 12px;
}
.ok {
  color: #079669;
  font-size: 12px;
}
.muted {
  color: #999;
  font-size: 12px;
}
</style>
