<!-- Mobile chat uses MobileChatMessageList (virtual scroll). This wrapper keeps legacy imports working. -->
<template>
  <MobileChatMessageList @scroll="(e) => emit('scroll', e)" />
</template>

<script setup lang="ts">
import MobileChatMessageList from '#/components/chat-room/MobileChatMessageList.vue'

const emit = defineEmits<{ scroll: [Event] }>()
</script>
