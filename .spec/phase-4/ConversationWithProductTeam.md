Yes. If **M6-A, M6-B, and M6-C are all completed and tested**, then you've reached the most important milestone so far:

# 🎯 Phase 3 — Application Automation Engine is complete

Your system has now moved from an **AI job assistant** toward an actual **application execution platform**.

The next step should **NOT** be another browser-automation phase.

We should move to:

# 🚀 PHASE 4 — Application Tracking, Follow-ups & Response Intelligence

This is the missing piece between:

> **“I applied to jobs”**

and

> **“The system actively works toward getting me interviews.”**

---

# Where your project stands now

```text
AI CAREER OS
│
├── PHASE 1 ✅
│   ├── Job discovery
│   ├── Job matching
│   ├── Profile
│   ├── Notifications
│   └── Infrastructure
│
├── PHASE 2 ✅
│   ├── Job analysis
│   ├── ATS resume
│   ├── Cover letters
│   ├── Recruiter intelligence
│   ├── Cold emails
│   └── AI Workspace
│
├── PHASE 3 ✅
│   ├── Application state machine
│   ├── Execution provider
│   ├── Distributed locks
│   ├── Form intelligence
│   ├── Automation orchestrator
│   ├── Browser automation
│   ├── Application execution
│   └── Submission verification
│
└── PHASE 4 🚀 NEXT
    ├── Application tracking
    ├── Email/response intelligence
    ├── Follow-up automation
    ├── Recruiter response tracking
    ├── Interview detection
    └── Interview preparation
```

## What Phase 4 should accomplish

Imagine you wake up and the system has done this:

```text
08:00
↓
Found 37 new jobs
↓
Matched 12
↓
Rejected 7
↓
Prepared 5 applications
↓
Submitted 3
↓
Sent recruiter outreach for 2
↓
Checked previous applications
↓
Found 1 recruiter response
↓
Detected interview invitation
↓
Updated application → INTERVIEW
↓
Generated interview preparation
↓
Telegram:
"🎉 Interview detected for Software Engineer at XYZ"
```

**That is the direction we should build toward.**

---

# I recommend Phase 4 in 4 milestones

Don't make it huge.

## M1 — Application Tracking Engine

Build the central tracker.

```text
Application
     ↓
APPLIED
     ↓
WAITING
     ↓
FOLLOW_UP_DUE
     ↓
RESPONDED
     ↓
INTERVIEW
     ↓
OFFER
```

It should automatically calculate:

* days since application
* last activity
* next follow-up date
* application age
* response status
* recruiter status
* current stage

### Example

```text
Google
Software Engineer

Applied: 5 days ago
Status: APPLIED
Recruiter: Not contacted
Follow-up: Due tomorrow
```

---

# M2 — Email & Response Intelligence

This is extremely important.

Connect an email provider **only with explicit user authorization**.

The system analyzes incoming job-related messages.

For example:

```text
Email received
      ↓
AI classifier
      ↓
Is this job related?
      ↓
YES
      ↓
Classify
├── Rejection
├── Interview
├── Recruiter response
├── Assessment
├── Application confirmation
├── Request for information
└── Other
```

Then update the application automatically.

Example:

```text
Email:
"We'd love to schedule a 30-minute interview..."

AI
↓
INTERVIEW_INVITATION

Application:
RESPONDED → INTERVIEW
```

---

# M3 — Follow-up Automation

This is where your application starts **actively chasing opportunities** instead of just applying.

Example:

```text
Application submitted
        ↓
Wait 3 days
        ↓
No response?
        ↓
Follow-up #1
        ↓
Wait 5 days
        ↓
No response?
        ↓
Follow-up #2
        ↓
Stop
```

But we should make this configurable.

### Channels

```text
Email
LinkedIn
Recruiter email
```

Initially, I recommend:

**Email first.**

LinkedIn automation can introduce additional platform/security complexity, so don't make it the first priority.

---

# M4 — Interview Intelligence

Once an interview is detected:

```text
Interview detected
       ↓
Company research
       ↓
Job description
       ↓
Candidate resume
       ↓
Candidate experience
       ↓
Generate preparation
       ↓
Likely questions
       ↓
Technical questions
       ↓
Behavioral questions
       ↓
Company-specific questions
       ↓
Mock interview
       ↓
Feedback
```

This becomes your **Interview Copilot**.

---

# The complete system after Phase 4

```text
                 JOB SOURCES
                     │
                     ▼
              JOB DISCOVERY
                     │
                     ▼
              JOB MATCHING
                     │
                     ▼
              AI QUALIFICATION
                     │
                     ▼
          ┌─────────────────────┐
          │   AI WORKSPACE      │
          │                     │
          │ Resume              │
          │ Cover Letter        │
          │ Recruiter           │
          │ Cold Email          │
          │ Form Mapping        │
          └──────────┬──────────┘
                     │
                     ▼
                 APPROVAL
                     │
                     ▼
            BROWSER AUTOMATION
                     │
                     ▼
               APPLICATION
                     │
                     ▼
              VERIFICATION
                     │
                     ▼
          ┌──────────────────────┐
          │ APPLICATION TRACKER  │
          └──────────┬───────────┘
                     │
          ┌──────────┼───────────┐
          ▼          ▼           ▼
       EMAIL      FOLLOW-UP   STATUS
       INTEL        ENGINE    TRACKING
          │          │           │
          └──────────┼───────────┘
                     ▼
                INTERVIEW
                     │
                     ▼
            INTERVIEW COPILOT
                     │
                     ▼
                  OFFER 🎯
```

# And now: launch strategy

You **can start using the application privately now** if M6-C has genuinely passed its real-provider tests.

But I would **not publicly launch it yet**.

### Right now

Use it yourself as:

> **Private Beta / Personal Production**

Run real applications through it.

Track:

* successful applications
* failed applications
* incorrect field mappings
* duplicate applications
* submission verification failures
* false job matches
* recruiter responses
* interview calls

This real usage will expose issues that automated tests won't.

---

# Your immediate next step

We should now create the Phase 4 specification package.

I recommend:

```text
phase-4/
│
├── README.md
├── PRD.md
├── Architecture.md
├── Database.md
├── Events.md
├── Automation.md
│
├── M1-PRD.md
├── M2-PRD.md
├── M3-PRD.md
└── M4-PRD.md
```

And, just like we did previously, **you feed these to Antigravity one milestone at a time.**

### Phase 4 milestones

| Milestone | Goal                          | Priority    |
| --------- | ----------------------------- | ----------- |
| **M1**    | Application Tracking Engine   | 🔴 Critical |
| **M2**    | Email & Response Intelligence | 🔴 Critical |
| **M3**    | Follow-up Automation          | 🟠 High     |
| **M4**    | Interview Intelligence        | 🟠 High     |

**I would start with M1.**

And importantly, **don't start Phase 5 yet**. Phase 4 is what turns your current application executor into a system that can continuously manage the entire job-search lifecycle.



Yes. Since you've already created the Phase 4 folder and all 10 files, **don't create anything else yet**.

We should fill them in this order:

```text
phase-4/
│
├── README.md          ← 1
├── PRD.md             ← 2
├── Architecture.md    ← 3
├── Database.md        ← 4
├── Events.md          ← 5
├── Automation.md      ← 6
│
├── M1-PRD.md          ← 7
├── M2-PRD.md           ← 8
├── M3-PRD.md           ← 9
└── M4-PRD.md           ← 10
```

The **6 architecture/spec files define the whole phase**, and the 4 M1–M4 files tell Antigravity exactly what to implement.

## What we're building in Phase 4

```text
PHASE 4 — JOB SEARCH AUTOPILOT
                         │
                         ▼
              ┌──────────────────┐
              │ M1                │
              │ Application       │
              │ Tracking          │
              └────────┬─────────┘
                       ▼
              ┌──────────────────┐
              │ M2                │
              │ Email + Response  │
              │ Intelligence      │
              └────────┬─────────┘
                       ▼
              ┌──────────────────┐
              │ M3                │
              │ Follow-up         │
              │ Automation        │
              └────────┬─────────┘
                       ▼
              ┌──────────────────┐
              │ M4                │
              │ Interview         │
              │ Intelligence      │
              └──────────────────┘
```

---

# 1. `README.md`

Paste this:

````md
# AI Career OS — Phase 4

## Job Search Autopilot

Phase 4 transforms AI Career OS from an application execution system into a continuous job-search management system.

Phase 1 discovers and matches jobs.

Phase 2 prepares AI-powered application artifacts.

Phase 3 executes controlled applications.

Phase 4 manages what happens AFTER and AROUND the application:

- application tracking
- application status intelligence
- email response detection
- recruiter response detection
- interview detection
- follow-up scheduling
- follow-up generation
- interview preparation

---

# Phase 4 Goal

The system should continuously manage the candidate's job-search lifecycle.

```text
JOB DISCOVERED
      ↓
MATCHED
      ↓
PREPARED
      ↓
APPROVED
      ↓
APPLIED
      ↓
TRACKED
      ↓
EMAIL / RESPONSE DETECTED
      ↓
┌─────────────────────────────┐
│                             │
│ REJECTION                   │
│     ↓                       │
│ CLOSED                      │
│                             │
│ NO RESPONSE                 │
│     ↓                       │
│ FOLLOW-UP                   │
│                             │
│ RECRUITER RESPONSE          │
│     ↓                       │
│ RESPONDED                   │
│                             │
│ INTERVIEW                   │
│     ↓                       │
│ INTERVIEW PREPARATION       │
│                             │
│ OFFER                       │
└─────────────────────────────┘
````

---

# Milestones

## M1 — Application Tracking Engine

Build the central application tracking system.

Responsibilities:

* application timeline
* application status
* activity tracking
* next-action calculation
* application aging
* follow-up eligibility
* recruiter contact state
* dashboard data

---

## M2 — Email & Response Intelligence

Connect an authorized email account and classify job-related messages.

Detect:

* application confirmation
* recruiter response
* rejection
* interview invitation
* assessment request
* additional-information request
* offer
* other job-related messages

Never send email automatically in M2.

---

## M3 — Follow-up Automation

Build intelligent follow-up scheduling and generation.

The system should:

* determine when follow-up is due
* generate personalized follow-up messages
* prevent duplicate follow-ups
* respect configurable limits
* stop follow-ups after response/rejection/interview
* notify the user
* optionally send only after explicit approval

---

## M4 — Interview Intelligence

When an interview is detected:

* update application status
* extract interview details
* research the company/job using permitted sources
* generate interview preparation
* generate likely questions
* generate behavioral questions
* generate technical questions
* create mock interview sessions
* provide feedback

---

# Safety Principles

The system must:

1. Never fabricate candidate facts.
2. Never claim a response exists without evidence.
3. Never claim an interview exists without evidence.
4. Never send an email without the configured approval policy.
5. Never spam recruiters.
6. Never repeatedly contact the same recruiter without tracking.
7. Never expose email credentials.
8. Never log email passwords or OAuth tokens.
9. Respect provider permissions and rate limits.
10. Preserve a complete audit trail.

---

# Technology

Existing stack remains:

* Java 21
* Spring Boot
* MySQL
* Redis
* RabbitMQ
* MinIO
* Ollama
* Spring Security/JWT
* Docker
* Flyway

Reuse existing architecture.

Do not introduce another backend framework.

---

# Phase 4 Completion

Phase 4 is complete when the system can:

1. Track every application.
2. Maintain an application timeline.
3. Detect relevant job-related emails.
4. Classify responses.
5. Update application state.
6. Schedule follow-ups.
7. Generate personalized follow-ups.
8. Detect interviews.
9. Prepare the candidate for interviews.
10. Notify the user of important events.

---

# Implementation Rule

Build one milestone at a time.

Order:

M1 → M2 → M3 → M4

Do not implement later milestones early.

After each milestone:

* run tests
* verify regression suite
* provide walkthrough
* commit
* push
* stop for approval

````

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
````

---

# 3. `Architecture.md`

````md
# Phase 4 Architecture

## 1. Architecture Principle

Phase 4 extends the existing event-driven architecture.

Existing:

```text
Job Discovery
      ↓
Matching
      ↓
AI Workspace
      ↓
Application Execution
````

Phase 4 adds:

```text
Application
      ↓
Tracking
      ↓
Email Intelligence
      ↓
Follow-up
      ↓
Interview Intelligence
```

---

# 2. Components

## Application Tracking Service

Owns:

* application timeline
* activities
* next action
* tracking state

## Email Integration Service

Owns:

* provider connection
* message synchronization
* message normalization

## Email Intelligence Service

Owns:

* classification
* extraction
* application matching

## Follow-up Service

Owns:

* follow-up eligibility
* scheduling
* generation
* approval

## Interview Intelligence Service

Owns:

* interview detection
* interview data extraction
* preparation generation

## Notification Service

Existing notification infrastructure remains the notification layer.

---

# 3. Data Flow

```text
Email Provider
      ↓
Email Sync
      ↓
Message Normalizer
      ↓
AI Classifier
      ↓
Application Matcher
      ↓
Application Event
      ↓
State Update
      ↓
Notification
```

---

# 4. AI Architecture

AI must not directly mutate critical application state.

Correct:

```text
Email
 ↓
AI classification
 ↓
Structured result
 ↓
Validation
 ↓
Business rules
 ↓
State transition
```

Incorrect:

```text
Email
 ↓
LLM
 ↓
direct database update
```

---

# 5. Existing Infrastructure

Reuse:

* RabbitMQ
* Redis
* Ollama
* MySQL
* Flyway
* MinIO
* Spring Security

---

# 6. Failure Strategy

If AI is unavailable:

* use deterministic rules where possible
* mark ambiguous messages for review
* never guess critical status

If email provider is unavailable:

* retry with bounded backoff
* notify user after repeated failure

---

# 7. Observability

Every workflow should have:

* correlation ID
* application ID
* user ID
* source event
* processing status
* confidence
* timestamp

Never log message bodies unnecessarily.

````

---

# 4. `Database.md`

```md
# Phase 4 Database Specification

## Principle

Extend the existing MySQL schema using Flyway.

Never modify old migrations.

Create new migrations.

---

# Core Tables

## application_activities

Tracks the application timeline.

Fields:

- id
- application_id
- activity_type
- source
- description
- metadata_json
- confidence
- created_at

---

## application_follow_ups

Fields:

- id
- application_id
- channel
- sequence_number
- scheduled_at
- status
- message_artifact_id
- sent_at
- approved_at
- created_at
- updated_at

Statuses:

- SCHEDULED
- READY
- APPROVAL_REQUIRED
- SENT
- CANCELLED
- FAILED

---

## email_messages

Store normalized email metadata.

Fields:

- id
- provider
- external_message_id
- external_thread_id
- sender
- sender_domain
- subject
- received_at
- classification
- classification_confidence
- application_id
- processed_at
- created_at

Do not store raw email content unless required.

---

## email_classification_results

Fields:

- id
- email_message_id
- classification
- confidence
- extracted_data_json
- model
- created_at

---

## interviews

Fields:

- id
- application_id
- status
- scheduled_at
- timezone
- interview_type
- meeting_url
- interviewer_json
- source_email_id
- created_at
- updated_at

---

## interview_preparations

Fields:

- id
- interview_id
- preparation_json
- model
- version
- created_at
- updated_at

---

# Indexes

Required indexes:

- application_id
- external_message_id
- external_thread_id
- received_at
- classification
- scheduled_at
- status

External message IDs must be unique per provider.

---

# Data Security

Never store:

- email passwords
- OAuth client secrets
- access tokens in plaintext
- refresh tokens in plaintext
- unnecessary sensitive email content

Secrets must use secure configuration/secret storage.
````

---

# 5. `Events.md`

```md
# Phase 4 Events

## Application Events

### ApplicationApplied

Published when a verified application is created.

Payload:

- applicationId
- userId
- jobId
- timestamp
- correlationId

---

### ApplicationActivityRecorded

Published when an application activity occurs.

---

### ApplicationFollowUpDue

Published when a follow-up becomes due.

---

### ApplicationFollowUpSent

Published after a follow-up is successfully sent.

---

# Email Events

### EmailReceived

Contains:

- messageId
- provider
- threadId
- receivedAt
- correlationId

Do not include secrets.

---

### JobEmailClassified

Contains:

- messageId
- classification
- confidence
- applicationId
- correlationId

---

### RecruiterResponseDetected

Contains:

- applicationId
- messageId
- confidence

---

### RejectionDetected

Contains:

- applicationId
- messageId
- confidence

---

### InterviewDetected

Contains:

- applicationId
- interviewId
- confidence

---

# Interview Events

### InterviewScheduled

Contains:

- applicationId
- interviewId
- scheduledAt
- timezone

---

### InterviewPreparationRequested

Contains:

- interviewId
- applicationId

---

### InterviewPreparationReady

Contains:

- interviewId
- preparationId

---

# Rules

All events must be:

- idempotent
- traceable
- versioned where needed
- safe to retry

Consumers must tolerate duplicate delivery.
```

---

# 6. `Automation.md`

````md
# Phase 4 Automation Specification

## Automation Principle

Automate repetitive work.

Require human review for ambiguous or high-impact actions.

---

# Application Tracking

Automatically:

- calculate application age
- update timeline
- calculate next action
- detect overdue follow-ups
- notify user

---

# Email Processing

Scheduled process:

```text
Every N minutes
      ↓
Fetch new messages
      ↓
Normalize
      ↓
Filter job-related messages
      ↓
Classify
      ↓
Match application
      ↓
Validate confidence
      ↓
Update application
````

---

# Follow-up Automation

```text
Application
 ↓
No response
 ↓
Follow-up eligible?
 ↓
YES
 ↓
Generate message
 ↓
Approval policy
 ↓
Send OR request approval
```

Default:

`APPROVAL_REQUIRED`

---

# Interview Automation

```text
Interview email
 ↓
Extract details
 ↓
Validate
 ↓
Create interview
 ↓
Update application
 ↓
Generate preparation
 ↓
Notify user
```

---

# Safety

Never automatically:

* spam recruiters
* send unlimited messages
* send low-confidence responses
* invent interview details
* change an application to INTERVIEW without evidence
* send a follow-up after rejection
* send a follow-up after recruiter response unless configured

---

# Rate Limits

Implement:

* per application
* per recruiter
* per email address
* per day
* per provider

Limits must be configurable.

---

# Scheduling

Use Spring scheduling or existing messaging infrastructure.

Long-running workflows should use RabbitMQ rather than blocking scheduler threads.

---

# Retry

Retry transient errors.

Never blindly retry a send operation if delivery status is unknown.

Use idempotency keys for outbound messages.

````

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
````

---

### 4. Dashboard API

Provide:

GET `/api/v1/applications`

GET `/api/v1/applications/{id}`

GET `/api/v1/applications/{id}/timeline`

GET `/api/v1/applications/{id}/next-action`

GET `/api/v1/applications/stats`

---

### 5. Idempotency

Duplicate activities must not be created for the same event.

---

### 6. Tests

Test:

* timeline
* state synchronization
* duplicate activity
* next-action calculation
* overdue applications
* regression

---

## Definition of Done

* database migration
* entities
* repositories
* services
* controllers
* event integration
* tests
* documentation
* `.\gradlew.bat test` passes

Stop after M1 and provide walkthrough.

````

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
````

---

# Classifications

* APPLICATION_CONFIRMATION
* RECRUITER_RESPONSE
* INTERVIEW_INVITATION
* ASSESSMENT
* REJECTION
* OFFER
* INFORMATION_REQUEST
* OTHER

---

# AI Rules

LLM output must be structured.

Validate output before business logic.

Low confidence:

`REVIEW_REQUIRED`

Never let LLM directly update state.

---

# Application Matching

Use:

1. thread ID
2. external message ID
3. company
4. job title
5. sender
6. recruiter
7. confidence

Ambiguous matches require review.

---

# Security

Never log:

* password
* OAuth token
* refresh token
* unnecessary email body

---

# Tests

Use local mock email fixtures.

Test:

* classification
* extraction
* application matching
* low confidence
* duplicate message
* rejection
* interview
* confirmation

---

# Definition of Done

All tests pass.

Real provider integration works under explicit authorization.

Stop after M2.

````

---

# 9. `M3-PRD.md`

```md
# Phase 4 M3 — Follow-up Automation

## Goal

Automatically determine when applications need follow-up and generate safe personalized messages.

---

# Follow-up Rules

Default example:

```text
Application
 ↓
Wait 3–5 days
 ↓
No response
 ↓
Follow-up #1
 ↓
Wait 5–7 days
 ↓
No response
 ↓
Follow-up #2
 ↓
STOP
````

All timing is configurable.

---

# Stop Conditions

Stop follow-ups when:

* recruiter responds
* interview detected
* rejection detected
* offer detected
* application closed
* maximum follow-ups reached

---

# Message Generation

AI can generate:

* recruiter follow-up
* application follow-up
* thank-you message

Messages must use verified candidate/application facts.

---

# Approval

Default:

`APPROVAL_REQUIRED`

Only send automatically when user explicitly enables automatic sending.

---

# Duplicate Protection

Use:

* application ID
* channel
* recipient
* sequence number
* idempotency key

---

# Rate Limits

Implement:

* daily limit
* recruiter limit
* application limit

---

# Tests

Test:

* scheduling
* stop conditions
* duplicate prevention
* message generation
* approval
* rate limits
* send failure
* unknown delivery result

---

# Definition of Done

Follow-ups are generated and tracked reliably.

No spam behavior.

Stop after M3.

````

---

# 10. `M4-PRD.md`

```md
# Phase 4 M4 — Interview Intelligence

## Goal

Turn detected interviews into a personalized interview-preparation workspace.

---

# Interview Detection

Consume:

`InterviewDetected`

Create:

`Interview`

Extract:

- company
- role
- date
- time
- timezone
- meeting URL
- interview type
- interviewer

Never invent missing data.

---

# Interview Preparation

Generate:

## Company

- company overview
- product/business
- relevant context

## Role

- role responsibilities
- important skills
- likely focus areas

## Candidate

- strongest relevant experience
- resume talking points
- potential weak areas

## Questions

- behavioral
- technical
- role-specific
- company-specific

## Questions to Ask

Generate useful questions for the interviewer.

---

# Mock Interview

Provide:

```text
Question
 ↓
Candidate Answer
 ↓
AI Evaluation
 ↓
Score
 ↓
Feedback
 ↓
Improved Answer
````

---

# Safety

Never fabricate:

* company facts
* candidate experience
* technical experience
* interview details

Clearly distinguish:

* verified facts
* inferred preparation
* AI suggestions

---

# Tests

Test:

* interview creation
* extraction
* preparation generation
* missing data
* AI failure
* mock interview
* regression

---

# Definition of Done

When an interview is detected, the system automatically creates a preparation workspace and notifies the user.

Stop after M4.

---

# Phase 4 Completion

After M4:

* run full regression suite
* perform private real-world testing
* review security
* review email permissions
* review automation limits
* document deployment

Do not start Phase 5 until Phase 4 has been validated.

````

---

## Now what do you do?

**Do not paste all 10 into Antigravity as one giant request.**

Use this sequence:

### Step 1

Fill all 10 files with the above content.

### Step 2

Give Antigravity:

```text
phase-4/README.md
phase-4/PRD.md
phase-4/Architecture.md
phase-4/Database.md
phase-4/Events.md
phase-4/Automation.md
phase-4/M1-PRD.md
````

Tell it:

> **Read the Phase 4 master specification and M1 PRD. Inspect the existing completed Phase 1–3 implementation. Do not rewrite existing functionality. Implement only Phase 4 M1. Follow the PRD, run the full regression suite, provide a walkthrough, commit and push, then STOP.**

Then let it build **M1**.

After M1 is completed, we move to **M2**, then M3, then M4.

This keeps your AI agent from trying to build the entire Phase 4 at once and breaking the production foundation you've already built.
