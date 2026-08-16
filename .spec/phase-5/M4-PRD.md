
---

# 10. `M4-PRD.md`

```md
# Phase 5 M4 — Production Deployment

## Goal

Deploy AI Career OS securely and reliably.

---

# Deployment

Deploy:

- frontend
- Spring Boot backend
- MySQL
- Redis
- RabbitMQ
- MinIO
- Ollama

---

# Requirements

## HTTPS

Required.

## Secrets

All production secrets externalized.

## Database

Backups enabled.

## Monitoring

Health and error monitoring enabled.

## Logging

Structured production logs.

## Recovery

Document:

- database restore
- service restart
- rollback
- failed migration recovery

---

# Production Profiles

Create a dedicated production configuration.

Development settings must never be reused blindly.

---

# Security

Verify:

- JWT configuration
- CORS
- authorization
- exposed ports
- database access
- Redis access
- RabbitMQ access
- MinIO access

---

# Health

Production health checks must verify:

- application
- database
- Redis
- RabbitMQ
- object storage

AI service health should also be monitored.

---

# Definition of Done

The deployed system:

- loads over HTTPS
- allows login
- loads dashboard
- communicates with backend
- processes jobs
- persists data
- survives service restart
- has backups
- has monitoring

Run production smoke tests.

Stop after M4.