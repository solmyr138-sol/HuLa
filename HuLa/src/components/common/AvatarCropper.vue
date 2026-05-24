<template>
  <n-modal
    :show="props.show"
    @update:show="$emit('update:show', $event)"
    :mask-closable="false"
    :class="mobile ? 'avatar-cropper-modal--mobile' : 'rounded-8px'"
    transform-origin="center">
    <div
      class="bg-[--bg-edit] box-border flex flex-col"
      :class="mobile ? 'avatar-cropper-shell--mobile' : 'w-560px h-480px items-center justify-between'">
      <!-- 标题栏 -->
      <n-flex :size="6" vertical class="w-full shrink-0">
        <div
          v-if="!mobile && isMac()"
          @click="closeWindow"
          class="mac-close size-13px shadow-inner bg-#ed6a5eff rounded-50% mt-6px select-none absolute left-6px">
          <svg class="hidden size-7px color-#000 select-none absolute top-3px left-3px">
            <use href="#close"></use>
          </svg>
        </div>

        <n-flex class="text-(14px [--text-color]) select-none pt-6px" justify="center">
          {{ t('components.avatarCropper.title') }}
        </n-flex>

        <svg
          v-if="!mobile && isWindows()"
          class="size-14px cursor-pointer pt-6px select-none absolute right-6px"
          @click="closeWindow">
          <use href="#close"></use>
        </svg>
        <span class="h-1px w-full bg-[--line-color]"></span>
      </n-flex>

      <!-- 主体内容 -->
      <div
        class="flex flex-col items-center w-full min-h-0"
        :class="mobile ? 'flex-1 overflow-y-auto px-12px py-8px gap-12px' : ''">
        <n-flex v-if="!mobile" align="center">
          <div class="w-320px h-320px p-10px mr-20px">
            <vue-cropper
              ref="cropperRef"
              :img="localImageUrl"
              :outputSize="0.4"
              :autoCrop="true"
              :fixedBox="true"
              :fixed="true"
              :centerBox="true"
              :autoCropWidth="cropBoxSize"
              :autoCropHeight="cropBoxSize"
              :fixedNumber="[1, 1]"
              @realTime="handleRealTime" />
          </div>

          <n-flex vertical class="px-20px">
            <div class="mb-20px">
              <div class="text-14px text-[--text-color] mb-8px">
                {{ t('components.avatarCropper.preview.round') }}
              </div>
              <div class="preview-wrapper">
                <div class="rounded-full preview-content" :style="previewStyle">
                  <img :src="previewUrl?.url" :style="previewUrl?.img" />
                </div>
              </div>
            </div>

            <div>
              <div class="text-14px text-[--text-color] mb-8px w-120px">
                {{ t('components.avatarCropper.preview.square') }}
              </div>
              <div class="preview-wrapper">
                <div class="rounded-36px preview-content" :style="previewStyle">
                  <img :src="previewUrl?.url" :style="previewUrl?.img" />
                </div>
              </div>
            </div>
          </n-flex>
        </n-flex>

        <template v-else>
          <div class="w-full flex justify-center">
            <div class="avatar-cropper-area--mobile" :style="{ width: `${cropBoxSize}px`, height: `${cropBoxSize}px` }">
              <vue-cropper
                ref="cropperRef"
                :img="localImageUrl"
                :outputSize="0.4"
                :autoCrop="true"
                :fixedBox="true"
                :fixed="true"
                :centerBox="true"
                :autoCropWidth="cropBoxSize"
                :autoCropHeight="cropBoxSize"
                :fixedNumber="[1, 1]"
                @realTime="handleRealTime" />
            </div>
          </div>

          <div class="w-full">
            <div class="text-14px text-[--text-color] mb-8px">
              {{ t('components.avatarCropper.preview.round') }}
            </div>
            <div class="preview-wrapper preview-wrapper--mobile">
              <div class="rounded-full preview-content" :style="previewStyle">
                <img :src="previewUrl?.url" :style="previewUrl?.img" />
              </div>
            </div>
          </div>
        </template>
      </div>

      <n-flex
        class="shrink-0 w-full"
        :class="mobile ? 'avatar-cropper-footer--mobile' : 'p-12px'"
        align="center"
        justify="center"
        :size="12">
        <n-button
          :class="mobile ? 'flex-1' : ''"
          quaternary
          @click="closeWindow"
          :disabled="loading">
          {{ t('components.common.cancel') }}
        </n-button>
        <n-button
          :class="mobile ? 'flex-1' : ''"
          secondary
          type="primary"
          @click="handleCrop"
          :loading="loading">
          {{ loadingText }}
        </n-button>
      </n-flex>
    </div>
  </n-modal>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { isMac, isMobile, isWindows } from '@/utils/PlatformConstants'
import 'vue-cropper/dist/index.css'
import { VueCropper } from 'vue-cropper'

const { t } = useI18n()
const mobile = isMobile()
const localImageUrl = ref('')
const cropperRef = ref()
const loading = ref(false)
const loadingText = computed(() =>
  loading.value ? t('components.avatarCropper.uploading') : t('components.common.confirm')
)
const previewUrl = ref<{
  url: string
  img: Record<string, string>
  w: number
  h: number
}>()

const cropBoxSize = ref(mobile ? calcMobileCropSize() : 320)

const previewStyle = computed(() => ({
  width: `${previewUrl.value?.w ?? 0}px`,
  height: `${previewUrl.value?.h ?? 0}px`,
  overflow: 'hidden' as const,
  transform: 'scale(0.4)',
  position: 'absolute' as const,
  top: '0',
  left: '0',
  transformOrigin: '0 0'
}))

function calcMobileCropSize() {
  if (typeof window === 'undefined') return 280
  const max = Math.min(window.innerWidth, window.innerHeight) - 48
  return Math.max(200, Math.min(320, Math.floor(max)))
}

const emit = defineEmits<{
  'update:show': [value: boolean]
  crop: [data: Blob]
}>()

const props = defineProps<{
  show: boolean
  imageUrl: string
}>()

watch(
  () => props.imageUrl,
  (newVal) => {
    localImageUrl.value = newVal
  },
  { immediate: true }
)

watch(
  () => props.show,
  (visible) => {
    if (visible && mobile) {
      cropBoxSize.value = calcMobileCropSize()
    }
  }
)

const handleRealTime = (data: { url: string; img: Record<string, string>; w: number; h: number }) => {
  previewUrl.value = data
}

const handleCrop = () => {
  loading.value = true
  cropperRef.value?.getCropBlob((blob: Blob) => {
    emit('crop', blob)
  })
}

const closeWindow = () => {
  if (!loading.value) {
    emit('update:show', false)
  }
}

const finishLoading = () => {
  loading.value = false
}

export interface AvatarCropperInstance {
  finishLoading: () => void
}
defineExpose<AvatarCropperInstance>({
  finishLoading
})

onUnmounted(() => {
  previewUrl.value = {
    url: '',
    img: {},
    w: 0,
    h: 0
  }
})
</script>

<style scoped>
.mac-close:hover svg {
  display: block;
}

:deep(.cropper-view-box) {
  border-radius: 50%;
  outline: none;
  outline-color: transparent;
}

:deep(.cropper-face) {
  background-color: transparent;
  border-radius: 50%;
}

:deep(.cropper-dashed) {
  display: none;
}

img {
  transition: opacity 0.2s ease-in-out;
}

.preview-wrapper {
  position: relative;
  width: calc(320px * 0.4);
  height: calc(320px * 0.4);
  overflow: hidden;
}

.preview-wrapper--mobile {
  width: calc(200px * 0.4);
  height: calc(200px * 0.4);
}

.preview-content {
  transform-origin: left top;
}

.avatar-cropper-area--mobile {
  max-width: 100%;
}

.avatar-cropper-shell--mobile {
  width: 100vw;
  height: 100vh;
  height: 100dvh;
  max-height: 100dvh;
  overflow: hidden;
}

.avatar-cropper-footer--mobile {
  padding: 12px 16px;
  padding-bottom: calc(12px + env(safe-area-inset-bottom, 0px));
  border-top: 1px solid var(--line-color, #eee);
  background: var(--bg-edit, #fff);
}
</style>

<style>
.avatar-cropper-modal--mobile .n-modal {
  width: 100vw !important;
  max-width: 100vw !important;
  margin: 0 !important;
  padding: 0 !important;
}

.avatar-cropper-modal--mobile .n-modal-body-wrapper {
  width: 100vw !important;
  max-width: 100vw !important;
}

.avatar-cropper-modal--mobile .n-modal-body {
  padding: 0 !important;
  width: 100vw !important;
  max-width: 100vw !important;
}
</style>
