
---

# 2. `PRD.md`

```md
# Phase 4 PRD — Job Search Autopilot

## 1. Product Vision

AI Career OS should continuously manage the candidate's job-search lifecycle instead of stopping after application submission.

The product should answer:

- What jobs did I apply to?
- What happened after I applied?
- Who responded?
- Which applications need follow-up?
- Which recruiter contacted me?
- Which applications have interviews?
- What should I do next?

---

# 2. Problem

After submitting applications, candidates usually manage everything manually:

- spreadsheets
- emails
- recruiter conversations
- follow-ups
- interview scheduling
- preparation

This causes missed opportunities and inconsistent follow-ups.

AI Career OS will centralize and automate this process.

---

# 3. Target User

Initial target:

A single job seeker using AI Career OS for their own job search.

Phase 4 is initially a personal/private production system.

Multi-user SaaS concerns are secondary.

---

# 4. Functional Requirements

## FR-1 Application Tracking

Every application must have:

- application ID
- job
- company
- applied timestamp
- current status
- recruiter status
- last activity
- next action
- next action date
- timeline
- execution reference

---

## FR-2 Application Timeline

Record events such as:

- discovered
- qualified
- prepared
- approved
- applied
- confirmation received
- recruiter contacted
- recruiter responded
- follow-up sent
- interview scheduled
- interview completed
- rejection
- offer

---

## FR-3 Email Classification

The system must classify authorized incoming messages.

Categories:

- APPLICATION_CONFIRMATION
- RECRUITER_RESPONSE
- INTERVIEW_INVITATION
- ASSESSMENT
- REJECTION
- OFFER
- INFORMATION_REQUEST
- OTHER

Classification must include confidence.

Low-confidence classifications require review.

---

## FR-4 Application Matching

Incoming emails should be matched to an existing application using:

1. provider message/thread identifiers
2. application reference
3. company
4. job title
5. sender domain
6. recruiter identity
7. confidence scoring

If confidence is insufficient, do not automatically update the application.

---

## FR-5 Follow-up

The system calculates follow-up eligibility.

Example default:

- Follow-up 1: 3–5 days
- Follow-up 2: 5–7 days later
- stop after configured maximum

Exact values must be configurable.

---

## FR-6 Follow-up Generation

AI may generate:

- recruiter follow-up
- application status follow-up
- interview confirmation
- thank-you message

Generated messages must be grounded in stored facts.

---

## FR-7 Interview Detection

Detect interview invitations from email.

Extract:

- company
- job
- interview date
- interview time
- timezone
- interview type
- meeting URL if present
- interviewer if available

Never invent missing information.

---

## FR-8 Interview Preparation

Generate:

- company overview
- role analysis
- candidate-specific talking points
- likely behavioral questions
- likely technical questions
- questions to ask interviewer
- preparation checklist

---

# 5. Non-Functional Requirements

## Reliability

Duplicate messages/events must not create duplicate application activities.

## Security

OAuth credentials/tokens must never be logged.

## Auditability

Every automated state change must have:

- source
- timestamp
- confidence
- actor
- correlation ID

## Privacy

Only required email data should be persisted.

## Performance

Background processing must be asynchronous where appropriate.

---

# 6. Human-in-the-loop

Human approval is required for:

- low-confidence email classification
- ambiguous application matching
- sending follow-ups unless explicitly configured otherwise
- ambiguous interview information
- high-impact status changes

---

# 7. Success Metrics

Track:

- applications tracked
- response detection accuracy
- interview detection accuracy
- false-positive rate
- follow-up completion
- duplicate prevention
- recruiter response rate
- interview conversion rate

---

# 8. Out of Scope

Phase 4 does not build:

- universal email providers
- unlimited recruiter messaging
- spam automation
- CAPTCHA bypass
- job-site scraping outside existing systems
- guaranteed interviews
- automatic interview attendance