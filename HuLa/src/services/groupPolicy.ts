import { imRequest } from '@/utils/ImRequestUtils'

export type GroupPolicy = {
  roomId: number
  joinMode: number
  historyVisibleToNew: boolean
  groupMuteAll: boolean
  allowMemberAddFriend: boolean
  allowMemberDm: boolean
  allowMemberChangeNickname: boolean
  speakIntervalSec: number
}

export async function fetchGroupPolicy(roomId: number) {
  return imRequest<GroupPolicy>({
    url: '/im/room/group/policy',
    params: { id: roomId }
  })
}

export async function updateGroupPolicy(body: GroupPolicy) {
  return imRequest({
    url: '/im/room/group/policy',
    method: 'PUT',
    body
  })
}

export async function muteGroupMember(body: { roomId: number; uid: number; mutedUntil?: string | null }) {
  return imRequest({
    url: '/im/room/group/policy/member/mute',
    method: 'PUT',
    body
  })
}

export async function updateMemberAcl(body: {
  roomId: number
  uid: number
  canEditAnyMessage?: boolean
  canRecallAnyMessage?: boolean
}) {
  return imRequest({
    url: '/im/room/group/policy/member/acl',
    method: 'PUT',
    body
  })
}

