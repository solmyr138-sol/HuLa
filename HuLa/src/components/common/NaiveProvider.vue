<template>
  <n-config-provider
    :theme-overrides="themes.content === ThemeEnum.DARK ? darkThemeOverrides : lightThemeOverrides"
    :theme="globalTheme"
    :locale="currentNaiveLocale"
    :date-locale="currentNaiveDateLocale">
    <n-loading-bar-provider>
      <n-dialog-provider>
        <n-notification-provider :max="notificMax">
          <n-message-provider :max="messageMax">
            <n-modal-provider>
              <slot></slot>
              <naive-provider-content />
            </n-modal-provider>
          </n-message-provider>
        </n-notification-provider>
      </n-dialog-provider>
    </n-loading-bar-provider>
  </n-config-provider>
</template>

<script setup lang="ts">
import { getCurrentWebviewWindow } from '@tauri-apps/api/webviewWindow'
import {
  darkTheme,
  dateEnUS,
  dateZhCN,
  enUS,
  type GlobalThemeOverrides,
  type NDateLocale,
  type NLocale,
  lightTheme,
  zhCN
} from 'naive-ui'
import { useI18n } from 'vue-i18n'
import { ThemeEnum } from '@/enums'
import { useSettingStore } from '@/stores/setting.ts'
import { isMobile } from '@/utils/PlatformConstants'

const brandPrimary = isMobile() ? '#4a90e2' : '#13987f'
const brandPrimarySoft = isMobile() ? '74, 144, 226' : '19, 152, 127'
const brandPrimaryProcess = isMobile() ? '#5a9ee8' : '#52aea3'

const { notificMax, messageMax } = defineProps<{
  notificMax?: number
  messageMax?: number
}>()
defineOptions({ name: 'NaiveProvider' })
const settingStore = useSettingStore()
const { themes } = storeToRefs(settingStore)
const { locale } = useI18n()

type NaiveLocalePack = {
  locale: NLocale
  dateLocale: NDateLocale
}

const naiveLocaleMap: Record<string, NaiveLocalePack> = {
  'zh-CN': { locale: zhCN, dateLocale: dateZhCN },
  zh: { locale: zhCN, dateLocale: dateZhCN },
  'en-US': { locale: enUS, dateLocale: dateEnUS },
  en: { locale: enUS, dateLocale: dateEnUS }
}

const defaultNaiveLocalePack = naiveLocaleMap['zh-CN']
const resolveNaiveLocale = (lang: string): NaiveLocalePack => naiveLocaleMap[lang] ?? defaultNaiveLocalePack
const currentNaiveLocale = computed(() => resolveNaiveLocale(locale.value).locale)
const currentNaiveDateLocale = computed(() => resolveNaiveLocale(locale.value).dateLocale)
/**监听深色主题颜色变化*/
const globalTheme = ref<any>(themes.value.content === ThemeEnum.DARK ? darkTheme : lightTheme)
const prefers = matchMedia('(prefers-color-scheme: dark)')
// 定义不需要显示消息提示的窗口
const noMessageWindows = ['tray', 'notify', 'capture', 'update', 'checkupdate']

const isValidContent = (theme?: string): theme is ThemeEnum => theme === ThemeEnum.DARK || theme === ThemeEnum.LIGHT

const applyThemeContent = (theme: ThemeEnum) => {
  globalTheme.value = theme === ThemeEnum.DARK ? darkTheme : lightTheme
  document.documentElement.dataset.theme = theme
  console.log(globalTheme.value)
}

const syncOsTheme = () => {
  if (themes.value.pattern !== ThemeEnum.OS) return
  settingStore.syncOsTheme()
}

const handlePrefersChange = () => {
  syncOsTheme()
}

let prefersListenerAttached = false
const attachPrefersListener = () => {
  if (prefersListenerAttached) return
  prefers.addEventListener('change', handlePrefersChange)
  prefersListenerAttached = true
}

const detachPrefersListener = () => {
  if (!prefersListenerAttached) return
  prefers.removeEventListener('change', handlePrefersChange)
  prefersListenerAttached = false
}

watch(
  () => themes.value.pattern,
  (pattern) => {
    if (pattern === ThemeEnum.OS) {
      syncOsTheme()
      attachPrefersListener()
      return
    }
    detachPrefersListener()
    settingStore.normalizeThemeState()
  },
  { immediate: true }
)

watch(
  () => themes.value.content,
  (content) => {
    if (!isValidContent(content)) {
      settingStore.normalizeThemeState()
      return
    }
    applyThemeContent(content)
  },
  { immediate: true }
)

onUnmounted(() => {
  detachPrefersListener()
})

const commonTheme: GlobalThemeOverrides = {
  Badge: {
    color: '#c14053'
  },
  Input: {
    borderRadius: '10px',
    borderHover: '0',
    // TODO: 不清楚为什么去掉边框
    // border: '0',
    borderDisabled: '0',
    borderFocus: '0',
    boxShadowFocus: '0'
  },
  Checkbox: {
    colorChecked: brandPrimary,
    borderChecked: `1px solid ${brandPrimary}`,
    borderFocus: `1px solid ${brandPrimary}`,
    boxShadowFocus: `0 0 0 2px rgba(${brandPrimarySoft}, 0.3)`
  },
  Tag: {
    borderRadius: '4px'
  },
  Button: {
    borderRadiusMedium: '10px',
    borderRadiusSmall: '6px',
    colorPrimary: brandPrimary
  },
  Tabs: {
    tabTextColorSegment: '#707070',
    tabPaddingMediumSegment: '4px',
    tabTextColorActiveLine: brandPrimary,
    tabTextColorHoverLine: brandPrimary,
    tabTextColorActiveBar: brandPrimary,
    tabTextColorHoverBar: brandPrimary,
    barColor: brandPrimary
  },
  Popover: {
    padding: '5px',
    borderRadius: '8px'
  },
  Dropdown: {
    borderRadius: '8px'
  },
  Avatar: {
    border: '1px solid #fff'
  },
  Switch: {
    railColorActive: brandPrimary,
    loadingColor: brandPrimary,
    boxShadowFocus: `0 0 0 2px rgba(${brandPrimarySoft}, 0.3)`
  },
  Radio: {
    boxShadowActive: `inset 0 0 0 1px ${brandPrimary}`,
    boxShadowFocus: `inset 0 0 0 1px ${brandPrimary},0 0 0 2px rgba(${brandPrimarySoft}, 0.3)`,
    boxShadowHover: `inset 0 0 0 1px ${brandPrimary}`,
    dotColorActive: brandPrimary
  },
  Message: {
    iconColorSuccess: brandPrimary,
    iconColorLoading: brandPrimary,
    loadingColor: brandPrimary,
    borderRadius: '8px'
  },
  Slider: {
    handleSize: '12px',
    fontSize: '10px',
    markFontSize: '8px',
    fillColor: brandPrimary,
    fillColorHover: brandPrimary,
    indicatorBorderRadius: '8px'
  },
  Notification: {
    borderRadius: '8px'
  },
  Steps: {
    indicatorBorderColorProcess: brandPrimary,
    indicatorColorProcess: brandPrimaryProcess
  },
  LoadingBar: {
    colorLoading: brandPrimary
  }
}

/** 浅色模式的主题颜色 */
const lightThemeOverrides: GlobalThemeOverrides = {
  ...commonTheme,
  Scrollbar: {
    color: '#d5d5d5',
    colorHover: '#c5c5c5'
  },
  Skeleton: {
    color: 'rgba(200, 200, 200, 0.6)',
    colorEnd: 'rgba(200, 200, 200, 0.2)'
  }
}

/** 深色模式的主题颜色 */
const darkThemeOverrides: GlobalThemeOverrides = {
  ...commonTheme,
  Scrollbar: {
    color: 'rgba(255, 255, 255, 0.2)',
    colorHover: 'rgba(255, 255, 255, 0.3)'
  }
}

// 挂载naive组件的方法至window, 以便在路由钩子函数和请求函数里面调用
const registerNaiveTools = () => {
  window.$loadingBar = useLoadingBar()
  window.$dialog = useDialog()
  window.$notification = useNotification()
  window.$modal = useModal()

  // 获取原始的消息对象
  const originalMessage = useMessage()

  // 创建一个空的消息对象，用于禁用消息的窗口
  const noOpMessage = {
    info: () => {},
    success: () => {},
    warning: () => {},
    error: () => {},
    loading: () => ({
      destroy: () => {},
      type: 'loading'
    }),
    create: () => ({
      destroy: () => {},
      type: 'info'
    }),
    destroyAll: () => {}
  } as unknown as ReturnType<typeof useMessage>

  // 检查当前路由是否需要禁用消息
  const shouldDisableMessage = () => {
    return noMessageWindows.includes(getCurrentWebviewWindow().label)
  }

  // 设置消息对象
  window.$message = shouldDisableMessage() ? noOpMessage : originalMessage
}

const NaiveProviderContent = defineComponent({
  name: 'NaiveProviderContent',
  setup() {
    registerNaiveTools()
  },
  render() {
    return h('div')
  }
})
</script>
<style>
.n-popover {
  zoom: var(--page-scale, 1);
}

.n-dropdown-menu {
  zoom: var(--page-scale, 1);
}

.n-tooltip {
  zoom: var(--page-scale, 1);
}

.n-modal {
  zoom: var(--page-scale, 1);
}

.n-drawer {
  zoom: var(--page-scale, 1);
}

.n-notification {
  zoom: var(--page-scale, 1);
}

.n-message {
  zoom: var(--page-scale, 1);
}

.n-date-picker-panel {
  zoom: var(--page-scale, 1);
}

.n-time-picker-panel {
  zoom: var(--page-scale, 1);
}

.n-cascader-menu {
  zoom: var(--page-scale, 1);
}

.n-select-menu {
  zoom: var(--page-scale, 1);
}

.n-popselect {
  zoom: var(--page-scale, 1);
}

.n-popselect-panel {
  zoom: var(--page-scale, 1);
}

.n-base-select-menu {
  zoom: var(--page-scale, 1);
}
</style>
