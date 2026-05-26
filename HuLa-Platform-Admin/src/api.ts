const base = import.meta.env.VITE_API_BASE || 'http://127.0.0.1:8080'

export async function api<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${base}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      Token: localStorage.getItem('token') || '',
      ...(init?.headers || {})
    },
    ...init
  })
  const json = await res.json()

  if (json.code === 406 || json.code === 40001 || json.code === 40009) {
    localStorage.removeItem('token')
    localStorage.removeItem('nickName')
    window.location.href = '/login'
    throw new Error('登录已过期，请重新登录')
  }

  if (!json.isSuccess && json.code !== 0 && json.code !== 200) {
    throw new Error(json.msg || '请求失败')
  }
  return json.data as T
}

export type DefTenant = {
  id: number
  name: string
  inviteCode?: string
  creditCode?: string
  state?: boolean
  status?: number
  accountCount?: number
  expireTime?: string
  createTime?: string
}
