import 'uno.css'
import '@unocss/reset/eric-meyer.css' // unocss提供的浏览器默认样式重置
import { setupI18n } from '@/services/i18n'
import { AppException } from '@/common/exception.ts'
import vResize from '@/directives/v-resize'
import vSlide from '@/directives/v-slide.ts'
import router from '@/router'
import { pinia } from '@/stores'
import { initializePlatform, isIOS, isMobile } from '@/utils/PlatformConstants'
import { startWebVitalObserver } from '@/utils/WebVitalsObserver'
import { invoke } from '@tauri-apps/api/core'
import App from '@/App.vue'

initializePlatform()
startWebVitalObserver()

if (isIOS()) {
  invoke('request_ios_badge_authorization').catch((error) => {
    console.warn('[HuLaBadge] 请求 iOS 角标权限失败', error)
  })
}

import('@/services/webSocketAdapter')

if (process.env.NODE_ENV === 'development') {
  import('@/utils/Console.ts').then((module) => {
    /**! 控制台打印项目版本信息(不需要可手动关闭)*/
    module.consolePrint()
  })

  if (isMobile()) {
    import('eruda').then((module) => {
      const eruda = 'default' in module ? module.default : module
      eruda.init()
    })
  }
}

export const forceUpdateMessageTop = (topValue: number) => {
  const messages = document.querySelectorAll('.n-message-container.n-message-container--top')

  messages.forEach((el) => {
    const dom = el as HTMLElement
    dom.style.top = `${topValue}px`
  })
}

if (isMobile()) {
  if (document.readyState === 'loading') {
    window.addEventListener('DOMContentLoaded', setup)
  } else {
    setup()
  }
}

async function setup() {
  await invoke('set_complete', { task: 'frontend' })
}

async function bootstrap() {
  const app = createApp(App)
  app.use(router).use(pinia)
  await setupI18n(app)
  app.directive('resize', vResize).directive('slide', vSlide)

  if (!__HULA_MOBILE_BUILD__ && !isMobile()) {
    const { default: TlbsMap } = await import('tlbs-map-vue')
    app.use(TlbsMap)
  }

  app.config.errorHandler = (err) => {
    if (err instanceof AppException) {
      window.$message.error(err.message)
      return
    }
    throw err
  }

  app.mount('#app')
}

void bootstrap()
