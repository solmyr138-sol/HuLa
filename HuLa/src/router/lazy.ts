import type { Component } from 'vue'

type ViewModule = { default: Component }

/** Route-level code splitting; global styles load in main.ts before mount. */
export const lazyView = (loader: () => Promise<ViewModule>) => {
  return () => loader().then((m) => m.default)
}
