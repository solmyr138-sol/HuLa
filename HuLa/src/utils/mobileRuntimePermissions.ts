import { invoke } from '@tauri-apps/api/core'
import type { PermissionState } from '@tauri-apps/api/core'
import {
  checkPermissions as checkCameraPermissions,
  requestPermissions as requestCameraPermissions
} from '@tauri-apps/plugin-barcode-scanner'
import { isAndroid, isMobile } from '@/utils/PlatformConstants'

const PERMISSIONS_PLUGIN = 'plugin:hula-permissions'
const SESSION_KEY = 'hula-mobile-runtime-permissions-v1'

type RuntimePermissionAlias = 'camera' | 'microphone' | 'media' | 'storageLegacy'

type RuntimePermissionMap = Partial<Record<RuntimePermissionAlias, PermissionState>>

const sleep = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms))

const needsPrompt = (state?: PermissionState) => {
  if (!state) return true
  return state === 'prompt' || state === 'prompt-with-rationale'
}

const isGranted = (state?: PermissionState) => state === 'granted'

async function checkHulaPermissions(): Promise<RuntimePermissionMap> {
  return await invoke<RuntimePermissionMap>(`${PERMISSIONS_PLUGIN}|checkPermissions`)
}

async function requestHulaPermissions(permissions: RuntimePermissionAlias[]): Promise<RuntimePermissionMap> {
  return await invoke<RuntimePermissionMap>(`${PERMISSIONS_PLUGIN}|requestPermissions`, { permissions })
}

async function requestCameraViaScanner() {
  const state = await checkCameraPermissions()
  if (state.camera === 'granted') return true
  if (needsPrompt(state.camera)) {
    const next = await requestCameraPermissions()
    return next.camera === 'granted'
  }
  return false
}

function resolvePermissionOrder(): RuntimePermissionAlias[] {
  const order: RuntimePermissionAlias[] = ['camera', 'microphone', 'media']
  if (isAndroid()) {
    order.push('storageLegacy')
  }
  return order
}

/**
 * 登录后按序向系统申请相机、麦克风、媒体权限（每次只弹一组，避免 Android 批量弹窗卡顿）。
 */
export async function requestEssentialMobilePermissions(options?: { force?: boolean }) {
  if (!isMobile()) return

  if (!options?.force && sessionStorage.getItem(SESSION_KEY) === '1') {
    return
  }

  const order = resolvePermissionOrder()

  try {
    for (const alias of order) {
      let current = await checkHulaPermissions()
      if (isGranted(current[alias])) continue
      if (!needsPrompt(current[alias])) continue

      current = await requestHulaPermissions([alias])
      if (!isGranted(current[alias])) {
        console.warn(`[mobileRuntimePermissions] ${alias} not granted:`, current[alias])
      }
      await sleep(350)
    }
  } catch (error) {
    console.warn('[mobileRuntimePermissions] hula-permissions plugin failed, fallback camera only:', error)
    try {
      await requestCameraViaScanner()
    } catch (cameraError) {
      console.warn('[mobileRuntimePermissions] camera fallback failed:', cameraError)
    }
  }

  sessionStorage.setItem(SESSION_KEY, '1')
}

export async function ensureCameraPermission(): Promise<boolean> {
  if (!isMobile()) return true
  try {
    const map = await checkHulaPermissions()
    if (isGranted(map.camera)) return true
    if (needsPrompt(map.camera)) {
      const next = await requestHulaPermissions(['camera'])
      return isGranted(next.camera)
    }
  } catch {
    return requestCameraViaScanner()
  }
  return false
}

export async function ensureMicrophonePermission(): Promise<boolean> {
  if (!isMobile()) return true
  try {
    const map = await checkHulaPermissions()
    if (isGranted(map.microphone)) return true
    if (needsPrompt(map.microphone)) {
      const next = await requestHulaPermissions(['microphone'])
      return isGranted(next.microphone)
    }
  } catch (error) {
    console.warn('[mobileRuntimePermissions] microphone request failed:', error)
  }
  return false
}

export async function ensureMediaPermission(): Promise<boolean> {
  if (!isMobile()) return true
  try {
    const map = await checkHulaPermissions()
    if (isGranted(map.media) && (isGranted(map.storageLegacy) || !isAndroid())) {
      return true
    }
    const toRequest: RuntimePermissionAlias[] = []
    if (needsPrompt(map.media)) toRequest.push('media')
    if (isAndroid() && needsPrompt(map.storageLegacy)) toRequest.push('storageLegacy')
    if (!toRequest.length) return isGranted(map.media) || isGranted(map.storageLegacy)
    const next = await requestHulaPermissions(toRequest)
    return isGranted(next.media) || isGranted(next.storageLegacy)
  } catch (error) {
    console.warn('[mobileRuntimePermissions] media request failed:', error)
  }
  return false
}
