
---

# 8. `M2-PRD.md`

```md
# Phase 4 M2 — Email & Response Intelligence

## Goal

Detect job-related incoming emails and safely map them to applications.

---

# Scope

Implement one email provider first.

The provider must use explicit user authorization.

---

# Email Pipeline

```text
Email Provider
 ↓
Sync
 ↓
Normalize
 ↓
Classify
 ↓
Extract
 ↓
Match Application
 ↓
Validate
 ↓
Create Activity
 ↓
Update State
 ↓
Notify