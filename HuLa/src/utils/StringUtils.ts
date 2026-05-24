/**
 * 给一个不是'/'开头的字符串的头部添加一个'/'
 * @param originPath
 */
export const addSlashToHead = (originPath: string) => {
  return originPath.startsWith('/') ? originPath : '/' + originPath
}

const INTERNAL_LOC_PLACE_PATTERN = /^(内网\s*IP|内部\s*IP|局域网|本机地址|本地链路|localhost|local)$/i

/**
 * 过滤内网 IP 归属地占位文案；合法城市名原样返回，否则返回空字符串。
 */
export const formatLocPlace = (locPlace?: string | null): string => {
  if (!locPlace) return ''
  const trimmed = locPlace.trim()
  if (!trimmed || INTERNAL_LOC_PLACE_PATTERN.test(trimmed)) return ''
  return trimmed
}
