<template>
  <n-config-provider :theme="naiveTheme" data-tauri-drag-region class="login-box size-full rounded-8px select-none flex flex-col">
    <ActionBar :max-w="false" :shrink="false" />

    <n-flex vertical justify="center" :size="25" class="w-full mt--40px flex-1 pointer-events-none">
      <n-flex class="ma text-center w-260px pointer-events-auto" vertical :size="16">
        <n-flex justify="center" align="center">
          <span class="text-(24px #70938c) textFont">{{ t('auth.register.title') }}</span>
          <img class="w-100px h-40px" src="/hula.png" alt="" />
        </n-flex>

        <n-steps :current="currentStep" size="small" class="mb-8px">
          <n-step :title="t('auth.register.steps.enterprise')" />
          <n-step :title="t('auth.register.steps.account')" />
        </n-steps>

        <n-form v-if="currentStep === 1" :model="form" :rules="rulesStep1" ref="formStep1">
          <n-form-item path="enterpriseCode">
            <n-input
              size="large"
              v-model:value="form.enterpriseCode"
              :placeholder="t('auth.register.placeholders.enterprise_code')"
              clearable
              @blur="resolveEnterprise" />
          </n-form-item>
          <p v-if="resolvedTenantName" class="text-12px text-#70938c text-left mb-8px">
            {{ t('auth.register.labels.enterprise_name') }}：{{ resolvedTenantName }}
          </p>
          <n-button block type="primary" :disabled="!form.enterpriseCode?.trim()" @click="goStep2">
            {{ t('auth.register.actions.next') }}
          </n-button>
        </n-form>

        <n-form v-else :model="form" :rules="rulesStep2" ref="formStep2">
          <n-form-item path="mobile">
            <n-input
              size="large"
              v-model:value="form.mobile"
              :placeholder="t('auth.register.placeholders.mobile')"
              clearable />
          </n-form-item>
          <n-form-item path="password">
            <n-input
              size="large"
              show-password-on="click"
              v-model:value="form.password"
              type="password"
              :placeholder="t('auth.register.placeholders.password')"
              clearable />
          </n-form-item>
          <n-form-item path="confirmPassword">
            <n-input
              size="large"
              show-password-on="click"
              v-model:value="form.confirmPassword"
              type="password"
              :placeholder="t('auth.register.placeholders.confirm_placeholder')"
              clearable />
          </n-form-item>
          <n-flex vertical v-if="form.password" :size="8" class="mb-8px text-left">
            <Validation :value="form.password" :message="t('auth.register.password_hints.min_length')" :validator="validateMinLength" />
            <Validation :value="form.password" :message="t('auth.register.password_hints.alpha_numeric')" :validator="validateAlphaNumeric" />
            <Validation :value="form.password" :message="t('auth.register.password_hints.special_char')" :validator="validateSpecialChar" />
          </n-flex>
          <n-flex align="center" :size="6">
            <n-checkbox v-model:checked="protocol" />
            <div class="text-12px color-#909090">
              <span>{{ t('login.term.checkout.text1') }}</span>
              <span class="color-#13987f cursor-pointer" @click.stop="openServiceAgreement">{{ t('login.term.checkout.text2') }}</span>
              <span>{{ t('login.term.checkout.text3') }}</span>
              <span class="color-#13987f cursor-pointer" @click.stop="openPrivacyAgreement">{{ t('login.term.checkout.text4') }}</span>
            </div>
          </n-flex>
          <n-flex :size="8" class="mt-8px">
            <n-button @click="currentStep = 1">{{ t('auth.register.actions.back') }}</n-button>
            <n-button block type="primary" :loading="registerLoading" :disabled="!canSubmit" @click="submitRegister">
              {{ t('auth.register.actions.submit') }}
            </n-button>
          </n-flex>
        </n-form>
      </n-flex>
    </n-flex>

    <p class="text-12px text-#909090 text-center pb-12px pointer-events-none">© {{ currentYear }} HuLa</p>
  </n-config-provider>
</template>

<script setup lang="ts">
import { getCurrentWebviewWindow, WebviewWindow } from '@tauri-apps/api/webviewWindow'
import dayjs from 'dayjs'
import { darkTheme, lightTheme, type FormInst } from 'naive-ui'
import { storeToRefs } from 'pinia'
import { useI18n } from 'vue-i18n'
import Validation from '@/components/common/Validation.vue'
import { useWindow } from '@/hooks/useWindow'
import { useSettingStore } from '@/stores/setting'
import { resolveEnterpriseCode } from '@/services/enterprise'
import { registerByEnterpriseMobile } from '@/utils/ImRequestUtils'
import { validateAlphaNumeric, validateSpecialChar } from '@/utils/Validate'

const { t } = useI18n()
const settingStore = useSettingStore()
const { themes } = storeToRefs(settingStore)
const naiveTheme = computed(() => (themes.value.content === 'dark' ? darkTheme : lightTheme))
const { createModalWindow } = useWindow()

const currentStep = ref(1)
const resolvedTenantName = ref('')
const protocol = ref(true)
const registerLoading = ref(false)
const currentYear = dayjs().year()

const form = reactive({
  enterpriseCode: '',
  mobile: '',
  password: '',
  confirmPassword: ''
})

const rulesStep1 = {
  enterpriseCode: { required: true, message: t('auth.register.form.rules.enterprise_code_required'), trigger: 'blur' }
}

const mobilePattern = /^1[3-9]\d{9}$/
const rulesStep2 = {
  mobile: {
    required: true,
    trigger: 'blur',
    validator(_: unknown, v: string) {
      if (!mobilePattern.test((v || '').trim())) return new Error(t('auth.register.form.rules.mobile_format'))
      return true
    }
  },
  password: { required: true, message: t('auth.register.form.rules.password_required'), trigger: 'blur' },
  confirmPassword: {
    required: true,
    trigger: 'blur',
    validator() {
      return form.confirmPassword === form.password || new Error(t('auth.register.form.rules.confirm_mismatch'))
    }
  }
}

const formStep1 = ref<FormInst | null>(null)
const formStep2 = ref<FormInst | null>(null)

const validateMinLength = (v: string) => v.length >= 6
const isPasswordValid = computed(
  () => validateMinLength(form.password) && validateAlphaNumeric(form.password) && validateSpecialChar(form.password)
)
const canSubmit = computed(
  () => protocol.value && mobilePattern.test(form.mobile.trim()) && isPasswordValid.value && form.confirmPassword === form.password
)

async function resolveEnterprise() {
  const code = form.enterpriseCode?.trim()
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

async function goStep2() {
  try {
    await formStep1.value?.validate()
    await resolveEnterprise()
    if (!resolvedTenantName.value) {
      window.$message.warning(t('auth.register.messages.enterprise_invalid'))
      return
    }
    currentStep.value = 2
  } catch {
    /* validation */
  }
}

async function submitRegister() {
  try {
    await formStep2.value?.validate()
  } catch {
    return
  }
  registerLoading.value = true
  try {
    await registerByEnterpriseMobile({
      enterpriseCode: form.enterpriseCode.trim(),
      mobile: form.mobile.trim(),
      password: form.password,
      confirmPassword: form.confirmPassword,
      systemType: 2
    })
    window.$message.success(t('auth.register.messages.register_success'))
    setTimeout(() => {
      WebviewWindow.getByLabel('login')?.then((w) => w?.setFocus())
      WebviewWindow.getCurrent().close()
    }, 1200)
  } catch {
    window.$message.error(t('auth.register.messages.register_fail'))
  } finally {
    registerLoading.value = false
  }
}

const openServiceAgreement = () => createModalWindow(t('login.term.checkout.text2'), 'modal-serviceAgreement', 600, 600, 'login')
const openPrivacyAgreement = () => createModalWindow(t('login.term.checkout.text4'), 'modal-privacyAgreement', 600, 600, 'login')

onMounted(() => getCurrentWebviewWindow().show())
</script>

<style scoped>
.login-box {
  background: var(--center-bg-color);
}
</style>
