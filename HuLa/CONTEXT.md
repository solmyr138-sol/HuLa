# HuLa Client

Cross-platform IM **client**: Vue 3 UI inside a Tauri v2 shell, with Rust for native I/O, local persistence, and WebSocket transport to HuLa-Server.

## Language

**Client**:
The Tauri + Vue application in `HuLa/`.
_Avoid_: Frontend-only, Electron app (unless `HuLa-Electron/` is explicitly in scope)

**Shell**:
The Tauri runtime (`src-tauri/`) hosting the webview and exposing commands.
_Avoid_: Backend (that term means HuLa-Server in this monorepo)

**Command**:
A `#[tauri::command]` invoked from TypeScript via `invoke()`; the boundary between UI and Rust.
_Avoid_: API endpoint (server HTTP), handler (too generic)

**Local store**:
SQLite via SeaORM in the shell, optionally encrypted with SQLCipher.
_Avoid_: Server database, cache (when meaning MySQL/Redis on server)

**Session**:
Authenticated user context on the client (tokens, profile); must stay consistent with server auth.
_Avoid_: Chat session as a synonym for **conversation**

**Conversation**:
A thread of messages between participants (DM or group), identified by server-side ids.
_Avoid_: Room (prefer only if code already uses `room`), thread (unless UI copy)

**Message**:
A single IM payload (text, media, system notice) with server-assigned identity and ordering.
_Avoid_: Event (server/MQ term), packet

**Realtime channel**:
WebSocket connection from shell to server (`ws_url` in config), not the HTTP REST surface.
_Avoid_: Socket.io, polling channel

**Remote API**:
HTTP calls to HuLa-Server (`base_url` in `configuration/local.yaml`).
_Avoid_: Tauri command, local DB query

**Desktop / Mobile**:
Platform targets sharing one codebase; UI may branch (Naive UI vs Vant).
_Avoid_: Web app (this is not a browser-only SPA)

## Example dialogue

**Dev:** User says messages disappear after restart — is that local store or server history?

**Expert:** If they never synced, check **local store** first. If they synced but reload from another device fails, trace **remote API** and the server **message** pipeline. The **realtime channel** only delivers live traffic; history comes from REST.
