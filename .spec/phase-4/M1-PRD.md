
---

# 7. `M1-PRD.md`

This is the **first file you should give Antigravity**.

```md
# Phase 4 M1 — Application Tracking Engine

## Goal

Build the central tracking system for every application.

---

## Features

### 1. Application Timeline

Track:

- discovered
- qualified
- prepared
- approved
- applied
- confirmation
- recruiter contact
- recruiter response
- follow-up
- interview
- rejection
- offer

---

### 2. Activity Model

Create `ApplicationActivity`.

Fields:

- id
- applicationId
- activityType
- source
- description
- metadata
- confidence
- createdAt

---

### 3. Next Action Engine

Calculate:

- next action
- due date
- reason

Example:

```text
APPLIED
↓
3 days
↓
NO RESPONSE
↓
FOLLOW_UP_DUE