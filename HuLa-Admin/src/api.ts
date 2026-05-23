const base = import.meta.env.VITE_API_BASE || 'http://127.0.0.1:18760/api'

// 与 HuLa 客户端一致：Authorization 为 clientId:secret 的 Base64，不要加 "Basic " 前缀
const oauthClientId = import.meta.env.VITE_OAUTH_CLIENT_ID || 'luohuo_web_pro'
const oauthClientSecret = import.meta.env.VITE_OAUTH_CLIENT_SECRET || 'luohuo_web_pro_secret'

function apiSuccess(json: { success?: boolean; code?: number }) {
  return json.success === true || json.code === 0 || json.code === 200
}

export type PageResp<T> = {
  pageNo: number
  pageSize: number
  totalRecords: number
  isLast?: boolean
  list: T[]
}

function authHeaders(extra?: HeadersInit): Record<string, string> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    'tenant-id': localStorage.getItem('tenantId') || '1'
  }
  const token = localStorage.getItem('token')
  // 与 HuLa 客户端 / 网关 Sa-Token 一致：请求头名为 token（非 Authorization: Bearer）
  if (token) headers.token = token
  if (extra && typeof extra === 'object' && !Array.isArray(extra)) {
    Object.assign(headers, extra as Record<string, string>)
  }
  return headers
}

export async function api<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${base}${path}`, {
    ...init,
    headers: authHeaders(init?.headers as Record<string, string> | undefined)
  })
  const json = await res.json()
  if (!apiSuccess(json)) {
    const expired = json.code === 401 || json.code === 406 || /token/i.test(json.msg || '')
    if (expired) {
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      window.location.href = '/login'
    }
    throw new Error(json.msg || '请求失败')
  }
  return json.data as T
}

export type LoginResult = {
  token: string
  refreshToken?: string
  uid?: number
}

/** 租户 IM 运营后台：与客户端相同，使用 IM 登录（systemType=2），账号为手机号 */
export async function loginAdmin(account: string, password: string, tenantId = '1') {
  const basic = btoa(`${oauthClientId}:${oauthClientSecret}`)
  const res = await fetch(`${base}/oauth/anyTenant/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: basic,
      'tenant-id': tenantId
    },
    body: JSON.stringify({
      grantType: 'PASSWORD',
      account,
      password,
      clientId: 'hula-admin-web',
      systemType: 2,
      deviceType: 'PC'
    })
  })
  const json = await res.json()
  if (!apiSuccess(json)) {
    const hint =
      json.code === -6
        ? '账号或登录类型不正确：请用 IM 注册手机号登录，不要用 admin'
        : json.msg
    throw new Error(hint || '登录失败')
  }
  return json.data as LoginResult
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

