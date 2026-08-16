
---

# 6. `phase-5/Deployment.md`

```md
# Phase 5 Deployment Specification

## Goal

Deploy AI Career OS so it can be accessed reliably outside the development machine.

---

# Production Components

```text
Internet
   │
   ▼
HTTPS / Reverse Proxy
   │
   ├──────────────► Frontend
   │
   └──────────────► Spring Boot API
                         │
              ┌──────────┼──────────┐
              ▼          ▼          ▼
            MySQL      Redis      RabbitMQ
              │
              ▼
             MinIO
              │
              ▼
            Ollama