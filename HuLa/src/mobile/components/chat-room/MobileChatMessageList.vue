<template>
  <div class="mobile-chat-main flex flex-col overflow-hidden h-full relative" @click="handleChatAreaClick">
    <div
      v-if="networkBanner"
      class="z-999 w-full h-40px rounded-4px text-(12px [--danger-text]) bg-[--danger-bg] flex-shrink-0 flex-center gap-6px">
      <svg class="size-16px">
        <use href="#cloudError"></use>
      </svg>
      {{ networkBanner.text }}
    </div>

    <div
      ref="scrollWrapRef"
      class="flex-1 min-h-0 overflow-y-auto mobile-chat-scroller-wrap"
      @scroll="handleVirtualScroll">
      <div
        v-show="chatStore.shouldShowNoMoreMessage"
        class="flex-center gap-6px h-32px flex-shrink-0 cursor-default select-none">
        <p class="text-(12px #909090)">{{ t('home.chat_main.no_more') }}</p>
      </div>

      <div class="flex flex-col">
        <template v-for="(item, index) in scrollerItems" :key="resolveMessageScrollerKey(item, index)">
          <span v-if="item.timeBlock" class="text-(12px #909090) select-none p-4px block text-center" @click.stop>
            {{ timeToStr(item.message.sendTime) }}
          </span>
          <div
            :data-message-index="index"
            :data-message-id="item.message.id"
            :class="[
              'mobile-msg-row msg-container select-none',
              item.message.type === MsgEnum.RECALL ? 'min-h-22px' : 'min-h-62px',
              isGroup ? 'p-[14px_10px_14px_20px]' : 'chat-single p-[4px_10px_10px_20px]',
              { 'active-reply': activeReply === item.message.id },
              { 'bg-#90909020': computeMsgHover(item) }
            ]"
            @click="onMessageClick(item)">
            <RenderMessage
              :message="item"
              :is-group="isGroup"
              :from-user="{ uid: item.fromUser.uid }"
              :upload-progress="item.uploadProgress"
              @jump2-reply="jumpToReplyMsg" />
          </div>
        </template>
      </div>
    </div>

    <footer
      v-if="shouldShowFloatFooter && currentNewMsgCount"
      class="absolute bottom-16px right-16px z-10"
      @click="scrollToBottom">
      <div class="float-box text-(12px [--mobile-brand-primary]) px-12px py-6px rounded-full bg-[--bg-popover] shadow-md">
        {{ t('home.chat_main.new_messages', { count: newMsgCountLabel }) }}
      </div>
    </footer>

    <FileUploadProgress />
  </div>
</template>

<script setup lang="ts">
import { useDebounceFn } from '@vueuse/core'
import { useI18n } from 'vue-i18n'
import { MittEnum, MsgEnum, ScrollIntentEnum } from '@/enums'
import { chatMainInjectionKey, useChatMain } from '@/hooks/useChatMain'
import { useAutoScrollGuard } from '@/hooks/useAutoScrollGuard'
import { useMitt } from '@/hooks/useMitt'
import { useNetworkStatus } from '@/hooks/useNetworkStatus'
import type { MessageType } from '@/services/types'
import { useChatStore } from '@/stores/chat'
import { useGlobalStore } from '@/stores/global'
import { timeToStr } from '@/utils/ComputedTime'
import { isMessageMultiSelectEnabled } from '@/utils/MessageSelect'
import { resolveMessageScrollerKey } from '@/utils/messageScrollerKey'
import RenderMessage from '@/components/rightBox/renderMessage/index.vue'
import FileUploadProgress from '@/components/rightBox/FileUploadProgress.vue'

const emit = defineEmits<{ scroll: [Event] }>()

const { t } = useI18n()
const chatStore = useChatStore()
const globalStore = useGlobalStore()
const networkStatus = useNetworkStatus()

const chatMainContext = useChatMain(false, { enableGroupNicknameModal: false })
provide(chatMainInjectionKey, chatMainContext)

const isGroup = computed(() => chatStore.isGroup)
const scrollerItems = computed(() => chatStore.chatMessageList.filter((msg) => Boolean(msg?.message)))

const currentNewMsgCount = computed(() => chatStore.currentNewMsgCount || null)
const newMsgCountLabel = computed(() => {
  if (!currentNewMsgCount.value?.count || currentNewMsgCount.value.count <= 0) return '0'
  return currentNewMsgCount.value.count > 99 ? '99+' : String(currentNewMsgCount.value.count)
})

const networkBanner = computed(() => {
  if (!networkStatus.browserOnline.value) {
    return { text: t('home.chat_main.network_offline') }
  }
  if (networkStatus.isWsConnecting.value) {
    return { text: t('home.chat_main.network_connecting') }
  }
  if (networkStatus.wsOnline.value === false) {
    return { text: t('home.chat_main.network_ws_offline') }
  }
  return null
})

const scrollWrapRef = ref<HTMLElement | null>(null)
const activeReply = ref('')
const hoverId = ref('')
const isLoadingMore = ref(false)
const suppressTopLoadMore = ref(false)
const isAtBottom = ref(true)
const scrollIntent = ref<ScrollIntentEnum>(ScrollIntentEnum.NONE)
const { isAutoScrolling, enableAutoScroll } = useAutoScrollGuard()

const computeMsgHover = computed(() => (item: MessageType) => {
  if (!chatStore.isMsgMultiChoose || !isMessageMultiSelectEnabled(item.message.type)) {
    return false
  }
  if (chatStore.msgMultiChooseMode === 'forward') {
    return false
  }
  return hoverId.value === item.message.id || item.isCheck
})

const shouldShowFloatFooter = computed(() => {
  if (isLoadingMore.value) return false
  return !!(currentNewMsgCount.value?.count && currentNewMsgCount.value.count > 0 && !isAtBottom.value)
})

const scrollToMessageIndex = (index: number) => {
  nextTick(() => {
    const target = scrollWrapRef.value?.querySelector(`[data-message-index="${index}"]`)
    target?.scrollIntoView({ block: 'center' })
  })
}

const scrollToBottom = () => {
  suppressTopLoadMore.value = true
  chatStore.clearNewMsgCount()
  isAtBottom.value = true
  enableAutoScroll(500)
  nextTick(() => {
    const container = scrollWrapRef.value
    if (container) {
      container.scrollTop = container.scrollHeight
    }
  })
  setTimeout(() => {
    suppressTopLoadMore.value = false
  }, 32)
}

const handleVirtualScroll = (event: Event) => {
  emit('scroll', event)
  const container = scrollWrapRef.value
  if (!container) return
  const { scrollTop, scrollHeight, clientHeight } = container
  if (isAutoScrolling.value) {
    isAtBottom.value = true
  } else {
    isAtBottom.value = scrollHeight - scrollTop - clientHeight <= 150
  }
  debouncedScrollOperations(container)
}

const debouncedScrollOperations = useDebounceFn(async (container: HTMLElement) => {
  const { scrollTop, scrollHeight, clientHeight } = container
  if (scrollTop < 60) {
    if (suppressTopLoadMore.value || chatStore.currentMessageOptions?.isLast) return
    await handleLoadMore(container)
  }
  if (scrollHeight - scrollTop - clientHeight <= 20) {
    chatStore.clearNewMsgCount()
  }
}, 16)

const handleLoadMore = async (container: HTMLElement) => {
  if (chatStore.currentMessageOptions?.isLoading || isLoadingMore.value || chatStore.currentMessageOptions?.isLast) {
    return
  }
  isLoadingMore.value = true
  const oldScrollHeight = container.scrollHeight
  await chatStore.loadMore()
  await nextTick()
  container.scrollTop += container.scrollHeight - oldScrollHeight
  isLoadingMore.value = false
}

const jumpToReplyMsg = async (key: string) => {
  const index = chatStore.chatMessageList.findIndex((msg) => msg.message.id === String(key))
  if (index !== -1) {
    scrollToMessageIndex(index)
    activeReply.value = String(key)
    return
  }
  window.$message.info('正在查找消息...')
  isLoadingMore.value = true
  let found = false
  for (let i = 0; i < 5 && !found; i++) {
    await chatStore.loadMore()
    const idx = chatStore.chatMessageList.findIndex((msg) => msg.message.id === key)
    if (idx !== -1) {
      scrollToMessageIndex(idx)
      activeReply.value = key
      found = true
    }
    await new Promise((r) => setTimeout(r, 300))
  }
  isLoadingMore.value = false
  if (!found) {
    window.$message.warning('无法找到原始消息')
  }
}

const onMessageClick = (item: MessageType) => {
  if (chatStore.isMsgMultiChoose && isMessageMultiSelectEnabled(item.message.type)) {
    item.isCheck = !item.isCheck
  }
}

const handleChatAreaClick = () => {
  if (activeReply.value) {
    activeReply.value = ''
  }
}

watch(
  () => globalStore.currentSessionRoomId,
  (newRoomId, oldRoomId) => {
    if (newRoomId && newRoomId !== oldRoomId) {
      suppressTopLoadMore.value = true
      isAtBottom.value = true
      enableAutoScroll(1200)
      scrollIntent.value = ScrollIntentEnum.INITIAL
    }
  },
  { flush: 'post' }
)

watch(scrollIntent, (intent) => {
  if (intent === ScrollIntentEnum.INITIAL || intent === ScrollIntentEnum.NEW_MESSAGE) {
    nextTick(() => scrollToBottom())
  }
  scrollIntent.value = ScrollIntentEnum.NONE
})

watch(
  () => chatStore.chatMessageList.length,
  (len, oldLen) => {
    if (len > oldLen && !isLoadingMore.value && isAtBottom.value) {
      scrollIntent.value = ScrollIntentEnum.NEW_MESSAGE
    }
  }
)

const handleSessionChanged = () => {
  nextTick(() => scrollToBottom())
}

useMitt.on(MittEnum.SESSION_CHANGED, handleSessionChanged)

useMitt.on(MittEnum.CHAT_SCROLL_BOTTOM, () => {
  if (chatStore.chatMessageList.length > 60) {
    chatStore.clearRedundantMessages(globalStore.currentSessionRoomId)
  }
  scrollToBottom()
})

onMounted(() => {
  nextTick(() => scrollToBottom())
})
</script>

<style lang="scss" scoped>
@use '@/styles/scss/render-message';

.mobile-chat-scroller-wrap {
  display: flex;
  flex-direction: column;
}

.mobile-msg-row {
  padding-bottom: 12px;
}
</style>
