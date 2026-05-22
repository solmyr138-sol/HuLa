# Context Map

HuLa is a monorepo: the **client** talks to the **server** over HTTP and WebSocket. Pick the context that matches the files you are editing.

## Contexts

- [Client](./HuLa/CONTEXT.md) — Tauri v2 shell, Vue 3 UI, Rust commands, local SQLite (SQLCipher), WebSocket to backend
- [Server](./HuLa-Server/CONTEXT.md) — Spring Cloud microservices (`luohuo-*`), Netty/WebFlux IM, MySQL, Redis, RocketMQ, Nacos

## Relationships

- **Client → Server**: REST under `/api` and WebSocket at `/api/ws/ws` (see `HuLa/src-tauri/configuration/local.yaml` for dev endpoints)
- **Server internal**: `luohuo-gateway` routes to `luohuo-im`, `luohuo-ws`, `luohuo-system`, `luohuo-ai`, etc.
- **Shared concern**: A **message** is authored on the server and displayed on the client; the client may cache conversation state locally but the server is the source of truth for delivery and history in cloud mode

## Out of scope for default CONTEXT

- `HuLa-Electron/` — alternate Electron shell (only if user targets it)
- `HuLa-MCP/` — MCP tooling (only if user targets it)
