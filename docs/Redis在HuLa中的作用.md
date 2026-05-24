# Redis 在 HuLa 中的作用

本文说明 **HuLa-Server** 中 Redis 的职责、典型 Key 与代码位置。Redis 与 MySQL、RocketMQ、Nacos 等一起在 `HuLa-Server/docs/install/docker/docker-compose.yml` 中部署；**客户端不直连 Redis**。

> 配置变更后如何避免读到旧缓存，见本文 [第十节](#十配置变更后如何保证-redis-读到最新值)。

---

## 一、基础设施与统一能力

| 作用 | 说明 |
|------|------|
| **统一配置** | 各微服务通过 Nacos 拉取 `redis.yml`（`bootstrap.yml` 的 `shared-configs`）。 |
| **缓存抽象** | `luohuo-cache-starter` 提供 `CacheOps` / `CachePlusOps`，可在 **Redis** 与 **Caffeine** 间切换（`luohuo.cache.type`）。 |
| **数据结构** | String、Hash、Set、ZSet、批量 mGet/mSet、ZSet 分数管道查询等。 |
| **限流脚本** | `luohuo-cache-starter/resources/limit.lua`（`INCR` + 短 TTL，可按接口接入）。 |

---

## 二、认证与 OAuth

| 作用 | 说明 | 典型组件 |
|------|------|----------|
| **Sa-Token 会话** | Token、登录态存 Redis，多网关实例共享。 | `sa-token-redis-jackson` |
| **验证码** | 图形/邮箱/短信验证码短期存储。 | `CaptchaCacheKeyBuilder` |
| **扫码登录** | 二维码状态，约 30 秒过期。 | `QrCacheKeyBuilder` |

---

## 三、WebSocket 路由与在线状态（核心）

### 3.1 设备 → WS 节点（`luohuo-router`）

| 作用 | 结构 | 说明 |
|------|------|------|
| 设备-节点映射 | Hash：`uid:clientId` → `nodeId` | `RouterCacheKeyBuilder`、`NacosRouterService` |
| 节点设备集合 | Set | `NodeDevices`，节点下线清理 |

与 **Nacos** 配合：Nacos 管 WS 实例存活，Redis 管用户设备落在哪台实例。

### 3.2 Presence（`luohuo-ws` + `luohuo-common`）

| Key 语义 | 结构 |
|----------|------|
| 全局在线设备 | ZSet：`uid:clientId` |
| 全局在线用户 | ZSet：`uid` |
| 用户所在群 | Set `roomId` |
| 群成员全集 | Set |
| 群内在线成员 | Set |
| 用户在线群映射 | Set |

`SessionManager.syncOnline()` 维护上下线；`OnlineService` 批量查询是否在线。多数 Presence Key TTL 约 **30 天**。

---

## 四、好友关系

| 作用 | 结构 |
|------|------|
| 用户好友列表 | Set |
| 反向好友（用于上线推送） | Set |
| 好友关系状态 | String，7 天 TTL |

`ApplyServiceImpl` 同意好友后写入 Set。

---

## 五、IM 热点数据缓存（`RedisKey` + `*Cache`）

| 缓存 | Key 模式 |
|------|----------|
| 消息 | `luohuo:msg:{messageId}` |
| 房间 / 群 / 单聊扩展 / 公告 | `roomInfo`、`group_info`、`room_friend`、`announcements` |
| 用户 / DefUser / 汇总 / 道具 | `userInfo`、`defUserInfo`、`userSummary`、`userItem` |
| 热门房间 | ZSet `hotRoom` |

朋友圈：`userFeed`、`feedMedia`、`feedTarget`；`FeedLikeCacheKeyBuilder`、`FeedCommentCacheKeyBuilder`。

---

## 六、消息推送（与 RocketMQ 配合）

| 作用 | 说明 |
|------|------|
| **在途消息 Passage** | 用户维度 Set：已推送未 ACK 的消息 ID；ACK 后移除；重试消费时校验。 |

持久化在 **MySQL**；Redis 不替代落库。

---

## 七、群聊与策略

| 机制 | 场景 |
|------|------|
| `@RedissonLock` | 加群、退群、踢人、管理员、好友申请等 |
| `SET NX EX` | `PolicyGuardServiceImpl` 群发言间隔 |
| Set 维护 | 进退群时同步群成员 / 在线集合 |

---

## 八、音视频房间（`luohuo-ws`）

`RoomMetadataService`、`VideoChatService`：`RoomMetadataCacheKeyBuilder`、`CloseRoomCacheKeyBuilder`、`UserRoomsCacheKeyBuilder`、`VideoRoomsCacheKeyBuilder`。

---

## 九、平台 / 租户 / 系统管理

`SuperCacheManagerImpl` + 各类 `*CacheKeyBuilder`：用户登录查找、员工/组织/角色/字典、应用 API 资源、**系统参数** `ConfigCacheKeyBuilder` 等。写库后 `cacheOps.del(...)` 淘汰缓存。

**AI**：`RedisVectorStore` 存 Embedding 向量（RAG）。

---

## 十、配置变更后如何保证 Redis 读到最新值

HuLa 里「配置」分多层，刷新方式不同。

### 10.1 数据库系统参数（`sys_config` 表）

**读取路径**：`SysSysConfigServiceImpl.get(name)` → 先 Redis Hash（`ConfigCacheKeyBuilder`），miss 再查库并 `hSet` 回填。

**已实现的刷新**（管理端改配置时）：

```text
PUT /config/update 或 /config/batchUpdate
  → DB updateById
  → clearConfigCache()      // 删除整包配置 Hash
  → resetConfigCache()      // 再从 DB 全量 async 写入 Redis
```

代码：`IndexController.updateConfig` / `batchUpdateConfig`。

**要保证最新，你需要：**

| 做法 | 说明 |
|------|------|
| **走官方更新接口** | 不要只改 MySQL 行而不调 `clearConfigCache`；否则 Redis 里仍是旧值。 |
| **改库后手动刷新** | 调 `SysConfigService.resetConfigCache()`，或管理端再保存一次触发上述接口。 |
| **注意 `loadingConfigCache`** | 仅当 Redis 中配置 Hash **长度为 0** 才从 DB 批量加载；已有缓存时不会自动全量重载——依赖上面的 **del + reset**。 |
| **单 key 的 `get()` 回填** | 仅当该 field **不存在** 时才查库写入；**不会覆盖已有旧 field**，所以不能指望「读一次就更新」。 |

多微服务实例共用同一 Redis 时，一次 `clearConfigCache` + `resetConfigCache` 对所有实例的后续 `get()` 生效（下次读共享 Redis）。

### 10.2 实体级业务缓存（用户、角色、字典等）

继承 `SuperCacheManagerImpl` 的 Manager：**updateById / removeById / updateAllById** 后会 **`delCache`**，一般能保证与 DB 一致。

| 要保证最新 | 说明 |
|------------|------|
| 用 Manager 提供的写方法 | 绕过 Manager 直接改表会导致 Redis 残留。 |
| 批量运维改库 | 需按实体类型删除对应 `*CacheKeyBuilder` 的 key，或重启并清空相关 Redis 前缀。 |

### 10.3 Nacos 中的 YAML（`common.yml`、`redis.yml` 等）

这是 **Spring 配置**，不是 `sys_config` Redis Hash。

| 机制 | 说明 |
|------|------|
| `@RefreshScope` | 如 `SystemProperties`、`CaptchaProperties` 等，Nacos 推送变更后可刷新**该 Bean**。 |
| 未加 `@RefreshScope` 的 Bean | 改 Nacos 后常需 **重启对应微服务** 才生效。 |
| 连接 Redis 本身 | 改 `redis.yml` 的 host/密码通常要 **重启** 才能换连接。 |

### 10.4 IM 运行时缓存（在线、好友、路由、在途消息）

不靠「配置表」，靠 **业务事件** 更新：

- 上下线 → `SessionManager.syncOnline`
- 进退群 → `RoomAppServiceImpl` 维护 Set
- 好友 → 申请通过后 `sAdd`
- 路由 → WS 注册/注销时写 `RouterCacheKeyBuilder`

改 DB 不会自动修正这些 Key；需走对应业务 API 或运维脚本删除相关 Redis Key。

### 10.5 客户端本地配置

Tauri 客户端 `config` 可能在 **localStorage** / 本地 YAML，与服务端 Redis **无关**。改服务端 `sys_config` 后，客户端需 **重新拉取 init/配置接口** 或清本地缓存。

### 10.6 推荐 checklist（改配置后）

1. **后台参数（logo、ICE、存储域名等）**：用 `/config/update`，或改库后调用 `resetConfigCache()`。
2. **用户/权限/字典**：用各模块 Manager 更新，或手动 `del` 对应缓存 key。
3. **Nacos YAML**：确认 Bean 是否 `@RefreshScope`；否则滚动重启服务。
4. **仍读到旧值**：查是否走了 Caffeine 本地缓存（`luohuo.cache.type`）、是否改错了 Redis DB index、是否多环境共用 Redis 但 key 未隔离。
5. **生产批量改库**：脚本末尾加「按前缀 SCAN + DEL」或统一调刷新接口，避免漏清。

---

## 十一、与 MySQL、RocketMQ 的分工

```text
MySQL     → 持久化真相源
RocketMQ  → IM ↔ WS 异步投递
Redis     → 在线/路由、热点缓存、Token/验证码、推送在途、分布式锁、AI 向量等
```

---

## 十二、主要代码索引

| 领域 | 路径 |
|------|------|
| IM Key 常量 | `luohuo-im-biz/.../RedisKey.java` |
| Presence / Friend | `luohuo-common/.../PresenceCacheKeyBuilder.java`、`FriendCacheKeyBuilder.java` |
| WS 路由 | `luohuo-router/.../RouterCacheKeyBuilder.java`、`NacosRouterService.java` |
| 系统配置缓存 | `luohuo-config-sdk/.../SysSysConfigServiceImpl.java` |
| 配置更新 API | `luohuo-system-controller/.../IndexController.java` |
| 会话上下线 | `luohuo-ws-biz/.../SessionManager.java` |
| 在途消息 | `luohuo-common/.../PassageMsgCacheKeyBuilder.java` |
| 缓存框架 | `luohuo-util/luohuo-cache-starter/` |
| 实体缓存基类 | `luohuo-mvc/.../SuperCacheManagerImpl.java` |

---

*与同目录 `Nacos-RocketMQ-MinIO在HuLa中的作用.md` 配套阅读。*
