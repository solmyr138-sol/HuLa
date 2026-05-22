# Client and server live in one monorepo with separate contexts

HuLa ships as a Tauri **client** (`HuLa/`) and a Spring Cloud **server** (`HuLa-Server/`) in a single git repository. Domain language, ADRs, and agent skills are split by context (`CONTEXT-MAP.md`) so agents do not conflate Tauri commands with Spring services.

**Why:** Developers run and debug both sides locally (Docker stack under `HuLa-Server/docs/install/docker/`). Shared versioning and docs reduce drift between WebSocket/REST contracts and the client configuration in `src-tauri/configuration/`.

**Consequence:** Changes that touch wire protocols need coordinated updates in both contexts; record contract decisions in `docs/adr/` or extend the relevant `CONTEXT.md` via `/grill-with-docs`.
