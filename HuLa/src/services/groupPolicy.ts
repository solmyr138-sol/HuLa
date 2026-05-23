import { ImUrlEnum } from '@/enums'
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
    url: ImUrlEnum.FETCH_GROUP_POLICY,
    params: { id: roomId }
  })
}

export async function updateGroupPolicy(body: GroupPolicy) {
  return imRequest({
    url: ImUrlEnum.UPDATE_GROUP_POLICY,
    body
  })
}

export async function muteGroupMember(body: { roomId: number; uid: number; mutedUntil?: string | null }) {
  return imRequest({
    url: ImUrlEnum.MUTE_GROUP_MEMBER,
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
    url: ImUrlEnum.UPDATE_GROUP_MEMBER_ACL,
    body
  })
}

