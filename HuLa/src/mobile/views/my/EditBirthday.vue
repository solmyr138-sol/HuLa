<template>
  <MobileScaffold :show-footer="false">
    <template #header>
      <HeaderBar :isOfficial="false" border :hidden-right="true" :room-name="t('mobile_edit_brithday.title')" />
    </template>

    <template #container>
      <div class="flex flex-col overflow-auto h-full">
        <div class="flex flex-col flex-1 gap-20px py-15px px-20px">
          <n-date-picker
            v-model:value="birthdayTimestamp"
            panel
            type="date"
            block
            class="m-auto rounded-16px"
            :is-date-disabled="disableFutureDate" />

          <div class="flex justify-center mt-20px">
            <n-button strong secondary round type="primary" block @click="handleSave">
              {{ t('mobile_edit_brithday.save_btn') }}
            </n-button>
          </div>
        </div>
      </div>
    </template>
  </MobileScaffold>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user.ts'
import { getProfileExtension, patchProfileExtension } from '@/utils/profileExtension'

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()

const birthdayTimestamp = ref<number | null>(null)

const disableFutureDate = (timestamp: number) => timestamp > Date.now()

const handleSave = () => {
  const uid = userStore.userInfo?.uid
  if (!uid) {
    window.$message.error('用户信息缺失')
    return
  }
  if (!birthdayTimestamp.value) {
    window.$message.warning(t('mobile_edit_profile.placeholder.brithday'))
    return
  }

  const birthdayStr = dayjs(birthdayTimestamp.value).format('YYYY-MM-DD')
  patchProfileExtension(uid, { birthday: birthdayStr })
  if (userStore.userInfo) {
    userStore.userInfo.birthday = birthdayStr
  }

  window.$message.success('生日已保存')
  router.back()
}

onMounted(() => {
  const uid = userStore.userInfo?.uid
  if (!uid) return
  const saved = getProfileExtension(uid).birthday
  if (saved) {
    const parsed = dayjs(saved)
    if (parsed.isValid()) {
      birthdayTimestamp.value = parsed.valueOf()
    }
  }
})
</script>

<style lang="scss" scoped></style>
