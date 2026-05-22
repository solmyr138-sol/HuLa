# HuLa 企业管理后台

**租户级** IM 运营后台（中文界面，对齐 `images/后台*.jpg`）。  
跨租户平台总后台见同级目录 **`HuLa-Platform-Admin/`**（devOperation / `/base/platform/tenant/*`）。

对接 `HuLa-Server`：

- `/im/admin/tenant/info` — 企业信息
- `/im/admin/tenant/policy` — 功能配置
- `/im/admin/tenant/policy/whitelist` — 允许名单
- `/im/admin/user/page` — 注册/注销用户
- `/im/admin/stats/home` — 统计

```bash
cd HuLa-Admin
pnpm install
pnpm dev
```

默认 `VITE_API_BASE=http://127.0.0.1:8080`，需携带 `tenant-id` 与登录 Token。

## 开发

```bash
cd HuLa-Admin
pnpm install
pnpm dev
```

环境变量 `VITE_API_BASE` 指向网关地址（默认 `http://127.0.0.1:8080`）。

## 多域名

在 `def_tenant.admin_domain` 配置管理后台域名；网关需根据 Host 解析 `tenant-id`（见 `TenantDomainWebFilter`）。
