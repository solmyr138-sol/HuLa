import type { Alias } from 'vite'

import { fileURLToPath, URL } from 'node:url'

import { isMobilePlatform } from './components'



const stubsDir = fileURLToPath(new URL('../stubs', import.meta.url))



/** Desktop-only packages stubbed on mobile builds (three is required by HuLaAssistant on mobile). */

export const MOBILE_STUB_MODULES = [

  'tlbs-map-vue',

  'mermaid',

  'stream-monaco',

  'stream-markdown',

  'markstream-vue',

  '@vue-office/docx',

  '@vue-office/excel',

  '@vue-office/pdf',

  '@vue-office/pptx',

  'driver.js'

] as const



const STUB_FILE_BY_MODULE: Partial<Record<(typeof MOBILE_STUB_MODULES)[number], string>> = {

  'markstream-vue': 'markstream-vue-stub.ts',

  '@vue-office/docx': 'vue-office-stub.ts',

  '@vue-office/excel': 'vue-office-stub.ts',

  '@vue-office/pdf': 'vue-office-stub.ts',

  '@vue-office/pptx': 'vue-office-stub.ts',

  'driver.js': 'driver-stub.ts'

}



export const isMobileBuildTarget = (platform?: string, mode?: string) => {

  return mode === 'android' || isMobilePlatform(platform)

}



/** Match package root and deep imports (e.g. `@vue-office/pdf/lib/v3/...`). */

function aliasForStubModule(moduleId: string, stubFile: string): Alias[] {

  const escaped = moduleId.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')

  const replacement = `${stubsDir}/${stubFile}`

  return [

    { find: new RegExp(`^${escaped}$`), replacement },

    { find: new RegExp(`^${escaped}/.+`), replacement }

  ]

}



export function createMobileBuildAliases(): Alias[] {

  const defaultStub = `${stubsDir}/default-stub.ts`

  const moduleAliases = MOBILE_STUB_MODULES.flatMap((moduleId) => {

    const stubFile = STUB_FILE_BY_MODULE[moduleId] ?? 'default-stub.ts'

    return aliasForStubModule(moduleId, stubFile)

  })



  return [

    { find: '@/router/desktop.routes', replacement: `${stubsDir}/desktop-routes-stub.ts` },

    { find: '@/router/common.routes', replacement: `${stubsDir}/common-routes-stub.ts` },

    ...moduleAliases,

    // CSS sidecars for stubbed packages

    ...aliasForStubModule('markstream-vue/index.css', 'default-stub.ts'),

    ...aliasForStubModule('driver.js/dist/driver.css', 'default-stub.ts')

  ]

}


