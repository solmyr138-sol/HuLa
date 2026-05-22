const base = import.meta.env.VITE_API_BASE || 'http://127.0.0.1:8080'

export async function api<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${base}${path}`, {
    headers: {
      'Content-Type': 'application/json',
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
