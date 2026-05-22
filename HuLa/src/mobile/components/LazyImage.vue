<template>
  <img
    v-if="shouldLoad"
    :src="src"
    :alt="alt"
    :class="imgClass"
    :loading="nativeLazy ? 'lazy' : undefined"
    decoding="async"
    @load="onLoad"
    @error="onError" />
  <div v-else ref="observeTarget" :class="['lazy-image-placeholder', imgClass]" :style="placeholderStyle" />
</template>

<script setup lang="ts">
const props = withDefaults(
  defineProps<{
    src: string
    alt?: string
    imgClass?: string
    width?: number
    height?: number
    rootMargin?: string
    nativeLazy?: boolean
  }>(),
  {
    alt: '',
    imgClass: '',
    rootMargin: '120px',
    nativeLazy: true
  }
)

const emit = defineEmits<{ load: []; error: [] }>()

const shouldLoad = ref(!('IntersectionObserver' in window))
const placeholderStyle = computed(() => ({
  width: props.width ? `${props.width}px` : '100%',
  height: props.height ? `${props.height}px` : 'auto',
  minHeight: props.height ? undefined : '48px',
  background: 'var(--bg-msg-hover, #f0f0f0)'
}))

let observer: IntersectionObserver | null = null
const observeTarget = useTemplateRef<HTMLElement>('observeTarget')

onMounted(() => {
  if (shouldLoad.value || !props.src) return
  const el = observeTarget.value
  if (!el) {
    shouldLoad.value = true
    return
  }
  observer = new IntersectionObserver(
    (entries) => {
      if (entries.some((e) => e.isIntersecting)) {
        shouldLoad.value = true
        observer?.disconnect()
      }
    },
    { rootMargin: props.rootMargin }
  )
  observer.observe(el)
})

onBeforeUnmount(() => {
  observer?.disconnect()
})

watch(
  () => props.src,
  () => {
    if (!props.src) {
      shouldLoad.value = false
    }
  }
)

const onLoad = () => emit('load')
const onError = () => emit('error')
</script>

<style scoped>
.lazy-image-placeholder {
  display: block;
  border-radius: 4px;
}
</style>
