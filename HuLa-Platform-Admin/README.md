# HuLa-Platform-Admin — 平台总后台

对应服务端 **devOperation** 应用（跨租户），与 **HuLa-Admin**（单租户 IM 运营后台）分离。

| 项目 | 范围 | API 前缀 |
|------|------|----------|
| HuLa-Admin | 租户内：用户/群/策略 | `/im/admin/*` |
| HuLa-Platform-Admin | 全平台：企业 CRUD、邀请码 | `/base/platform/tenant/*` |

## 功能

- 平台概览（企业总数）
- 企业列表、搜索
- 创建企业（自动生成 `invite_code`）
- 重置企业邀请码

## 开发

```bash
cd HuLa-Platform-Admin
pnpm install
pnpm dev
```

默认端口 **5174**，网关 `VITE_API_BASE=http://127.0.0.1:8080`。

## 注册流程（客户端）

1. 第一步：企业邀请码（`invite_code`）
2. 第二步：手机号 + 密码 → `POST /oauth/anyTenant/registerByEnterpriseMobile`
