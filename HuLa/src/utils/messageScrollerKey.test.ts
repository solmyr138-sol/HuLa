import { describe, expect, it } from 'vitest'
import type { MessageType } from '@/services/types'
import { resolveMessageScrollerKey } from '@/utils/messageScrollerKey'

const baseMsg = (overrides: Partial<MessageType['message']> = {}): MessageType =>
  ({
    message: {
      id: '100',
      roomId: 'room-1',
      sendTime: 1,
      type: 1,
      body: {},
      status: 1,
      ...overrides
    },
    fromUser: { uid: 'u1' }
  }) as MessageType

describe('resolveMessageScrollerKey', () => {
  it('uses message.id when present', () => {
    expect(resolveMessageScrollerKey(baseMsg(), 0)).toBe('100')
  })

  it('falls back to stable synthetic key when id is missing', () => {
    expect(resolveMessageScrollerKey(baseMsg({ id: undefined as unknown as string }), 3)).toBe('room-1_1_u1_3')
  })
})
