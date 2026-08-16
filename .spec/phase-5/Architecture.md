
---

# 3. `phase-5/Architecture.md`

```md
# Phase 5 Architecture

## 1. Architecture

Phase 5 adds a frontend to the existing backend.

```text
                Browser
                   │
                   ▼
          Production Frontend
                   │
                   │ HTTPS
                   ▼
          Spring Boot Backend
                   │
       ┌───────────┼───────────┐
       ▼           ▼           ▼
     MySQL       Redis      RabbitMQ
       │                       │
       ▼                       ▼
    MinIO                    Workers
                               │
                               ▼
                             Ollama