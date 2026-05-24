import { useProfileExtensionStore } from '@/stores/profileExtension'

export type ProfileExtension = {
  birthday?: string
  region?: string
  phone?: string
}

export const getProfileExtension = (uid: string): ProfileExtension => {
  return useProfileExtensionStore().get(uid)
}

export const patchProfileExtension = (uid: string, patch: Partial<ProfileExtension>) => {
  useProfileExtensionStore().patch(uid, patch)
}

export const resolveDisplayPhone = (account?: string, phone?: string, savedPhone?: string) => {
  const normalizedPhone = phone?.trim() || savedPhone?.trim()
  if (normalizedPhone) return normalizedPhone
  const normalizedAccount = account?.trim() ?? ''
  return /^1[3-9]\d{9}$/.test(normalizedAccount) ? normalizedAccount : ''
}
