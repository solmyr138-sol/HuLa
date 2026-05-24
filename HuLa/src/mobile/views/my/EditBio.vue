<template>
  <MobileScaffold :show-footer="false">
    <template #header>
      <HeaderBar :isOfficial="false" border :hidden-right="true" :room-name="t('mobile_edit_bio.title')" />
    </template>

    <template #container>
      <div class="flex flex-col overflow-auto h-full">
        <div class="flex flex-col flex-1 gap-20px py-15px px-20px">
          <n-form class="rounded-15px p-10px shadow" label-placement="left" label-width="100px">
            <n-form-item>
              <n-input
                v-model:value="localBio"
                type="textarea"
                :placeholder="t('mobile_edit_bio.placeholder')"
                class="w-full"
                :autosize="bioAutosize"
                :maxlength="300"
                :show-count="true" />
            </n-form-item>
          </n-form>

          <div class="flex justify-center">
            <n-button @click="handleSave" block type="primary" strong secondary>
              {{ t('mobile_edit_bio.save_btn') }}
            </n-button>
          </div>
        </div>
      </div>
    </template>
  </MobileScaffold>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user.ts'
import { useLoginHistoriesStore } from '@/stores/loginHistory'
import { ModifyUserInfo } from '@/utils/ImRequestUtils'
import { getProfileExtension } from '@/utils/profileExtension'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
const userStore = useUserStore()
const loginHistoriesStore = useLoginHistoriesStore()
const bioAutosize = { minRows: 5, maxRows: 20 }

const router = useRouter()
const localBio = ref(userStore.userInfo?.resume || '')

// 保存个人简介
const handleSave = async () => {
  const userInfo = userStore.userInfo
  if (!userInfo) {
    window.$message.error('用户信息缺失')
    return
  }

  const extension = getProfileExtension(userInfo.uid)
  const avatar = userInfo.avatar?.trim() || '/logoD.png'

  try {
    await ModifyUserInfo({
      name: userInfo.name,
      sex: userInfo.sex,
      phone: userInfo.phone ?? extension.phone ?? '',
      avatar,
      resume: localBio.value,
      region: userInfo.region ?? extension.region ?? '',
      birthday: userInfo.birthday ?? extension.birthday ?? '',
      modifyNameChance: userInfo.modifyNameChance
    })
    userInfo.resume = localBio.value
    loginHistoriesStore.updateLoginHistory(userInfo)
    window.$message.success('简介已保存')
    router.back()
  } catch (error) {
    console.error('[EditBio] 保存失败:', error)
    window.$message.error('保存失败')
  }
}

onMounted(() => {
  localBio.value = userStore.userInfo?.resume || ''
})
</script>

<style lang="scss" scoped>
@use '@/styles/scss/form-item.scss';
</style>
