
---

# 4. `phase-5/Database.md`

```md
# Phase 5 Database Specification

## Principle

Phase 5 should minimize database changes.

Phases 1–4 already contain the core domain model.

Do not duplicate existing entities.

---

# Existing Core Domains

Reuse:

- users
- profiles
- skills
- jobs
- job matches
- applications
- executions
- workspaces
- recruiters
- communications
- email messages
- follow-ups
- interviews
- interview preparations
- notifications

---

# Optional Phase 5 Tables

Only create additional tables when required by an actual feature.

Potential tables:

## user_preferences

Stores:

- preferred job locations
- preferred roles
- minimum match score
- notification preferences
- automation preferences

---

## integration_connections

Stores provider connection metadata.

Fields:

- id
- user_id
- provider
- status
- external_account_id
- created_at
- updated_at

Never store credentials in plaintext.

---

## automation_settings

Stores:

- automation enabled
- auto-follow-up enabled
- auto-apply enabled
- daily limits
- approval policy

All dangerous automation must default to OFF.

---

# Database Rules

- Flyway only
- never edit previous migrations
- all new schema changes use new migrations
- foreign keys where appropriate
- indexes for frequent queries
- timestamps in UTC
- optimistic locking where needed

---

# Data Retention

Only store data required for product functionality.

Sensitive data must have defined retention behavior.