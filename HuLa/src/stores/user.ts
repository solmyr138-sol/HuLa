import { defineStore } from 'pinia'
import { StoresEnum } from '@/enums'
import type { UserInfoType } from '@/services/types'
import { getUserDetail } from '@/utils/ImRequestUtils'
import { getProfileExtension, resolveDisplayPhone } from '@/utils/profileExtension'
import * as PathUtil from '@/utils/PathUtil'
import { useGlobalStore } from './global'

export const useUserStore = defineStore(
  StoresEnum.USER,
  () => {
    const userInfo = ref<UserInfoType>()
    const globalStore = useGlobalStore()

    const mergeProfileExtension = (res: Partial<UserInfoType>) => {
      const uid = userInfo.value?.uid ?? res.uid
      const extension = uid ? getProfileExtension(uid) : {}
      return {
        ...userInfo.value,
        ...res,
        name: res.name ?? userInfo.value?.name,
        phone: resolveDisplayPhone(res.account, res.phone, extension.phone),
        region: res.region || extension.region || userInfo.value?.region,
        birthday: res.birthday || extension.birthday || userInfo.value?.birthday
      } as UserInfoType
    }

    const getUserDetailAction = () => {
      getUserDetail()
        .then((res: Partial<UserInfoType>) => {
          userInfo.value = mergeProfileExtension(res)
        })
        .catch((e) => {
          console.error('获取用户详情失败:', e)
        })
    }

    const isMe = computed(() => (id: string) => {
      return userInfo.value?.uid === id
    })

    const getUserRoomDir = async () => {
      return await PathUtil.getUserVideosDir(userInfo.value!.uid, globalStore.currentSessionRoomId)
    }

    const getUserRoomAbsoluteDir = async () => {
      return await PathUtil.getUserAbsoluteVideosDir(userInfo.value!.uid, globalStore.currentSessionRoomId)
    }

    return { userInfo, getUserDetailAction, mergeProfileExtension, isMe, getUserRoomDir, getUserRoomAbsoluteDir }
  },
  {
    share: {
      enable: true,
      initialize: true
    }
  }
)
