/**
 * 需要强制分离到独立 chunk 的模块配置
 * 格式: { 模块路径片段: chunk 名称 }
 */
export const manualChunkConfig: Record<string, string> = {
  'src/enums/index.ts': 'enums',
  'src/utils/TauriInvokeHandler.ts': 'tauri-invoke',
  'src/hooks/useLogin.ts': 'login-hook',
  'src/mobile/components/ImagePreview.vue': 'image-preview',
  'src/router/index.ts': 'router',
  'src/router/mobile.routes.ts': 'mobile-routes',
  'src/router/desktop.routes.ts': 'desktop-routes',
  'src/components/rightBox/chatBox/ChatMain.vue': 'desktop-chat-main',
  'src/components/rightBox/renderMessage': 'render-message'
}

/** Desktop-heavy deps: isolate on mobile builds so they are not in the main chunk. */
export const mobileLazyVendorPackages = new Set([
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
])

/**
 * 创建 manualChunks 函数
 * @param dependencies - 项目依赖列表，用于处理 node_modules 的分离
 * @returns manualChunks 函数
 */
export function createManualChunks(dependencies: string[], isMobileBuild = false) {
  return (id: string): string | undefined => {
    // 移除查询参数以进行精确匹配
    const cleanId = id.split('?')[0]

    // 检查是否需要强制分离到独立 chunk
    for (const [modulePath, chunkName] of Object.entries(manualChunkConfig)) {
      if (cleanId.includes(modulePath)) {
        return chunkName
      }
    }

    // 处理 node_modules 的分离
    if (id.includes('node_modules')) {
      // 找到匹配的依赖包名
      const matchedDep = dependencies.find((dep) => id.includes(`node_modules/${dep}`))
      if (matchedDep) {
        const chunkName = matchedDep.replace(/[@/]/g, '-')
        if (isMobileBuild && mobileLazyVendorPackages.has(matchedDep)) {
          return `lazy-${chunkName}`
        }
        return chunkName
      }
      return 'vendor'
    }

    return undefined
  }
}
