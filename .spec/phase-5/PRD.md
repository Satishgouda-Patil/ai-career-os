
---

# 2. `phase-5/PRD.md`

```md
# Phase 5 PRD — Production Launch

## 1. Product Vision

AI Career OS should become a personal Career Operating System that can be opened every day and used to manage the entire job-search process.

The product must provide one place to:

- discover jobs
- evaluate opportunities
- prepare applications
- approve applications
- execute applications
- track applications
- monitor recruiter communication
- manage follow-ups
- prepare for interviews
- analyze job-search performance

---

# 2. Current System

Phases 1–4 are complete.

Existing capabilities include:

- authentication
- profile management
- job ingestion
- job matching
- AI matching
- resume generation
- cover letters
- recruiter discovery
- communication generation
- application execution
- application state machine
- execution locks
- form discovery
- field mapping
- application tracking
- email intelligence
- follow-up automation
- interview intelligence
- mock interviews

Phase 5 must build on this foundation.

---

# 3. Product Requirements

## PR-1 Dashboard

Dashboard must show:

- jobs found today
- high-match jobs
- applications
- applications requiring review
- follow-ups due
- recruiter responses
- upcoming interviews
- recent activity

---

## PR-2 Job Management

User can:

- browse jobs
- filter jobs
- sort by match score
- view job details
- view AI analysis
- open application workspace

---

## PR-3 Application Workspace

Application workspace must show:

- job information
- match score
- resume
- cover letter
- recruiter
- recruiter message
- application readiness
- execution status
- approval status
- timeline

---

## PR-4 Application Tracking

User can see:

- all applications
- current state
- company
- role
- applied date
- last activity
- next action
- interview status

---

## PR-5 Communication

User can see:

- recruiter communications
- generated messages
- follow-ups
- email-derived events
- approval requests

---

## PR-6 Interview Workspace

User can:

- view interviews
- view interview details
- view preparation
- practice mock interviews
- receive AI feedback

---

## PR-7 Notifications

Notifications should surface:

- high-quality jobs
- application review requests
- recruiter responses
- follow-up due
- interview detected
- interview approaching
- execution failures

---

# 4. UX Principles

The application must be:

- clean
- minimal
- fast
- responsive
- professional
- desktop-first but mobile-friendly

Avoid unnecessary animations.

Avoid clutter.

Important actions must be obvious.

---

# 5. Human Approval

The UI must clearly distinguish:

```text
READY
REVIEW REQUIRED
APPROVED
EXECUTING
APPLIED
FAILED