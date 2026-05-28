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
    'tenant-id': localStorage.getItem('tenantId') || '1',
    tenantId: localStorage.getItem('tenantId') || '1'
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
  tenantId?: number
}

/** 租户 IM 运营后台：仅需账号+密码；企业管理员账号会自动解析企业ID */
export async function loginAdmin(account: string, password: string) {
  const tenantId = localStorage.getItem('tenantId') || '1'
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
        ? '账号或登录类型不正确：请使用 IM 账号或 admin_邀请码'
        : json.msg
    throw new Error(hint || '登录失败')
  }
  return json.data as LoginResult
}

export type TenantInfo = {
  tenantId: number
  inviteCode: string
  tenantName: string
  logo?: string
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
  defUserId?: number
  account?: string
  mobile?: string
  nickname?: string
  state?: number
  registerTime?: string
  deletedTime?: string
  lastLoginIp?: string
}

type UserSearchRow = {
  uid: string
  name: string
  account: string
  avatar?: string
}

/** 企业信息：优先 IM 管理端；旧版 IM 服务无该接口时走 OAuth */
export async function getTenantInfo(): Promise<TenantInfo> {
  try {
    return await api<TenantInfo>('/im/admin/tenant/info')
  } catch {
    const ent = await api<{ tenantId: number | string; inviteCode: string; tenantName: string }>(
      '/oauth/anyone/enterprise'
    )
    let registeredCount = 0
    try {
      const users = await api<PageResp<UserSearchRow>>('/im/user/search?pageNo=1&pageSize=1')
      registeredCount = Number(users.totalRecords) || 0
    } catch {
      /* ignore */
    }
    return {
      tenantId: Number(ent.tenantId),
      inviteCode: ent.inviteCode,
      tenantName: ent.tenantName,
      registeredCount,
      accountLimit: 0
    }
  }
}

/** 注册用户 / 注销用户分页（deletedOnly=true 仅查注销审计表） */
export async function listImUsers(params: {
  pageNo: number
  pageSize: number
  keyword?: string
  deletedOnly?: boolean
}): Promise<PageResp<ImUserRow>> {
  const q = new URLSearchParams({
    pageNo: String(params.pageNo),
    pageSize: String(params.pageSize),
    keyword: params.keyword?.trim() ?? '',
    deletedOnly: String(!!params.deletedOnly)
  })
  const res = await api<PageResp<ImUserRow>>(`/im/admin/user/page?${q}`)
  return {
    ...res,
    pageNo: Number(res.pageNo),
    pageSize: Number(res.pageSize),
    totalRecords: Number(res.totalRecords),
    list: (res.list ?? []).map((row) => ({
      ...row,
      uid: Number(row.uid),
      defUserId: row.defUserId != null ? Number(row.defUserId) : undefined
    }))
  }
}

export async function updateImUser(body: {
  uid: number
  nickname?: string
  email?: string
  state?: number
}) {
  return api<void>('/im/admin/user/update', {
    method: 'PUT',
    body: JSON.stringify(body)
  })
}

type OssToken = { uploadUrl: string; downloadUrl: string }

/** 上传企业 LOGO（MinIO/七牛预签名，与客户端一致） */
export async function uploadTenantLogoFile(file: File): Promise<string> {
  const q = new URLSearchParams({
    scene: 'avatar',
    fileName: file.name
  })
  const cred = await api<OssToken>(`/system/anyTenant/ossToken?${q}`)
  const res = await fetch(cred.uploadUrl, {
    method: 'PUT',
    body: file,
    headers: { 'Content-Type': file.type || 'application/octet-stream' }
  })
  if (!res.ok) {
    throw new Error('上传图片失败')
  }
  return cred.downloadUrl
}

export async function updateTenantLogo(logo: string) {
  return api<void>('/im/admin/tenant/logo', {
    method: 'PUT',
    body: JSON.stringify({ logo })
  })
}

export async function resetImUserPassword(body: {
  uid: number
  password: string
  confirmPassword: string
}) {
  return api<void>('/im/admin/user/reset-password', {
    method: 'PUT',
    body: JSON.stringify(body)
  })
}

export type GroupChatRow = {
  groupId: number
  roomId: number
  groupName?: string
  account?: string
  avatar?: string
  memberNum?: number
  onlineNum?: number
  ownerName?: string
  createTime?: string | number
}

/** 租户群聊分页（IM 管理端 /room/group/page） */
export async function listGroupChats(params: {
  pageNo: number
  pageSize: number
  groupNameKeyword?: string
}): Promise<PageResp<GroupChatRow>> {
  const q = new URLSearchParams({
    pageNo: String(params.pageNo),
    pageSize: String(params.pageSize)
  })
  const kw = params.groupNameKeyword?.trim()
  if (kw) q.set('groupNameKeyword', kw)
  const res = await api<PageResp<GroupChatRow>>(`/im/room/group/page?${q}`)
  return {
    ...res,
    pageNo: Number(res.pageNo),
    pageSize: Number(res.pageSize),
    totalRecords: Number(res.totalRecords),
    list: (res.list ?? []).map((row) => ({
      ...row,
      groupId: Number(row.groupId),
      roomId: Number(row.roomId),
      memberNum: row.memberNum != null ? Number(row.memberNum) : undefined,
      onlineNum: row.onlineNum != null ? Number(row.onlineNum) : undefined
    }))
  }
}

