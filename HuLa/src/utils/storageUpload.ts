import { invoke } from '@tauri-apps/api/core'
import { BaseDirectory, writeFile } from '@tauri-apps/plugin-fs'
import { UploadSceneEnum } from '@/enums'
import { useUpload } from '@/hooks/useUpload'
import { isMobile } from '@/utils/PlatformConstants'
import { removeTempFile } from '@/utils/TempFileManager'
import { getUploadProvider } from '@/utils/ImRequestUtils'

/**
 * 头像等小文件：已配置 OSS 则直传，否则走服务端 multipart 落盘。
 */
export async function uploadImageWithStorageFallback(
  file: File,
  options?: { scene?: UploadSceneEnum; enableDeduplication?: boolean; bizType?: string }
): Promise<string> {
  const scene = options?.scene ?? UploadSceneEnum.AVATAR
  const bizType = options?.bizType ?? 'avatar'

  const provider = await getUploadProvider()

  // MinIO：一律经服务端 putObject，避免客户端预签名 PUT 403 SignatureDoesNotMatch
  if (provider?.provider === 'minio') {
    return await uploadFileToServer(file, bizType)
  }

  if (provider?.ready && provider.provider === 'qiniu') {
    const { uploadFile } = useUpload()
    const result = await uploadFile(file, {
      scene,
      enableDeduplication: options?.enableDeduplication ?? true
    })
    const downloadUrl = result?.downloadUrl
    if (!downloadUrl) {
      throw new Error('上传失败，未获取到文件地址')
    }
    return downloadUrl
  }

  return await uploadFileToServer(file, bizType)
}

export async function uploadFileToServer(file: File, bizType: string): Promise<string> {
  const baseDir = isMobile() ? BaseDirectory.AppData : BaseDirectory.AppCache
  const baseDirName = isMobile() ? 'AppData' : 'AppCache'
  const tempName = `server_upload_${Date.now()}_${file.name || 'avatar.webp'}`
  const buffer = await file.arrayBuffer()
  await writeFile(tempName, new Uint8Array(buffer), { baseDir })

  try {
    return await invoke<string>('server_file_upload', {
      path: tempName,
      baseDir: baseDirName,
      bizType
    })
  } finally {
    await removeTempFile(tempName, { baseDir })
  }
}
