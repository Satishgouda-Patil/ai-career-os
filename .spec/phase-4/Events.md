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