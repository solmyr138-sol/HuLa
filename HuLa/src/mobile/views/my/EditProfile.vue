<template>
  <MobileScaffold :show-footer="false" :safe-area="false">
    <template #header>
      <HeaderBar
        :isOfficial="false"
        :hidden-right="true"
        :enable-default-background="false"
        :enable-shadow="false"
        :room-name="t('enterprise.profile_title')" />
    </template>
    <template #container>
      <div class="flex flex-col overflow-auto h-full">
        <div class="p-20px">
          <div class="flex justify-center mb-50px">
            <div class="rounded-full relative bg-white w-86px h-86px overflow-hidden avatar-pick-target">
              <input
                type="file"
                accept="image/jpeg,image/png,image/webp"
                class="avatar-pick-input"
                @change="handleFileChange" />
              <n-avatar
                class="absolute pointer-events-none"
                :size="86"
                :src="profileAvatarUrl"
                fallback-src="/logo.png"
                round />
              <div
                class="absolute h-50% w-full bottom-0 bg-[rgb(50,50,50)] bg-clip-padding backdrop-filter backdrop-blur-sm bg-opacity-15 backdrop-saturate-100 backdrop-contrast-100 pointer-events-none"></div>
              <div class="absolute bottom-25% text-center w-full text-12px text-white pointer-events-none">
                {{ t('mobile_edit_profile.change_avatar') }}
              </div>
            </div>
            <AvatarCropper ref="cropperRef" v-model:show="showCropper" :image-url="localImageUrl" @crop="handleCrop" />
          </div>

          <n-card class="p-0! rounded-16px">
            <n-form label-placement="left" label-align="left" :label-width="80">
              <n-form-item :label="t('mobile_edit_profile.nickname')">
                <n-input
                  v-model:value="editName"
                  :maxlength="8"
                  :allow-input="noSideSpace"
                  spellcheck="false"
                  autocomplete="off"
                  autocorrect="off"
                  autocapitalize="off"
                  :placeholder="t('mobile_edit_profile.placeholder.nickname')"
                  class="bg-transparent!" />
              </n-form-item>

              <n-divider class="my-3! p-0!" />

              <n-form-item :label="t('mobile_edit_profile.gender')">
                <n-input
                  @click="pickerState.gender = true"
                  v-model:value="genderText"
                  readonly
                  :placeholder="t('mobile_edit_profile.placeholder.gender')"
                  class="bg-transparent!" />
              </n-form-item>

              <n-divider class="my-3! p-0!" />

              <n-form-item :label="t('mobile_edit_profile.brithday')">
                <n-input
                  @click="toEditBirthday"
                  v-model:value="birthday"
                  readonly
                  :placeholder="t('mobile_edit_profile.placeholder.brithday')"
                  class="bg-transparent!" />
              </n-form-item>
            </n-form>
          </n-card>

          <n-card class="p-0! rounded-16px mt-12px">
            <n-form label-placement="left" label-align="left" :label-width="80">
              <n-form-item :label="t('mobile_edit_profile.region')">
                <n-input
                  @click="pickerState.region = true"
                  v-model:value="region"
                  readonly
                  :placeholder="t('mobile_edit_profile.placeholder.region')"
                  class="bg-transparent!" />
              </n-form-item>

              <n-divider class="my-3! p-0!" />

              <n-form-item :label="t('mobile_edit_profile.phone')">
                <n-input
                  v-model:value="editPhone"
                  type="text"
                  inputmode="numeric"
                  :maxlength="11"
                  spellcheck="false"
                  autocomplete="off"
                  autocorrect="off"
                  autocapitalize="off"
                  :placeholder="t('mobile_edit_profile.placeholder.phone')"
                  class="bg-transparent!" />
              </n-form-item>

              <n-divider class="my-3! p-0!" />

              <n-form-item :label="t('mobile_edit_profile.bio')">
                <n-input
                  type="textarea"
                  v-model:value="localUserInfo.resume"
                  :placeholder="t('mobile_edit_profile.placeholder.bio')"
                  class="bg-transparent!"
                  @click="toEditBio"
                  readonly />
              </n-form-item>

              <n-drawer
                v-model:show="pickerState.gender"
                class="rounded-t-20px! overflow-hidden"
                position="bottom"
                round
                placement="bottom"
                default-height="300px">
                <van-picker
                  :columns="pickerColumn.gender"
                  @confirm="pickerConfirm.gender"
                  @cancel="pickerState.gender = false" />
              </n-drawer>

              <area-drawer
                v-model:show="pickerState.region"
                @confirm="pickerConfirm.region"
                @cancel="pickerState.region = false" />

              <div class="flex justify-center mt-20px pb-10px">
                <n-button block type="primary" strong secondary round :loading="saving" @click="saveEditInfo">
                  {{ t('mobile_edit_profile.save_btn') }}
                </n-button>
              </div>
            </n-form>
          </n-card>
        </div>
      </div>
    </template>
  </MobileScaffold>
</template>

<script setup lang="ts">
import { onBeforeRouteUpdate, useRoute } from 'vue-router'
import { useAvatarUpload } from '@/hooks/useAvatarUpload'
import router from '@/router'
import type { ModifyUserInfoType, UserInfoType } from '@/services/types.ts'
import { useGroupStore } from '@/stores/group'
import { useLoginHistoriesStore } from '@/stores/loginHistory'
import { useUserStore } from '@/stores/user.ts'
import { AvatarUtils } from '@/utils/AvatarUtils'
import { getUserDetail, ModifyUserInfo, uploadAvatar } from '@/utils/ImRequestUtils'
import { getProfileExtension, patchProfileExtension, resolveDisplayPhone } from '@/utils/profileExtension'
import { useI18n } from 'vue-i18n'
import { fetchEnterpriseProfile, type EnterpriseProfile } from '@/services/enterprise'
import { AppException } from '@/common/exception'

const { t } = useI18n()
const groupStore = useGroupStore()
const userStore = useUserStore()
const loginHistoriesStore = useLoginHistoriesStore()
const route = useRoute()
const enterprise = ref<EnterpriseProfile | null>(null)
const saving = ref(false)
const loadedUid = ref('')
const editName = ref('')
const editPhone = ref('')

const localUserInfo = ref<Partial<ModifyUserInfoType>>({
  name: '',
  sex: 1,
  phone: '',
  avatar: '',
  resume: '',
  modifyNameChance: 0
} as ModifyUserInfoType)

const profileAvatarUrl = computed(() => {
  const raw = localUserInfo.value.avatar || userStore.userInfo?.avatar || ''
  const url = AvatarUtils.getAvatarUrl(raw)
  const ts = userStore.userInfo?.avatarUpdateTime
  if (!raw || AvatarUtils.isDefaultAvatar(raw) || url === '/logoD.png') return url
  const sep = url.includes('?') ? '&' : '?'
  return ts ? `${url}${sep}t=${ts}` : url
})

const genderText = computed(() => {
  const item = pickerColumn.value.gender.find((i) => i.value === localUserInfo.value.sex)
  return item ? item.text : ''
})

const region = ref('')
const birthday = ref('')

const pickerColumn = ref({
  gender: [
    { text: t('mobile_edit_profile.genders.male'), value: 1 },
    { text: t('mobile_edit_profile.genders.female'), value: 2 }
  ]
})

const resolveRegionLabel = (payload: {
  selectedOptions?: Array<{ text?: string; name?: string }>
}) => {
  const selected = payload?.selectedOptions ?? []
  return selected
    .map((item) => item.text ?? item.name ?? '')
    .filter(Boolean)
    .join('/')
}

const pickerConfirm = {
  gender: (data: { selectedOptions: Array<{ value: number }> }) => {
    localUserInfo.value.sex = data.selectedOptions[0]?.value ?? localUserInfo.value.sex
    pickerState.value.gender = false
  },
  region: (payload: { selectedOptions?: Array<{ text?: string; name?: string }> }) => {
    region.value = resolveRegionLabel(payload)
    pickerState.value.region = false
  }
}

const pickerState = ref({
  gender: false,
  region: false
})

const noSideSpace = (value: string) => !value.startsWith(' ') && !value.endsWith(' ')
const mobilePattern = /^1[3-9]\d{9}$/

const resolveAvatarForSave = (userInfo: UserInfoType) => {
  const avatar = localUserInfo.value.avatar || userInfo.avatar
  if (avatar && avatar.trim()) return avatar
  return '/logoD.png'
}

const applyExtensionFields = (userInfo: UserInfoType) => {
  const extension = getProfileExtension(userInfo.uid)
  editPhone.value = resolveDisplayPhone(userInfo.account, userInfo.phone, extension.phone)
  birthday.value = userInfo.birthday || extension.birthday || ''
  region.value = userInfo.region || extension.region || ''
}

const loadProfileFields = (force = false) => {
  const userInfo = userStore.userInfo
  if (!userInfo?.uid) return
  if (!force && loadedUid.value === userInfo.uid) return

  editName.value = userInfo.name ?? ''
  localUserInfo.value = {
    ...userInfo,
    sex: userInfo.sex,
    avatar: userInfo.avatar,
    resume: userInfo.resume,
    modifyNameChance: userInfo.modifyNameChance
  }
  applyExtensionFields(userInfo)
  loadedUid.value = userInfo.uid
}

const refreshBirthdayFromStore = () => {
  const uid = userStore.userInfo?.uid
  if (!uid) return
  const extension = getProfileExtension(uid)
  birthday.value = userStore.userInfo?.birthday || extension.birthday || birthday.value
}

const {
  localImageUrl,
  showCropper,
  handleFileChange,
  handleCrop: onCrop
} = useAvatarUpload({
  onSuccess: async (downloadUrl) => {
    if (!downloadUrl) {
      window.$message.error('上传头像失败，未获取到文件地址')
      return
    }
    try {
      await uploadAvatar({ avatar: downloadUrl })
      localUserInfo.value.avatar = downloadUrl
      userStore.userInfo!.avatar = downloadUrl
      userStore.userInfo!.avatarUpdateTime = Date.now()
      const history = loginHistoriesStore.loginHistories.find((item) => item.uid === userStore.userInfo!.uid)
      if (history) {
        history.avatar = downloadUrl
      }
      updateCurrentUserCache('avatar', downloadUrl)
      loginHistoriesStore.updateLoginHistory(<UserInfoType>userStore.userInfo)
      window.$message.success(t('home.profile_edit.toast.avatar_update_success'))
    } catch (error) {
      console.error('[EditProfile] 保存头像失败:', error)
      window.$message.error('头像保存失败')
    }
  }
})

const handleCrop = async (cropBlob: Blob) => {
  await onCrop(cropBlob)
}

const updateCurrentUserCache = (key: 'name' | 'wearingItemId' | 'avatar', value: string) => {
  const currentUser = userStore.userInfo?.uid && groupStore.getUserInfo(userStore.userInfo.uid)
  if (currentUser) {
    currentUser[key] = value
  }
}

const toEditBirthday = () => {
  router.push('/mobile/mobileMy/editBirthday')
}

const toEditBio = () => {
  router.push('/mobile/mobileMy/editBio')
}

const saveEditInfo = async () => {
  if (saving.value) return

  const userInfo = userStore.userInfo
  if (!userInfo) {
    window.$message.error('用户信息缺失')
    return
  }

  const nextName = editName.value.trim()
  if (!nextName) {
    window.$message.error('昵称不能为空')
    return
  }

  const previousName = userInfo.name
  const nextPhone = editPhone.value.trim()
  if (nextPhone && !mobilePattern.test(nextPhone)) {
    window.$message.error('请输入正确的手机号')
    return
  }

  const nextRegion = region.value.trim()
  const nextBirthday = birthday.value.trim()
  const nextAvatar = resolveAvatarForSave(userInfo)

  saving.value = true
  try {
    patchProfileExtension(userInfo.uid, {
      birthday: nextBirthday,
      region: nextRegion,
      phone: nextPhone
    })

    await ModifyUserInfo({
      name: nextName,
      sex: localUserInfo.value.sex!,
      phone: nextPhone,
      avatar: nextAvatar,
      resume: localUserInfo.value.resume ?? userInfo.resume ?? '',
      region: nextRegion,
      birthday: nextBirthday,
      modifyNameChance: localUserInfo.value.modifyNameChance ?? userInfo.modifyNameChance
    })

    userInfo.name = nextName
    userInfo.sex = localUserInfo.value.sex!
    userInfo.phone = nextPhone
    userInfo.region = nextRegion
    userInfo.birthday = nextBirthday
    userInfo.resume = localUserInfo.value.resume ?? userInfo.resume ?? ''
    userInfo.avatar = nextAvatar

    localUserInfo.value.name = nextName
    localUserInfo.value.phone = nextPhone
    localUserInfo.value.avatar = nextAvatar
    editName.value = nextName
    editPhone.value = nextPhone

    try {
      const detail = (await getUserDetail()) as Partial<UserInfoType>
      Object.assign(userInfo, userStore.mergeProfileExtension(detail), {
        name: nextName,
        phone: nextPhone,
        region: nextRegion,
        birthday: nextBirthday
      })
    } catch (error) {
      console.warn('[EditProfile] 刷新用户详情失败，使用本地已保存数据', error)
    }

    loginHistoriesStore.updateLoginHistory(<UserInfoType>userInfo)
    updateCurrentUserCache('name', nextName)

    if (localUserInfo.value.modifyNameChance && nextName !== previousName) {
      localUserInfo.value.modifyNameChance -= 1
      userInfo.modifyNameChance = localUserInfo.value.modifyNameChance
    }

    window.$message.success('修改成功')
  } catch (error) {
    console.error('[EditProfile] 保存失败:', error)
    const message = error instanceof AppException ? error.message : error instanceof Error ? error.message : '保存失败'
    window.$message.error(message)
  } finally {
    saving.value = false
  }
}

watch(
  () => route.name,
  (name) => {
    if (name !== 'mobileEditProfile') return
    refreshBirthdayFromStore()
    localUserInfo.value.resume = userStore.userInfo?.resume ?? localUserInfo.value.resume
  }
)

onBeforeRouteUpdate((to, from) => {
  if (to.name === 'mobileEditProfile' && from.name === 'mobileEditBirthday') {
    refreshBirthdayFromStore()
  }
  if (to.name === 'mobileEditProfile' && from.name === 'mobileEditBio') {
    localUserInfo.value.resume = userStore.userInfo?.resume ?? localUserInfo.value.resume
  }
})

onMounted(async () => {
  loadProfileFields(true)
  try {
    enterprise.value = await fetchEnterpriseProfile()
  } catch {
    enterprise.value = null
  }
})
</script>

<style lang="scss" scoped>
@use '@/styles/scss/form-item.scss';

.avatar-pick-input {
  position: absolute;
  inset: 0;
  z-index: 2;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
}
</style>
