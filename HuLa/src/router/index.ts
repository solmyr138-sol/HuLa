import { invoke } from '@tauri-apps/api/core'
import { type } from '@tauri-apps/plugin-os'
import { createRouter, createWebHistory, type RouteLocationNormalized } from 'vue-router'

import { commonRoutes } from '@/router/common.routes'
import { desktopRoutes } from '@/router/desktop.routes'
import { mobileRoutes } from '@/router/mobile.routes'
import { TauriCommand } from '@/enums'
import { ensureAppStateReady } from '@/utils/AppStateReady'

const { BASE_URL } = import.meta.env

const isMobileRuntime = type() === 'ios' || type() === 'android'
const isMobileApp = __HULA_MOBILE_BUILD__ || isMobileRuntime

const getAllRoutes = () => {
  if (isMobileApp) {
    return mobileRoutes
  }
  return [...commonRoutes, ...desktopRoutes]
}

const router: any = createRouter({
  history: createWebHistory(BASE_URL),
  routes: getAllRoutes()
})

router.beforeEach(async (to: RouteLocationNormalized) => {
  if (!isMobileApp) {
    return true
  }

  try {
    await ensureAppStateReady()

    const isLoginPage = to.path === '/mobile/login'
    const isSplashPage = to.path === '/mobile/splashscreen'
    const isForgetPage = to.path === '/mobile/MobileForgetPassword'
    const isAgreementPage = to.path === '/mobile/serviceAgreement' || to.path === '/mobile/privacyAgreement'

    if (isSplashPage || isForgetPage || isAgreementPage) {
      return true
    }

    const tokens = await invoke<{ token: string | null; refreshToken: string | null }>(TauriCommand.GET_USER_TOKENS)
    const isLoggedIn = !!(tokens.token && tokens.refreshToken)

    if (!isLoggedIn && !isLoginPage) {
      return '/mobile/login'
    }

    return true
  } catch (error) {
    console.error('[守卫] 获取token错误:', error)
    if (to.path !== '/mobile/login') {
      return '/mobile/login'
    }
    return true
  }
})

export default router
