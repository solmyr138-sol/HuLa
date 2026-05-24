import type { MessageType } from '@/services/types'

/**
 * vue-virtual-scroller keyField only supports top-level keys or a resolver function —
 * not dot paths like "message.id".
 */
export function resolveMessageScrollerKey(item: MessageType, index: number): string {
  const id = item?.message?.id
  if (id != null && String(id) !== '') {
    return String(id)
  }

  const roomId = item?.message?.roomId ?? 'room'
  const sendTime = item?.message?.sendTime ?? 0
  const uid = item?.fromUser?.uid ?? 'unknown'
  return `${roomId}_${sendTime}_${uid}_${index}`
}
