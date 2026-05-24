import { UploadSceneEnum } from '@/enums'
import { uploadImageWithStorageFallback } from '@/utils/storageUpload'
import { ensureMediaPermission } from '@/utils/mobileRuntimePermissions'
import { isMobile } from '@/utils/PlatformConstants'

export interface AvatarUploadOptions {
  onSuccess?: (downloadUrl: string) => void | Promise<void>
  scene?: UploadSceneEnum
  sizeLimit?: number
}

export const useAvatarUpload = (options: AvatarUploadOptions = {}) => {
  const { onSuccess, scene = UploadSceneEnum.AVATAR, sizeLimit = 150 } = options

  const fileInput = ref<HTMLInputElement>()
  const localImageUrl = ref('')
  const showCropper = ref(false)
  const cropperRef = ref()

  const openFileSelector = () => openAvatarCropper()
  const openAvatarCropper = () => fileInput.value?.click()

  const previewImageFile = (file: File) => {
    const img = new Image()
    const url = URL.createObjectURL(file)
    img.onload = () => {
      localImageUrl.value = url
      nextTick(() => {
        showCropper.value = true
      })
    }
    img.onerror = () => {
      window.$message.error('图片加载失败')
      URL.revokeObjectURL(url)
    }
    img.src = url
  }

  const handleFileChange = async (e: Event) => {
    if (isMobile()) {
      const granted = await ensureMediaPermission()
      if (!granted) {
        window.$message.warning('需要相册/媒体访问权限才能选择图片')
        if (fileInput.value) fileInput.value.value = ''
        return
      }
    }
    const file = (e.target as HTMLInputElement).files?.[0]
    if (file) previewImageFile(file)
    if (fileInput.value) fileInput.value.value = ''
  }

  const uploadAvatarFile = async (file: File): Promise<string> => {
    return await uploadImageWithStorageFallback(file, {
      scene,
      enableDeduplication: true,
      bizType: 'avatar'
    })
  }

  const handleCrop = async (cropBlob: Blob) => {
    try {
      const file = new File([cropBlob], `avatar_${Date.now()}.webp`, { type: 'image/webp' })

      if (file.size > sizeLimit * 1024) {
        window.$message.error(`图片大小不能超过${sizeLimit}KB`)
        cropperRef.value?.finishLoading()
        return
      }

      const downloadUrl = await uploadAvatarFile(file)

      if (onSuccess) {
        await onSuccess(downloadUrl)
      }

      if (localImageUrl.value) URL.revokeObjectURL(localImageUrl.value)
      localImageUrl.value = ''
      if (fileInput.value) fileInput.value.value = ''

      cropperRef.value?.finishLoading()
      showCropper.value = false
    } catch (error) {
      console.error('[useAvatarUpload]', error)
      window.$message.error(error instanceof Error ? error.message : '上传头像失败')
      cropperRef.value?.finishLoading()
    }
  }

  return {
    fileInput,
    localImageUrl,
    showCropper,
    cropperRef,
    openFileSelector,
    handleFileChange,
    handleCrop,
    openAvatarCropper
  }
}
