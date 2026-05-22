# HuLa Monorepo — Agent Context

HuLa is an instant-messaging product split across a **Tauri client** (`HuLa/`) and a **Spring Cloud server** (`HuLa-Server/`). Other folders (`HuLa-Electron/`, `HuLa-MCP/`) are auxiliary; default to client + server unless the user names another scope.

Client-specific conventions also live in `HuLa/AGENTS.md`. Read that file when editing only the Tauri/Vue app.

## Engineering workflow (Matt Pocock skills)

Skills are installed under `.agents/skills/` (see `skills-lock.json`). In Cursor, invoke them by name (e.g. `/grill-with-docs`, `/tdd`, `/diagnose`).

| When | Skill |
|------|--------|
| Starting a non-trivial change | `grill-with-docs` (preferred) or `grill-me` — align before coding |
| Bug or regression | `diagnose` — reproduce → minimise → hypothesise → fix → regression test |
| New feature or behaviour change | `tdd` — one vertical slice at a time (red → green → refactor) |
| Unfamiliar code area | `zoom-out` — system-level context first |
| Plan → tickets | `to-prd` then `to-issues` |
| Incoming issues | `triage` |
| Recurring design debt | `improve-codebase-architecture` |
| Session handoff | `handoff` |

**Rules:** Use vocabulary from `CONTEXT.md` / `CONTEXT-MAP.md`. Respect ADRs in `docs/adr/`. Do not skip alignment on large changes. Prefer small, test-backed steps.

## Agent skills

### Issue tracker

GitHub Issues on `solmyr138-sol/HuLa` via the `gh` CLI. See `docs/agents/issue-tracker.md`.

### Triage labels

Five canonical triage roles with default label names (`needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`). See `docs/agents/triage-labels.md`.

### Domain docs

Multi-context monorepo: read `CONTEXT-MAP.md`, then the relevant `HuLa/CONTEXT.md` or `HuLa-Server/CONTEXT.md`. System-wide ADRs live in `docs/adr/`. See `docs/agents/domain.md`.

## Repo layout

| Path | Role |
|------|------|
| `HuLa/` | Tauri v2 + Vue 3 + Rust client (desktop/mobile) |
| `HuLa-Server/` | Spring Boot 3 / Spring Cloud IM backend (`luohuo-*` modules) |
| `HuLa-Server/docs/install/docker/` | Local Docker stack for dev (MySQL, Redis, Nacos, RocketMQ, MinIO) |
| `.agents/skills/` | Matt Pocock agent skills (engineering + productivity) |

## Language

Reply in the same language the user uses (e.g. 简体中文 for Chinese questions).
