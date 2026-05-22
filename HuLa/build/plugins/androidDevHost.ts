import type { Plugin } from 'vite'

const TAURI_DEV_ORIGIN = 'http://tauri.localhost'
const ABSOLUTE_DEV_URL = /https?:\/\/(?:192\.168\.\d+\.\d+|10\.0\.2\.2|127\.0\.0\.1):5210/g

function rewriteDevUrls(code: string): string {
  return ABSOLUTE_DEV_URL.test(code) ? code.replace(ABSOLUTE_DEV_URL, TAURI_DEV_ORIGIN) : code
}

/** Tauri Android WebView runs at tauri.localhost — rewrite dev URLs in responses and modules (incl. Workers). */
export function androidDevHostPlugin(enabled: boolean): Plugin {
  if (!enabled) {
    return { name: 'hula-android-dev-host-disabled' }
  }

  return {
    name: 'hula-android-dev-host',
    apply: 'serve',
    enforce: 'post',
    transform(code) {
      return rewriteDevUrls(code)
    },
    configureServer(server) {
      server.middlewares.use((req, res, next) => {
        const accept = req.headers.accept ?? ''
        if (!accept.includes('text/html') && !accept.includes('javascript') && !accept.includes('json')) {
          next()
          return
        }

        const end = res.end.bind(res)
        const chunks: Buffer[] = []

        res.write = ((chunk: unknown, ...args: unknown[]) => {
          if (chunk) {
            chunks.push(Buffer.isBuffer(chunk) ? chunk : Buffer.from(String(chunk)))
          }
          return true
        }) as typeof res.write

        res.end = ((chunk: unknown, ...args: unknown[]) => {
          if (chunk) {
            chunks.push(Buffer.isBuffer(chunk) ? chunk : Buffer.from(String(chunk)))
          }
          if (chunks.length === 0) {
            return end(chunk, ...args)
          }
          const body = rewriteDevUrls(Buffer.concat(chunks).toString('utf8'))
          if (body !== Buffer.concat(chunks).toString('utf8')) {
            const buf = Buffer.from(body)
            if (res.getHeader('Content-Length')) {
              res.setHeader('Content-Length', buf.length)
            }
            return end(buf, ...args)
          }
          return end(chunk, ...args)
        }) as typeof res.end

        next()
      })
    }
  }
}
