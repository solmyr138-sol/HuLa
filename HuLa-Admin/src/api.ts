const base = import.meta.env.VITE_API_BASE || 'http://127.0.0.1:8080'

export type PageResp<T> = {
  pageNo: number
  pageSize: number
  totalRecords: number
  isLast?: boolean
  list: T[]
}

export async function api<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${base}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      'tenant-id': localStorage.getItem('tenantId') || '1',
      Authorization: localStorage.getItem('token') ? `Bearer ${localStorage.getItem('token')}` : '',
      ...(init?.headers || {})
    },
    ...init
  })
  const json = await res.json()
  if (!json.isSuccess && json.code !== 0) {
    throw new Error(json.msg || '请求失败')
  }
  return json.data as T
}

export type TenantInfo = {
  tenantId: number
  inviteCode: string
  tenantName: string
  creditCode?: string
  registeredCount: number
  accountLimit: number
  expireTime?: string
  serverUrl?: string
  appDownloadUrl?: string
}

export type TenantPolicy = {
  forbidCreateGroup?: boolean
  forbidBroadcast?: boolean
  forbidMemberAddFriend?: boolean
  allowCrossTenantFriend?: boolean
  allowCrossTenantGroupInvite?: boolean
}

export type WhitelistUser = {
  id: number
  imUid: number
  account?: string
  mobile?: string
  nickname?: string
}

export type ImUserRow = {
  uid: number
  account?: string
  mobile?: string
  nickname?: string
  state?: number
  registerTime?: string
  deletedTime?: string
}
