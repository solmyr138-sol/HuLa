# HuLa Server

Spring Cloud **server** for IM: gateway routing, auth, IM domain services, WebSocket push, optional AI (`luohuo-ai`), backed by MySQL, Redis, and RocketMQ.

## Language

**Server**:
The `HuLa-Server/` backend and its `luohuo-*` deployable modules.
_Avoid_: Client, Tauri, frontend

**Service module**:
A Maven module under `luohuo-cloud/` (e.g. `luohuo-im-server`, `luohuo-gateway`) deployed as its own process.
_Avoid_: Package (Java package), plugin

**Gateway**:
`luohuo-gateway` — edge routing, auth, rate limits; clients hit this, not individual services directly in production.
_Avoid_: API gateway as generic term without module name

**IM service**:
Domain logic for contacts, groups, messages, announcements (`luohuo-im` and related tables `im_*`).
_Avoid_: Chat app, messaging microservice (vague)

**WebSocket service**:
`luohuo-ws` — long-lived connections and push; pairs with Netty/WebFlux stack.
_Avoid_: WS server colloquialism in docs

**Tenant**:
Multi-tenant isolation in platform tables (`def_tenant`, datasource routing); IM DB may be sharded per tenant (`luohuo_im_01`, etc.).
_Avoid_: User (a person), account (client-side)

**Message**:
Persisted IM record and/or MQ payload representing user-visible traffic; ordering and idempotency matter for group chat.
_Avoid_: DTO (implementation detail), event (unless RocketMQ event)

**Presence**:
Online state and routing hints (`luohuo-presence`); used to target push.
_Avoid_: Status message, last seen (unless product feature)

**Outbox / MQ**:
RocketMQ for async decoupling between services; transactional messaging where configured.
_Avoid_: Queue (generic), Kafka

**Nacos**:
Service discovery and shared config for local/docker and cloud deploys.
_Avoid_: Consul, Eureka (not used here)

## Example dialogue

**Dev:** Should we add validation in the gateway or IM service?

**Expert:** Auth and cross-cutting checks belong on the **gateway**; **message** business rules stay in the **IM service**. If other services must react, publish via **outbox / MQ** rather than calling IM internals from the gateway.
