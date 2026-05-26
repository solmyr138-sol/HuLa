<template>
  <div v-if="groupStore.isAdminOrLord()" class="box-item cursor-default mt-10px">
    <n-flex vertical :size="8">
      <p class="text-(12px #909090) pb-8px">{{ t('home.chat_header.sidebar.group.policy.title') }}</p>

      <div class="flex-between-center">
        <p>{{ t('home.chat_header.sidebar.group.policy.join_mode') }}</p>
        <n-select
          size="small"
          class="w-140px"
          v-model:value="policy.joinMode"
          :options="joinModeOptions"
          @update:value="savePolicy" />
      </div>

      <div class="flex-between-center">
        <p>{{ t('home.chat_header.sidebar.group.policy.history') }}</p>
        <n-switch size="small" v-model:value="policy.historyVisibleToNew" @update:value="savePolicy" />
      </div>

      <div class="flex-between-center">
        <p>{{ t('home.chat_header.sidebar.group.policy.mute_all') }}</p>
        <n-switch size="small" v-model:value="policy.groupMuteAll" @update:value="savePolicy" />
      </div>

      <div class="flex-between-center">
        <p>{{ t('home.chat_header.sidebar.group.policy.add_friend') }}</p>
        <n-switch size="small" v-model:value="policy.allowMemberAddFriend" @update:value="savePolicy" />
      </div>

      <div class="flex-between-center">
        <p>{{ t('home.chat_header.sidebar.group.policy.dm') }}</p>
        <n-switch size="small" v-model:value="policy.allowMemberDm" @update:value="savePolicy" />
      </div>

      <div class="flex-between-center">
        <p>{{ t('home.chat_header.sidebar.group.policy.nickname') }}</p>
        <n-switch size="small" v-model:value="policy.allowMemberChangeNickname" @update:value="savePolicy" />
      </div>

      <div class="flex-between-center">
        <p>{{ t('home.chat_header.sidebar.group.policy.speak_interval') }}</p>
        <n-select
          size="small"
          class="w-140px"
          v-model:value="policy.speakIntervalSec"
          :options="speakOptions"
          @update:value="savePolicy" />
      </div>

      <n-divider class="my-8px!" />
      <p class="text-(12px #909090)">{{ t('home.chat_header.sidebar.group.policy.member_acl') }}</p>
      <div class="flex-between-center">
        <p>{{ t('home.chat_header.sidebar.group.policy.mute_member') }}</p>
        <n-input-number
          size="small"
          class="w-140px"
          v-model:value="muteMinutes"
          :min="0"
          placeholder="分钟" />
        <n-button size="tiny" @click="applyMute">应用</n-button>
      </div>
      <div class="flex-between-center">
        <p>{{ t('home.chat_header.sidebar.group.policy.can_edit') }}</p>
        <n-switch size="small" v-model:value="memberAcl.canEditAnyMessage" @update:value="saveMemberAcl" />
      </div>
      <div class="flex-between-center">
        <p>{{ t('home.chat_header.sidebar.group.policy.can_recall') }}</p>
        <n-switch size="small" v-model:value="memberAcl.canRecallAnyMessage" @update:value="saveMemberAcl" />
      </div>
      <n-select
        size="small"
        class="w-full"
        v-model:value="aclTargetUid"
        :options="memberOptions"
        placeholder="选择成员" />
    </n-flex>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import {
  fetchGroupPolicy,
  muteGroupMember,
  updateGroupPolicy,
  updateMemberAcl,
  type GroupPolicy
} from '@/services/groupPolicy'
import { useGroupStore } from '@/stores/group'

const { t } = useI18n()
const groupStore = useGroupStore()

const aclTargetUid = ref<number | null>(null)
const muteMinutes = ref(0)
const memberAcl = ref({ canEditAnyMessage: false, canRecallAnyMessage: false })

const memberOptions = computed(() =>
  (groupStore.userList ?? []).map((m: { uid: string; name: string }) => ({
    label: m.name,
    value: Number(m.uid)
  }))
)

const policy = ref<GroupPolicy>({
  roomId: 0,
  joinMode: 3,
  historyVisibleToNew: true,
  groupMuteAll: false,
  allowMemberAddFriend: true,
  allowMemberDm: true,
  allowMemberChangeNickname: true,
  speakIntervalSec: 0
})

const joinModeOptions = [
  { label: t('home.chat_header.sidebar.group.policy.join_admin'), value: 1 },
  { label: t('home.chat_header.sidebar.group.policy.join_member'), value: 2 },
  { label: t('home.chat_header.sidebar.group.policy.join_open'), value: 3 }
]

const speakOptions = [
  { label: t('home.chat_header.sidebar.group.policy.speak_off'), value: 0 },
  { label: '5s', value: 5 },
  { label: '10s', value: 10 },
  { label: '30s', value: 30 },
  { label: '1min', value: 60 },
  { label: '5min', value: 300 },
  { label: '15min', value: 900 },
  { label: '30min', value: 1800 },
  { label: '1h', value: 3600 }
]

const load = async () => {
  const roomId = Number(groupStore.countInfo?.roomId)
  if (!roomId) return
  policy.value = await fetchGroupPolicy(roomId)
}

const savePolicy = async () => {
  try {
    await updateGroupPolicy({ ...policy.value, roomId: Number(groupStore.countInfo?.roomId) })
    window.$message?.success(t('home.chat_header.sidebar.group.policy.saved'))
  } catch {
    window.$message?.error(t('home.chat_header.sidebar.group.policy.save_fail'))
  }
}

const saveMemberAcl = async () => {
  if (!aclTargetUid.value) return
  try {
    await updateMemberAcl({
      roomId: Number(groupStore.countInfo?.roomId),
      uid: aclTargetUid.value,
      canEditAnyMessage: memberAcl.value.canEditAnyMessage,
      canRecallAnyMessage: memberAcl.value.canRecallAnyMessage
    })
    window.$message?.success(t('home.chat_header.sidebar.group.policy.saved'))
  } catch {
    window.$message?.error(t('home.chat_header.sidebar.group.policy.save_fail'))
  }
}

const applyMute = async () => {
  if (!aclTargetUid.value || !muteMinutes.value) return
  const d = new Date(Date.now() + muteMinutes.value * 60_000)
  const pad = (n: number) => String(n).padStart(2, '0')
  const until = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
  try {
    await muteGroupMember({
      roomId: Number(groupStore.countInfo?.roomId),
      uid: aclTargetUid.value,
      mutedUntil: until
    })
    window.$message?.success(t('home.chat_header.sidebar.group.policy.saved'))
  } catch {
    window.$message?.error(t('home.chat_header.sidebar.group.policy.save_fail'))
  }
}

watch(
  () => groupStore.countInfo?.roomId,
  () => load(),
  { immediate: true }
)
</script>
