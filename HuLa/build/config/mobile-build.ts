import { isMobilePlatform } from './components'

/** Desktop-only packages: alias to empty stub on mobile builds so they never ship in APK. */
export const MOBILE_STUB_MODULES = [
  'tlbs-map-vue',
  'mermaid',
  'three',
  'stream-monaco',
  'stream-markdown',
  'markstream-vue',
  '@vue-office/docx',
  '@vue-office/excel',
  '@vue-office/pdf',
  '@vue-office/pptx',
  'driver.js'
] as const

export const isMobileBuildTarget = (platform?: string, mode?: string) => {
  return mode === 'android' || isMobilePlatform(platform)
}
