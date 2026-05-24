# HuLa 网页版实现方案

本文档基于 HuLa 客户端（Tauri + Vue 3）现有架构，说明纯 H5 / 网页版的能力边界与推荐实现路径。

---

## 一、背景：APP 端能否用 H5 等价实现？

### 1.1 当前 APP 实际是什么

HuLa 移动端不是「浏览器里跑的 SPA」，而是：

- **Vue 3 + Vant** 负责界面（`HuLa/src/mobile/`）
- **Tauri + Rust** 负责：HTTP、WebSocket、SQLite、发消息管道、上传、通知、扫码、录音等

`HuLa/CONTEXT.md` 也写明：这是 **Tauri 壳里的客户端**，不是 browser-only 的 Web App。

移动端路由覆盖的能力大致包括：登录/协议/忘记密码、消息列表、单聊/群聊、群设置/公告/成员、好友与建群、社区动态、个人中心与资料、扫码、二维码登录确认、音视频通话、AI 助手、同步数据页等。

### 1.2 按能力分层（H5 能否等价）

| 类别 | 代表功能 | 纯 H5 |
|------|----------|--------|
| **界面与业务逻辑** | 会话列表、聊天 UI、好友、社区、资料编辑、群管理页面 | ✅ 可复用大部分 Vue 页面与交互 |
| **需重写基础设施** | 登录、所有 `imRequest`、发消息、历史消息分页、会话列表 | ⚠️ 现网全部走 `im_request_command` / `send_msg` 等 **Tauri Command**，H5 要换成 `fetch` + 自管 token，并重写消息收发与本地缓存 |
| **实时** | WebSocket 收消息、重连 | ⚠️ 现为 **Rust `tokio-tungstenite`**；H5 需浏览器 `WebSocket`，并重写 `webSocketRust` / 事件分发 |
| **本地数据** | 离线消息、快速翻历史、多账号库切换 | ⚠️ 现为 **SQLite（可 SQLCipher）**；H5 通常用 IndexedDB，加密与容量、一致性都弱一档 |
| **媒体** | 图片/文件（`van-uploader` + Rust 上传）、语音消息 | ⚠️ 上传走 `upload_file_put` 等；H5 可用预签名 URL + `fetch`，语音需 **MediaRecorder** 替代 `mic-recorder` |
| **扫码** | `MobileQRCode` 用 `barcode-scanner` 插件 | ⚠️ 可用 `getUserMedia` + jsQR 类库，但 iOS/微信内核兼容与体验常不如原生 |
| **通话** | `/mobile/rtcCall` 复用 `CallWindow` / WebRTC | ⚠️ 浏览器 WebRTC 可行，但后台、来电、蓝牙路由等仍弱于 App |
| **系统能力** | 本地通知（`@tauri-apps/plugin-notification`）、角标、后台保活、启动图、iOS 键盘顶起 | ❌ 纯 H5 **无法等价**；PWA + Web Push 在 iOS 上仍有限制 |
| **安全与设备** | 设备指纹登录、`switch_user_database`、token 存 Rust 侧 | ⚠️ 占位 H5 只能 localStorage/IndexedDB，安全模型不同 |

### 1.3 为什么不能直接部署现有 mobile 路由

共享层（不仅是 `src/mobile/`）大量绑定 Tauri，例如：

- 路由守卫：`invoke(GET_USER_TOKENS)` 判登录
- 登录后：`rustWebSocketClient.initConnect()`、`invoke(SAVE_USER_INFO)`、`switch_user_database`
- 发消息：`sendMessageWithChannel` → Rust `send_msg` + SQLite
- 所有 IM API：`imRequest` → `im_request_command`（Rust 代发 HTTP）

**结论**：界面是 Web 技术，运行时是原生壳。去掉壳不是「换个部署方式」，而是**再做一个 H5 运行时适配层**。

| 问题 | 答案 |
|------|------|
| 现有 APP **界面上的功能**，H5 **能不能做**？ | **大部分能**，但依赖 **重写/替换** 与 Rust 绑定的通信、存储、推送、录音、扫码等 |
| **能不能 100% 等价**？ | **不能**。后台通知、持久 WebSocket、加密本地库、启动图/键盘等原生体验，纯 H5 做不到与当前 Tauri 方案同级 |
| 工作量量级 | 相当于做一个 **「H5 版客户端运行时」** + 抽离 `invoke` 依赖 |

---

## 二、网页版怎么实现

做「网页版」不是单独部署 `mobile` 路由就行，而是要加一层 **Web 运行时**，把现在走 Rust/Tauri 的能力换成浏览器能力。

### 2.1 现状（决定你怎么做）

| 层 | 现在 | 网页版需要 |
|----|------|------------|
| UI | `src/mobile/`（Vant）+ 大量 `src/stores`、`hooks` | 可复用，但要避开 `invoke` |
| HTTP | 全部 `imRequest` → `im_request_command`（Rust 代发） | 浏览器 `fetch` + Token |
| 实时 | `webSocketRust`（Rust） | 浏览器 `WebSocket`（`webSocketAdapter.ts` 注释里提过可切换，但 JS 实现已不存在） |
| 本地库 | SQLite + `send_msg` / `page_msg` | IndexedDB 或「以服务端为准、内存缓存」 |
| 构建 | `__HULA_MOBILE_BUILD__` 只给 Tauri 移动端 | 新增 `__HULA_WEB_BUILD__` 或 `VITE_PLATFORM=web` |

服务端不用重写：`base_url`、`ws_url` 与 App 相同（见 `HuLa/src-tauri/configuration/local.yaml` 的 `/api`、`/api/ws/ws`），重点是 **网关 CORS** 和 **对象存储跨域**。

### 2.2 三条实现路线（由易到难）

#### 方案 A：同仓库「Web 构建目标」（最贴合 HuLa，推荐）

在 `HuLa/` 里增加 **平台抽象 + Web 专用构建**，与桌面/移动并列。

```
Vue UI (mobile / desktop)
        ↓
platform 层（新增：tauri.ts / web.ts）
        ↓
Tauri + SQLite + Rust WS  |  fetch + IndexedDB + Browser WS
```

**构建**

- Vite：`pnpm build:web` → `VITE_PLATFORM=web`，`define: { __HULA_WEB_BUILD__: true }`
- 路由：优先复用 **`mobile.routes.ts`**（已是触屏/窄屏），大屏可加 CSS 居中 max-width
- 入口：`main.ts` 里 Web 不加载 Tauri 专用逻辑（快捷键、多窗口等）

**必须实现的 platform 接口（最小集）**

1. **Auth**：`getTokens` / `setTokens` / `removeTokens` → `localStorage` 或 `sessionStorage`（企业场景可再谈 httpOnly Cookie，需网关配合）
2. **HTTP**：`imRequest` → 对 `HuLa-Server` 直接 `fetch`（复用现有 URL 枚举与类型）
3. **WebSocket**：新建 `webSocketWeb.ts`，协议与 Rust 侧对齐（连 `/api/ws/ws`，带 token/clientId）
4. **消息**：
   - **轻量**：不发本地库，列表/历史全走 REST，内存里保留当前会话
   - **完整**：IndexedDB 存会话与消息，对齐 `page_msg` 体验
5. **上传**：走服务端预签名 URL + 浏览器 `PUT`（确认 MinIO/OSS **CORS** 允许 Web 域名）
6. **守卫**：`router/index.ts` 里 Web 分支用 storage 判登录，不调 `invoke(GET_USER_TOKENS)`

**改造方式（减少大爆炸）**

- 新建 `src/platform/index.ts`，导出 `isWeb()` / `getPlatform()`
- 把 `ImRequestUtils.imRequest`、`MessageSender`、`useLogin` 里的 `invoke` 改为调 platform
- `grep invoke(` 按模块替换；移动端专用（扫码、启动图、键盘）用 `if (!isWeb())` 包一层

**分期交付（建议）**

| 阶段 | 功能 | 价值 |
|------|------|------|
| M1 | 登录/登出、会话列表、进房看历史、收实时文字 | 可演示的网页 IM |
| M2 | 发文字/图片/文件、群聊基础、好友列表 | 日常可用 |
| M3 | 语音消息、音视频通话、社区动态、AI 助手 | 接近 App |
| M4 | 离线缓存、Web Push（可选） | 体验补强 |

#### 方案 B：独立 `HuLa-Web` 子项目（边界清晰、前期更快）

新建 `HuLa-Web/`（或 monorepo 包 `@hula/web`）：

- **共享**：`services/types`、枚举、部分 utils（可从 `HuLa` 抽 `packages/shared`）
- **重写**：stores 里所有 Tauri 依赖、WebSocket、消息管道
- **UI**：复制/引用 mobile 页面，或直接用 Vant + 同一设计稿

适合：网页版需求与 App 长期分叉、或团队分工（一人专 Web）。

缺点：双份业务逻辑，修 bug 要改两处，除非 shared 抽得好。

#### 方案 C：混合壳（Capacitor / 自定义 WebView）

网页技术写 UI，用原生壳补通知、文件、扫码。

- 若目标是 **「只要浏览器能打开」** → 不必上 Capacitor
- 若目标是 **「网页代码 + 仍要上架商店 + 推送」** → 可考虑，但已不是纯网页版

### 2.3 服务端 / 运维要一并做的

1. **CORS**：`luohuo-gateway` 允许 Web 源（如 `https://im.example.com`），`Authorization`、自定义头放行
2. **WebSocket**：同源或 WSS 代理；token 传递方式与 App 一致（query / 首帧鉴权看现有 ws 实现）
3. **对象存储**：预签名上传/下载域名对浏览器开放 CORS
4. **部署**：静态资源 Nginx/CDN；`history` 模式需 fallback 到 `index.html`
5. **安全**：Web 上 token 在 localStorage 有 XSS 风险，需 CSP、依赖审计；高安全场景用 BFF + Cookie

### 2.4 UI 选型建议

| 选项 | 说明 |
|------|------|
| **复用 mobile + Vant** | 工作量最小，手机浏览器、窄屏桌面都合适 |
| **复用 desktop + Naive** | 适合「仅 PC 浏览器」的管理/办公场景 |
| **响应式两套** | 成本高，一般 M2 以后再做 |

`src/mobile/views/message/index.vue` 等 mobile 页面，在方案 A 里 **可以保留**，只要底层 store 不再直接 `invoke`。

### 2.5 能力对齐预期

网页版 = **方案 A/B + 平台层**，不是把现有 APK 里的 WebView 直接当网站发布。

- **能较快对齐的**：登录、消息、好友、群聊、资料、社区、前台通话（WebRTC）
- **要降级或后置的**：杀进程后推送、强离线、原生扫码/录音体验、SQLCipher 级本地加密

### 2.6 推荐选择

| 目标 | 建议 |
|------|------|
| 继续维护一套 Vue、和 App 功能逐步对齐 | **方案 A**（`__HULA_WEB_BUILD__` + `src/platform`） |
| 先 2～4 周出一个只读/轻聊 demo | A 的 **M1**，HTTP + 浏览器 WS，不做 IndexedDB |
| 网页与 App 产品路线完全分离 | **方案 B** |

---

## 三、相关代码位置（便于实施时检索）

| 用途 | 路径 |
|------|------|
| 移动端路由 | `HuLa/src/router/mobile.routes.ts` |
| 路由守卫（Tauri token） | `HuLa/src/router/index.ts` |
| IM HTTP 封装 | `HuLa/src/utils/ImRequestUtils.ts` |
| WebSocket（Rust） | `HuLa/src/services/webSocketRust.ts` |
| WebSocket 适配器（待扩展 Web 实现） | `HuLa/src/services/webSocketAdapter.ts` |
| 发消息管道 | `HuLa/src/utils/MessageSender.ts` |
| 登录初始化 | `HuLa/src/hooks/useLogin.ts` |
| 移动端 Tauri 权限 | `HuLa/src-tauri/capabilities/mobile.json` |
| 开发端点配置 | `HuLa/src-tauri/configuration/local.yaml` |
| 构建标志 | `HuLa/vite.config.ts`（`__HULA_MOBILE_BUILD__`） |

---

## 四、待决事项（立项前建议对齐）

1. **目标用户**：手机浏览器为主，还是 PC 浏览器为主？
2. **功能边界**：M1 是否足够先上线，还是必须 M2/M3？
3. **离线策略**：纯在线 vs IndexedDB 本地缓存？
4. **部署域名**：与 API 是否同域（影响 CORS 与 Cookie 方案）？
5. **是否做 PWA**：影响推送与「添加到主屏幕」体验。

---

*文档生成自架构讨论，实施前请对照 `HuLa/CONTEXT.md`、`CONTEXT-MAP.md` 与 `docs/adr/` 最新决策。*
