<template>
  <MobileLayout>
    <MobileScaffold :background="false" :safe-area="false">
      <template #container>
        <div class="h-full flex-col-center gap-40px">
          <div class="flex-center absolute top-13vh left-36px">
            <p class="text-(20px #333) dark:text-gray-400">{{ t('login.mobile.welcome_title') }}</p>
            <img src="@/assets/mobile/2.svg" alt="" class="w-80px h-20px" />
          </div>

          <!-- 选项卡导航 -->
          <div class="w-80% h-40px absolute top-20vh flex-center">
            <div class="flex w-200px relative">
              <div
                @click="activeTab = 'login'"
                :class="[
                  'z-999 w-100px text-center transition-all duration-300 ease-out',
                  activeTab === 'login' ? 'text-(18px #000) dark:text-white' : 'text-(16px #666) dark:text-gray-400'
                ]">
                {{ t('login.mobile.tabs.login') }}
              </div>
              <div
                @click="activeTab = 'register'"
                :class="[
                  'z-999 w-100px text-center transition-all duration-300 ease-out',
                  activeTab === 'register' ? 'text-(18px #000) dark:text-white' : 'text-(16px #666) dark:text-gray-400'
                ]">
                {{ t('login.mobile.tabs.register') }}
              </div>
              <!-- 选中条 -->
              <div
                style="border-radius: 24px 42px 4px 24px"
                :class="[
                  'z-10 absolute bottom--4px h-6px w-34px bg-[--mobile-brand-primary] transition-all duration-300 ease-out',
                  activeTab === 'login' ? 'left-[33px]' : 'left-[133px]'
                ]"></div>
            </div>
          </div>

          <!-- 头像 -->
          <img v-if="activeTab === 'login'" :src="userInfo.avatar" alt="logo" class="size-86px rounded-full" />

          <!-- 登录表单 -->
          <n-flex v-if="activeTab === 'login'" class="text-center w-80%" vertical :size="16">
            <n-input
              :class="{ 'pl-22px': loginHistories.length > 0 }"
              size="large"
              v-model:value="userInfo.account"
              type="text"
              spellCheck="false"
              autoComplete="off"
              autoCorrect="off"
              autoCapitalize="off"
              :placeholder="accountPH"
              @focus="accountPH = ''"
              @blur="accountPH = t('login.mobile.input.account_placeholder')"
              clearable>
              <template #suffix>
                <n-flex v-if="loginHistories.length > 0" @click="arrowStatus = !arrowStatus">
                  <svg v-if="!arrowStatus" class="down w-18px h-18px color-#505050">
                    <use href="#down"></use>
                  </svg>
                  <svg v-else class="down w-18px h-18px color-#505050"><use href="#up"></use></svg>
                </n-flex>
              </template>
            </n-input>

            <!-- 账号选择框-->
            <div
              style="border: 1px solid rgba(70, 70, 70, 0.1)"
              v-if="loginHistories.length > 0 && arrowStatus"
              class="account-box absolute w-80% max-h-140px bg-#fdfdfd mt-45px z-99 rounded-8px p-8px box-border">
              <n-scrollbar style="max-height: 120px" trigger="none">
                <n-flex
                  vertical
                  v-for="item in loginHistories"
                  :key="item.account"
                  @click="giveAccount(item)"
                  class="p-8px hover:bg-#f3f3f3 hover:rounded-6px">
                  <div class="flex-between-center">
                    <n-avatar :src="AvatarUtils.getAvatarUrl(item.avatar)" class="size-28px bg-#ccc rounded-50%" />
                    <p class="text-14px color-#505050">{{ item.account }}</p>
                    <svg @click.stop="delAccount(item)" class="w-12px h-12px">
                      <use href="#close"></use>
                    </svg>
                  </div>
                </n-flex>
              </n-scrollbar>
            </div>

            <n-input
              class="pl-22px mt-8px"
              size="large"
              show-password-on="click"
              v-model:value="userInfo.password"
              type="password"
              spellCheck="false"
              autoComplete="off"
              autoCorrect="off"
              autoCapitalize="off"
              :placeholder="passwordPH"
              @focus="passwordPH = ''"
              @blur="passwordPH = t('login.mobile.input.code_placeholder')"
              clearable />

            <n-flex justify="flex-end" :size="6">
              <n-button text color="#4a90e2" @click="handleForgetPassword">
                {{ t('login.mobile.forget_code') }}
              </n-button>
            </n-flex>

            <n-button
              :loading="loading"
              :disabled="loginDisabled"
              tertiary
              style="color: #fff"
              class="w-full mt-8px mb-50px gradient-button"
              @click="normalLogin('MOBILE', true, false)">
              <span>{{ loginText }}</span>
            </n-button>

            <!-- 协议 -->
            <n-flex align="center" justify="center" :style="agreementStyle" :size="6" class="absolute bottom-0 w-[80%]">
              <n-checkbox v-model:checked="protocol" />
              <div class="text-12px color-#909090 cursor-default lh-14px">
                <span>{{ t('login.term.checkout.text1') }}</span>
                <span @click.stop="toServiceAgreement" class="color-#4a90e2 cursor-pointer">
                  {{ t('login.term.checkout.text2') }}
                </span>
                <span>{{ t('login.term.checkout.text3') }}</span>
                <span @click.stop="toPrivacyAgreement" class="color-#4a90e2 cursor-pointer">
                  {{ t('login.term.checkout.text4') }}
                </span>
              </div>
            </n-flex>
          </n-flex>

          <!-- 注册：第一步企业号 -->
          <n-flex v-if="activeTab === 'register' && currentStep === 1" class="text-center w-80%" vertical :size="16">
            <n-input
              size="large"
              v-model:value="registerInfo.enterpriseCode"
              type="text"
              :placeholder="t('auth.register.placeholders.enterprise_code')"
              clearable
              @blur="resolveEnterpriseOnMobile" />
            <p v-if="resolvedTenantName" class="text-12px text-[--mobile-brand-primary] w-full text-left">
              {{ t('auth.register.labels.enterprise_name') }}：{{ resolvedTenantName }}
            </p>
            <n-button
              tertiary
              style="color: #fff"
              class="w-full mt-8px mb-50px gradient-button"
              :disabled="!registerInfo.enterpriseCode?.trim() || !resolvedTenantName"
              @click="handleRegisterStep">
              <span>{{ t('auth.register.actions.next') }}</span>
            </n-button>
          </n-flex>

          <!-- 注册：第二步手机号+密码 -->
          <n-flex v-if="activeTab === 'register' && currentStep === 2" class="text-center w-80%" vertical :size="16">
            <n-input
              size="large"
              v-model:value="registerInfo.mobile"
              type="text"
              maxlength="11"
              :placeholder="t('auth.register.placeholders.mobile')"
              clearable />

            <n-input
              class="pl-16px"
              size="large"
              minlength="6"
              show-password-on="click"
              v-model:value="registerInfo.password"
              type="password"
              :allow-input="noSideSpace"
              :placeholder="registerPasswordPH"
              clearable />

            <n-input
              class="pl-16px"
              size="large"
              minlength="6"
              show-password-on="click"
              v-model:value="registerInfo.confirmPassword"
              type="password"
              :allow-input="noSideSpace"
              :placeholder="confirmPasswordPH"
              clearable />

            <n-flex vertical v-if="registerInfo.password" :size="10" class="mt-8px">
              <Validation
                :value="registerInfo.password"
                :message="t('login.mobile.register.pass_validate_info.minlength', { len: 6 })"
                :validator="validateMinLength" />
              <Validation
                :value="registerInfo.password"
                :message="t('login.mobile.register.pass_validate_info.valid_characters')"
                :validator="validateAlphaNumeric" />
              <Validation
                :value="registerInfo.password"
                :message="t('login.mobile.register.pass_validate_info.must_special_char')"
                :validator="validateSpecialChar" />
            </n-flex>

            <n-flex align="center" justify="center" :size="6" class="mt-10px">
              <n-checkbox v-model:checked="registerProtocol" />
              <div class="text-12px color-#909090 cursor-default lh-14px">
                <span>{{ t('login.term.checkout.text1') }}</span>
                <span @click.stop="toServiceAgreement" class="color-#4a90e2 cursor-pointer">{{ t('login.term.checkout.text2') }}</span>
                <span>{{ t('login.term.checkout.text3') }}</span>
                <span @click.stop="toPrivacyAgreement" class="color-#4a90e2 cursor-pointer">{{ t('login.term.checkout.text4') }}</span>
              </div>
            </n-flex>

            <n-flex class="w-full gap-10px">
              <n-button class="flex-1" @click="currentStep = 1">{{ t('auth.register.actions.back') }}</n-button>
              <n-button
                :loading="registerLoading"
                :disabled="!isStep2Valid"
                tertiary
                style="color: #fff"
                class="flex-1 gradient-button"
                @click="handleRegisterStep">
                <span>{{ t('auth.register.actions.submit') }}</span>
              </n-button>
            </n-flex>
          </n-flex>
        </div>
      </template>
    </MobileScaffold>
  </MobileLayout>
</template>

<script setup lang="ts">
import { useDebounceFn } from '@vueuse/core'
import { invoke } from '@tauri-apps/api/core'
import Validation from '@/components/common/Validation.vue'
import router from '@/router'
import type { UserInfoType } from '@/services/types'
import { resolveEnterpriseCode } from '@/services/enterprise'
import { useLoginHistoriesStore } from '@/stores/loginHistory.ts'
import { useMobileStore } from '@/stores/mobile'
import { AvatarUtils } from '@/utils/AvatarUtils'
import { registerByEnterpriseMobile } from '@/utils/ImRequestUtils'
import { isAndroid, isIOS } from '@/utils/PlatformConstants'
import { validateAlphaNumeric, validateSpecialChar } from '@/utils/Validate'
import { useMitt } from '../hooks/useMitt'
import { WsResponseMessageType } from '../services/wsType'
import { useSettingStore } from '../stores/setting'
import { clearListener } from '../utils/ReadCountQueue'
import { useLogin } from '../hooks/useLogin'
import { useI18n } from 'vue-i18n'

interface LocalRegisterInfo {
  enterpriseCode: string
  mobile: string
  password: string
  confirmPassword: string
}

const { t } = useI18n()
const loginHistoriesStore = useLoginHistoriesStore()
const { loginHistories } = loginHistoriesStore
const mobileStore = useMobileStore()
const safeArea = computed(() => mobileStore.safeArea)
const settingStore = useSettingStore()
const { login } = storeToRefs(settingStore)

const isJumpDirectly = ref(false)

/** 当前激活的选项卡 */
const activeTab = ref<'login' | 'register'>('login')

/** 当前注册步骤 */
const currentStep = ref(1)

/** 注册账号信息 */
const registerInfo = ref<LocalRegisterInfo>({
  enterpriseCode: '',
  mobile: '',
  password: '',
  confirmPassword: ''
})
const resolvedTenantName = ref('')
const mobilePattern = /^1[3-9]\d{9}$/

// 登录相关的占位符和状态
const accountPH = ref(t('login.mobile.input.account_placeholder'))
const passwordPH = ref(t('login.mobile.input.code_placeholder'))
const protocol = ref(true)
const arrowStatus = ref(false)

// 注册相关的占位符和状态
const registerPasswordPH = ref(t('login.mobile.register.input.password'))
const confirmPasswordPH = ref(t('login.mobile.register.input.confirm_password'))
const registerProtocol = ref(true)
const registerLoading = ref(false)
const { normalLogin, loading, loginText, loginDisabled, info: userInfo } = useLogin()

const agreementStyle = computed(() => {
  const inset = safeArea.value.bottom || 0
  if (isAndroid()) {
    return { bottom: `${inset + 10}px` }
  }
  if (inset > 0) {
    return { bottom: `${inset}px` }
  }
  return { bottom: 'var(--safe-area-inset-bottom)' }
})

/** 不允许输入空格 */
const noSideSpace = (value: string) => !value.startsWith(' ') && !value.endsWith(' ')

/** 密码验证函数 */
const validateMinLength = (value: string) => value.length >= 6

/** 检查密码是否满足所有条件 */
const isPasswordValid = computed(() => {
  const password = registerInfo.value.password
  return validateMinLength(password) && validateAlphaNumeric(password) && validateSpecialChar(password)
})

/** 检查第二步是否可以注册 */
const isStep2Valid = computed(() => {
  return (
    mobilePattern.test(registerInfo.value.mobile?.trim() || '') &&
    isPasswordValid.value &&
    registerInfo.value.confirmPassword === registerInfo.value.password &&
    registerProtocol.value
  )
})

async function resolveEnterpriseOnMobile() {
  const code = registerInfo.value.enterpriseCode?.trim()
  if (!code) {
    resolvedTenantName.value = ''
    return
  }
  try {
    const r = await resolveEnterpriseCode(code)
    resolvedTenantName.value = r.tenantName
  } catch {
    resolvedTenantName.value = ''
  }
}

// 监听登录表单变化
watchEffect(() => {
  loginDisabled.value = !(userInfo.value.account && userInfo.value.password && protocol.value)
  // 清空账号的时候设置默认头像
  if (!userInfo.value.account) {
    userInfo.value.avatar = '/logo.png'
  }
})

// 监听选项卡切换，重置状态
watch(activeTab, (newTab) => {
  if (newTab === 'login') {
    // 切换到登录时重置注册状态
    resetRegisterForm()
  } else {
    // 切换到注册时重置登录表单
    resetLoginForm()
  }
})

// 监听账号输入
watch(
  () => userInfo.value.account,
  (newAccount) => {
    if (!newAccount) {
      userInfo.value.avatar = '/logo.png'
      return
    }

    refreshAvatar(newAccount)
  }
)

/** 重置登录表单 */
const resetLoginForm = () => {
  userInfo.value = {
    account: '',
    password: '',
    avatar: '',
    uid: '',
    name: ''
  }
  accountPH.value = t('login.mobile.input.account_placeholder')
  passwordPH.value = t('login.mobile.input.code_placeholder')
  arrowStatus.value = false
}

/** 重置注册表单 */
const resetRegisterForm = () => {
  registerInfo.value = {
    enterpriseCode: '',
    mobile: '',
    password: '',
    confirmPassword: ''
  }
  resolvedTenantName.value = ''
  currentStep.value = 1
  registerPasswordPH.value = t('login.mobile.register.input.password')
  confirmPasswordPH.value = t('login.mobile.register.input.confirm_password')
}

/** 处理注册步骤 */
const handleRegisterStep = async () => {
  if (currentStep.value === 1) {
    await resolveEnterpriseOnMobile()
    if (!resolvedTenantName.value) {
      window.$message.warning(t('auth.register.messages.enterprise_invalid'))
      return
    }
    currentStep.value = 2
    return
  }
  await handleRegisterComplete()
}

/** 完成注册 */
const handleRegisterComplete = async () => {
  if (!isStep2Valid.value) {
    window.$message.warning(t('login.mobile.complete_info_before_register'))
    return
  }

  try {
    registerLoading.value = true
    await registerByEnterpriseMobile({
      enterpriseCode: registerInfo.value.enterpriseCode.trim(),
      mobile: registerInfo.value.mobile.trim(),
      password: registerInfo.value.password,
      confirmPassword: registerInfo.value.confirmPassword,
      systemType: 2
    })
    activeTab.value = 'login'
    userInfo.value.account = registerInfo.value.mobile
    window.$message.success(t('auth.register.messages.register_success'))
    resetRegisterForm()
  } catch (error) {
    window.$message.error(t('auth.register.messages.register_fail'))
    console.error(error)
  } finally {
    registerLoading.value = false
  }
}

/**
 * 给账号赋值
 * @param item 账户信息
 * */
const giveAccount = (item: UserInfoType) => {
  const { account, avatar, name, uid } = item
  userInfo.value.account = account || ''
  userInfo.value.avatar = avatar
  userInfo.value.name = name
  userInfo.value.uid = uid
  arrowStatus.value = false
}

/** 删除账号列表内容 */
const delAccount = (item: UserInfoType) => {
  // 获取删除前账户列表的长度
  const lengthBeforeDelete = loginHistories.length
  loginHistoriesStore.removeLoginHistory(item)
  // 判断是否删除了最后一个条目，并据此更新arrowStatus
  if (lengthBeforeDelete === 1 && loginHistories.length === 0) {
    arrowStatus.value = false
  }
  userInfo.value.account = ''
  userInfo.value.password = ''
  userInfo.value.avatar = '/logo.png'
}

const handleForgetPassword = () => {
  router.push({
    name: 'mobileForgetPassword'
  })
}

const closeMenu = (event: MouseEvent) => {
  const target = event.target as Element
  if (!target.matches('.account-box, .account-box *, .down')) {
    arrowStatus.value = false
  }
}

onBeforeMount(async () => {
  // const token = localStorage.getItem('TOKEN')
  // const refreshToken = localStorage.getItem('REFRESH_TOKEN')

  if (!login.value.autoLogin) {
    localStorage.removeItem('TOKEN')
    localStorage.removeItem('REFRESH_TOKEN')
    clearListener()
    return
  }

  // 只有在非自动登录的情况下才验证token并直接打开主窗口
  // if (token && refreshToken && !login.value.autoLogin) {
  //   isJumpDirectly.value = true
  //   try {
  //     // await openHomeWindow()
  //     return // 直接返回，不执行后续的登录相关逻辑
  //   } catch (error) {
  //     isJumpDirectly.value = false
  //     // token无效，清除token并重置状态
  //     localStorage.removeItem('TOKEN')
  //     localStorage.removeItem('REFRESH_TOKEN')
  //     userStore.userInfo = undefined
  //   }
  // }
})

const toServiceAgreement = () => {
  router.push({
    name: 'mobileServiceAgreement'
  })
}

const toPrivacyAgreement = () => {
  router.push({
    name: 'mobilePrivacyAgreement'
  })
}

const refreshAvatar = useDebounceFn((newAccount: string) => {
  const matchedAccount = loginHistories.find(
    (history) => history.account === newAccount || history.email === newAccount
  )
  if (matchedAccount) {
    userInfo.value.avatar = AvatarUtils.getAvatarUrl(matchedAccount.avatar)
  } else {
    userInfo.value.avatar = '/logo.png'
  }
}, 300)

onMounted(async () => {
  window.addEventListener('click', closeMenu, true)
  if (isIOS()) {
    invoke('set_webview_keyboard_adjustment', { enabled: false })
  }
  // 只有在需要登录的情况下才显示登录窗口
  if (isJumpDirectly.value) {
    loading.value = false
    router.push('/mobile/message')
    return
  }

  // 进入登录页面时立即隐藏首屏，确保无论登录成功或失败都能看到登录界面
  await invoke('hide_splash_screen')

  useMitt.on(WsResponseMessageType.NO_INTERNET, () => {
    loginDisabled.value = true
    loginText.value = t('login.status.service_disconnected')
  })

  if (login.value.autoLogin) {
    normalLogin('MOBILE', true, true)
  } else {
    loginHistories.length > 0 && giveAccount(loginHistories[0])
  }
})

onUnmounted(() => {
  window.removeEventListener('click', closeMenu, true)
  if (isIOS()) {
    invoke('set_webview_keyboard_adjustment', { enabled: false })
  }
})
</script>

<style scoped lang="scss">
@use '@/styles/scss/login';

.gradient-button {
  background: linear-gradient(to left, #4a90e2, #357abd);
  box-shadow:
    inset 0px 3px 16px 0px rgba(255, 255, 255, 0.5),
    inset 0px -2px 27px 0px rgba(53, 122, 189, 0.76),
    inset 0px 2px 6px 0px rgba(254, 254, 254, 0.5);
}

[data-theme='dark'] .gradient-button {
  background: linear-gradient(to left, #3a7bc8, #2d6aa8);
  box-shadow: 0 0 8px rgba(45, 106, 168, 0.4), inset 0 1px 3px rgba(0, 0, 0, 0.1);
}
</style>
