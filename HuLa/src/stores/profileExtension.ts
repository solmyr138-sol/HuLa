import { defineStore } from 'pinia'
import { StoresEnum } from '@/enums'

export type ProfileExtensionFields = {
  birthday?: string
  region?: string
  phone?: string
}

export const useProfileExtensionStore = defineStore(StoresEnum.PROFILE_EXTENSION, () => {
  const byUid = ref<Record<string, ProfileExtensionFields>>({})

  const get = (uid: string): ProfileExtensionFields => {
    if (!uid) return {}
    return byUid.value[uid] ?? {}
  }

  const patch = (uid: string, patch: Partial<ProfileExtensionFields>) => {
    if (!uid) return
    byUid.value[uid] = { ...byUid.value[uid], ...patch }
  }

  return { byUid, get, patch }
})
