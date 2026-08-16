
---

# 8. `M2-PRD.md`

```md
# Phase 5 M2 — Real Integrations

## Goal

Replace development/mock integrations with carefully controlled real integrations.

---

# Priority

Implement one provider at a time.

Order:

1. Email
2. Job source
3. Application provider
4. Notifications

---

# Email

Initial implementation should support one real email provider.

Requirements:

- OAuth authorization
- secure token storage
- message synchronization
- thread synchronization
- job-email classification
- application matching

Start with READ access.

Do not automatically send email during initial integration testing.

---

# Job Source

Connect one reliable job source already compatible with the existing job ingestion architecture.

Requirements:

- scheduled fetching
- deduplication
- source tracking
- error handling
- rate limits

---

# Application Provider

Connect one real application provider supported by the existing provider SPI.

Requirements:

- capability validation
- form discovery
- field mapping
- human approval
- execution lock
- execution result
- audit trail

Never assume a provider supports every job.

---

# Notifications

At least one real notification channel must work.

Existing Telegram integration may be reused.

---

# Security

All provider credentials must be protected.

Never log:

- access tokens
- refresh tokens
- passwords
- secret keys

---

# Testing

Each real provider must have:

- sandbox/mock tests
- failure tests
- authentication failure tests
- rate-limit tests
- duplicate-event tests

---

# Definition of Done

At least one real provider works end-to-end.

All existing tests pass.

Stop after M2.