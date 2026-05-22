<template>
  <DynamicScroller
    ref="scrollerRef"
    :items="items"
    :min-item-size="minItemSize"
    :key-field="keyField"
    class="smart-virtual-list"
    v-bind="$attrs"
    @scroll="(e: Event) => emit('scroll', e)">
    <template #default="slotProps">
      <DynamicScrollerItem
        :item="slotProps.item"
        :active="slotProps.active"
        :data-index="slotProps.index"
        :size-dependencies="sizeDependencies?.(slotProps.item)">
        <slot v-bind="slotProps" />
      </DynamicScrollerItem>
    </template>
  </DynamicScroller>
</template>

<script setup lang="ts">
import 'vue-virtual-scroller/dist/vue-virtual-scroller.css'
import { DynamicScroller, DynamicScrollerItem } from 'vue-virtual-scroller'

withDefaults(
  defineProps<{
    items: Record<string, unknown>[]
    keyField?: string
    minItemSize?: number
    sizeDependencies?: (item: Record<string, unknown>) => unknown[]
  }>(),
  {
    keyField: 'id',
    minItemSize: 72
  }
)

const emit = defineEmits<{ scroll: [Event] }>()

const scrollerRef = ref<InstanceType<typeof DynamicScroller> | null>(null)

defineExpose({
  scrollToItem: (index: number) => {
    const api = scrollerRef.value as { scrollToItem?: (i: number) => void } | null
    api?.scrollToItem?.(index)
  },
  getScroller: () => scrollerRef.value
})
</script>

<style scoped>
.smart-virtual-list {
  height: 100%;
}
</style>
