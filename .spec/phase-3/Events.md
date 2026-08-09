# AI Career OS — Phase 3

# Events & Messaging Specification

## Application Automation Event Architecture

Version: 3.0

Status: Specification

---

# 1. Purpose

This document defines the event-driven architecture for Phase 3.

Phase 3 transforms AI Career OS from an AI-assisted workspace into an automated career application system.

The event system connects:

```text
Job Discovery
      ↓
Matching
      ↓
Application Preparation
      ↓
Approval
      ↓
Application Execution
      ↓
Verification
      ↓
Communication
      ↓
Follow-up
      ↓
Response Detection
      ↓
Interview / Outcome
      ↓
Analytics
````

RabbitMQ is the asynchronous messaging backbone.

MySQL remains the source of truth.

Redis is used only for caching, locks, rate limiting, and short-lived coordination where appropriate.

---

# 2. Core Architecture

```text
                         ┌─────────────────┐
                         │      MySQL      │
                         │ Source of Truth │
                         └────────┬────────┘
                                  │
                                  │ Outbox
                                  ▼
                         ┌─────────────────┐
                         │    Publisher    │
                         └────────┬────────┘
                                  │
                                  ▼
                         ┌─────────────────┐
                         │    RabbitMQ     │
                         │ Event Backbone  │
                         └────────┬────────┘
                                  │
              ┌───────────────────┼───────────────────┐
              │                   │                   │
              ▼                   ▼                   ▼
       Application Worker   Communication Worker   Follow-up Worker
              │                   │                   │
              ▼                   ▼                   ▼
          Browser/API        Email Provider       Scheduler
              │                   │                   │
              └───────────────────┼───────────────────┘
                                  │
                                  ▼
                              MySQL
```

---

# 3. Messaging Principles

## Rule 1 — Events Are Facts

An event represents something that happened.

Example:

```text
application.approved
```

means:

> An application was approved.

It does not mean:

> Please approve this application.

Commands and events must not be confused.

---

# 4. Commands vs Events

## Command

A command requests an action.

Examples:

```text
application.prepare
application.execute
communication.send
followup.schedule
```

## Event

An event reports a completed state change.

Examples:

```text
application.prepared
application.approved
application.submitted
communication.sent
followup.scheduled
```

---

# 5. RabbitMQ Topology

Use a topic exchange:

```text
ai-career.events
```

Recommended dead-letter exchange:

```text
ai-career.dlx
```

Recommended retry exchange:

```text
ai-career.retry
```

---

# 6. Exchanges

Create:

```text
ai-career.events
ai-career.commands
ai-career.retry
ai-career.dlx
```

Events and commands should remain conceptually separate.

---

# 7. Event Routing Keys

Use hierarchical routing keys.

Format:

```text
<domain>.<entity>.<event>
```

Examples:

```text
application.created
application.qualified
application.prepared
application.approved
application.execution_started
application.submitted
application.failed

communication.created
communication.approved
communication.queued
communication.sent
communication.failed

followup.created
followup.scheduled
followup.sent
followup.cancelled

interview.detected
application.outcome_recorded
```

---

# 8. Event Envelope

Every event must use a common envelope.

Example:

```json
{
  "eventId": "uuid",
  "eventType": "application.approved",
  "eventVersion": 1,
  "occurredAt": "2026-08-09T18:30:00Z",
  "producer": "application-service",
  "correlationId": "uuid",
  "causationId": "uuid",
  "userId": "uuid",
  "aggregateType": "APPLICATION",
  "aggregateId": "uuid",
  "payload": {}
}
```

---

# 9. Event ID

Every event must have a globally unique:

```text
eventId
```

Use UUID.

Never reuse event IDs.

---

# 10. Event Version

Every event must contain:

```text
eventVersion
```

Start with:

```text
1
```

When the event contract changes incompatibly:

```text
2
```

Do not silently change version 1.

---

# 11. occurredAt

The timestamp at which the event occurred.

Use UTC.

Format:

```text
ISO-8601
```

---

# 12. Producer

Identifies the service/module that produced the event.

Examples:

```text
job-service
application-service
automation-service
communication-service
followup-service
notification-service
analytics-service
```

---

# 13. correlationId

Used to trace a complete workflow.

Example:

```text
JOB MATCH
   ↓
APPLICATION
   ↓
APPROVAL
   ↓
EXECUTION
   ↓
SUBMISSION
   ↓
FOLLOW-UP
```

All related events should share the same correlation ID where appropriate.

---

# 14. causationId

Identifies the event or command that directly caused the current event.

Example:

```text
application.approved
```

causes:

```text
application.execution_requested
```

The second event contains:

```text
causationId = application.approved.eventId
```

---

# 15. aggregateType

Examples:

```text
JOB
APPLICATION
COMMUNICATION
FOLLOWUP
INTERVIEW
USER
```

---

# 16. aggregateId

The ID of the primary domain object.

For example:

```text
aggregateType = APPLICATION
aggregateId = application UUID
```

---

# 17. Event Payload Rules

Payloads must contain business facts required by consumers.

Do not put:

* Passwords
* JWT tokens
* API keys
* Provider secrets
* Large binary documents
* Complete PDF/DOCX files

inside events.

Use IDs or object-storage references instead.

---

# 18. Event Size

Keep events small.

Target:

```text
< 64 KB
```

Preferred:

```text
< 16 KB
```

Large content should live in MinIO.

---

# 19. Job Events

Phase 1 already contains job ingestion and matching.

Phase 3 should integrate with those events without breaking Phase 1.

Relevant events:

```text
job.discovered
job.created
job.updated
job.matched
```

---

# 20. job.discovered

Published when a new job is discovered.

Example:

```json
{
  "jobId": "uuid",
  "source": "JOOBLE",
  "externalJobId": "12345",
  "title": "Software Engineer",
  "company": "Example Corp"
}
```

Consumers may include:

```text
matching
notification
analytics
application-eligibility
```

---

# 21. job.matched

Published when a job receives a candidate match.

Example:

```json
{
  "jobId": "uuid",
  "userId": "uuid",
  "matchId": "uuid",
  "matchScore": 91.5,
  "recommendation": "STRONG_APPLY"
}
```

---

# 22. Application Lifecycle Events

Core lifecycle:

```text
application.created
application.qualified
application.preparation_started
application.prepared
application.review_required
application.approved
application.rejected
application.execution_requested
application.execution_started
application.submission_requires_review
application.submitted
application.verification_started
application.verified
application.failed
application.cancelled
application.closed
```

---

# 23. application.created

Published when an application record is created.

Payload:

```json
{
  "applicationId": "uuid",
  "userId": "uuid",
  "jobId": "uuid",
  "matchScore": 91.5,
  "recommendation": "STRONG_APPLY"
}
```

Consumers:

```text
automation
notification
analytics
workspace
```

---

# 24. application.qualified

Published when the application passes eligibility rules.

Payload:

```json
{
  "applicationId": "uuid",
  "userId": "uuid",
  "jobId": "uuid",
  "matchScore": 91.5,
  "atsScore": 87.0,
  "ruleId": "uuid"
}
```

---

# 25. application.preparation_started

Indicates that AI preparation has started.

Consumers:

```text
AI pipeline
notification
workspace
analytics
```

---

# 26. application.prepared

Indicates that required application artifacts are ready.

Payload:

```json
{
  "applicationId": "uuid",
  "resumeVersionId": "uuid",
  "coverLetterVersionId": "uuid",
  "questionCount": 8,
  "answeredQuestionCount": 8,
  "atsScore": 92.0
}
```

---

# 27. application.review_required

Published when human approval is required.

Example reasons:

```text
approval policy
unknown application question
sensitive question
low AI confidence
unsupported field
captcha
MFA
unknown provider
```

---

# 28. application.approved

Payload:

```json
{
  "applicationId": "uuid",
  "approvalId": "uuid",
  "approvedBy": "uuid",
  "approvedAt": "2026-08-09T18:30:00Z"
}
```

Consumers:

```text
application-execution
notification
analytics
```

---

# 29. application.rejected

Payload:

```json
{
  "applicationId": "uuid",
  "approvalId": "uuid",
  "reason": "User rejected application"
}
```

---

# 30. application.execution_requested

This is a command-like event indicating that execution should occur.

Routing:

```text
application.execution_requested
```

Consumer:

```text
application-execution-worker
```

---

# 31. application.execution_started

Payload:

```json
{
  "applicationId": "uuid",
  "executionId": "uuid",
  "provider": "GREENHOUSE",
  "attempt": 1
}
```

---

# 32. application.submission_requires_review

This is one of the most important Phase 3 events.

It means automation cannot safely continue.

Reasons may include:

```text
CAPTCHA
MFA
UNKNOWN_QUESTION
SENSITIVE_QUESTION
FILE_UPLOAD_FAILURE
PROVIDER_ERROR
UNSUPPORTED_FIELD
LOGIN_REQUIRED
```

The system must stop.

It must NOT blindly retry.

---

# 33. application.submitted

Payload:

```json
{
  "applicationId": "uuid",
  "executionId": "uuid",
  "submittedAt": "2026-08-09T18:30:00Z",
  "providerApplicationId": "ABC-123"
}
```

Consumers:

```text
followup
notification
analytics
communication
```

---

# 34. application.verification_started

Published after submission when verification is required.

---

# 35. application.verified

Payload:

```json
{
  "applicationId": "uuid",
  "executionId": "uuid",
  "verified": true,
  "verificationMethod": "PROVIDER_CONFIRMATION"
}
```

---

# 36. application.failed

Payload:

```json
{
  "applicationId": "uuid",
  "executionId": "uuid",
  "errorCode": "PROVIDER_TIMEOUT",
  "retryable": true
}
```

Consumers decide whether retry is appropriate.

---

# 37. Communication Events

Use:

```text
communication.created
communication.review_required
communication.approved
communication.queued
communication.sending
communication.sent
communication.delivered
communication.failed
communication.received
communication.replied
```

---

# 38. communication.created

Created when AI generates communication content.

Payload:

```json
{
  "communicationId": "uuid",
  "applicationId": "uuid",
  "type": "INITIAL_OUTREACH",
  "channel": "EMAIL"
}
```

---

# 39. communication.review_required

Used when human approval is required before sending.

---

# 40. communication.approved

Indicates the user approved the communication.

Consumer:

```text
communication sender
```

---

# 41. communication.queued

Indicates communication has entered the sending queue.

---

# 42. communication.sent

Payload:

```json
{
  "communicationId": "uuid",
  "provider": "SMTP",
  "providerMessageId": "message-id",
  "sentAt": "2026-08-09T18:30:00Z"
}
```

---

# 43. communication.failed

Payload:

```json
{
  "communicationId": "uuid",
  "errorCode": "SMTP_FAILURE",
  "retryable": true
}
```

---

# 44. communication.received

Used when an inbound recruiter communication is detected.

Payload:

```json
{
  "communicationId": "uuid",
  "applicationId": "uuid",
  "channel": "EMAIL",
  "providerMessageId": "message-id"
}
```

---

# 45. communication.replied

Used when an inbound response is classified as a reply to an existing outreach.

---

# 46. Follow-Up Events

Use:

```text
followup.created
followup.scheduled
followup.ready
followup.sent
followup.failed
followup.cancelled
followup.skipped
```

---

# 47. followup.created

Payload:

```json
{
  "followupId": "uuid",
  "applicationId": "uuid",
  "sequence": 1
}
```

---

# 48. followup.scheduled

Payload:

```json
{
  "followupId": "uuid",
  "applicationId": "uuid",
  "scheduledAt": "2026-08-12T09:00:00Z"
}
```

---

# 49. followup.ready

Indicates the scheduled time has arrived and eligibility checks should run.

---

# 50. followup.sent

Payload:

```json
{
  "followupId": "uuid",
  "communicationId": "uuid",
  "sentAt": "2026-08-12T09:00:00Z"
}
```

---

# 51. followup.cancelled

Follow-up must be cancelled when appropriate.

Examples:

```text
Recruiter replied
Interview scheduled
Application rejected
User cancelled application
Offer received
```

---

# 52. Interview Events

Use:

```text
interview.detected
interview.scheduled
interview.rescheduled
interview.completed
```

---

# 53. interview.detected

Published when AI or an integration detects evidence of an interview.

Payload:

```json
{
  "applicationId": "uuid",
  "sourceCommunicationId": "uuid",
  "confidence": 0.94,
  "detectedType": "HR_INTERVIEW"
}
```

AI detection must not automatically mark an interview as confirmed unless configured to do so.

---

# 54. application.outcome_recorded

Payload:

```json
{
  "applicationId": "uuid",
  "outcomeId": "uuid",
  "outcomeType": "INTERVIEW",
  "source": "EMAIL"
}
```

---

# 55. Notification Events

Existing Telegram notification support must remain compatible.

Recommended events:

```text
notification.requested
notification.sent
notification.failed
```

Notifications may be consumed from application events.

---

# 56. Analytics Events

Analytics should consume domain events instead of querying every operational table for every metric.

Examples:

```text
application.created
application.approved
application.submitted
application.failed
communication.sent
communication.replied
interview.detected
application.outcome_recorded
```

---

# 57. Event Consumers

Recommended consumers:

```text
Application Event Consumer
Automation Consumer
Communication Consumer
Follow-Up Consumer
Notification Consumer
Analytics Consumer
Workspace Consumer
```

A consumer may subscribe to multiple routing keys.

---

# 58. Queue Strategy

Recommended queues:

```text
application.execution.queue
communication.queue
followup.queue
notification.queue
analytics.queue
workspace.queue
```

Retry queues:

```text
application.execution.retry.queue
communication.retry.queue
followup.retry.queue
notification.retry.queue
```

Dead-letter queues:

```text
application.execution.dlq
communication.dlq
followup.dlq
notification.dlq
analytics.dlq
```

---

# 59. Queue Naming Rule

Use:

```text
<domain>.<purpose>.queue
```

Examples:

```text
application.execution.queue
communication.send.queue
followup.process.queue
```

Keep naming consistent.

---

# 60. Message Acknowledgement

Consumers must acknowledge a message only after successful processing.

Do not acknowledge immediately after receiving.

Workflow:

```text
Receive
 ↓
Validate
 ↓
Process
 ↓
Persist
 ↓
Publish resulting event
 ↓
ACK
```

---

# 61. Failure Handling

If processing fails:

```text
Message
 ↓
Retry
 ↓
Retry
 ↓
Retry
 ↓
DLQ
```

Do not retry forever.

---

# 62. Retry Policy

Recommended:

```text
Attempt 1 → immediate
Attempt 2 → 30 seconds
Attempt 3 → 2 minutes
Attempt 4 → 10 minutes
Attempt 5 → DLQ
```

Exact values may be configurable.

---

# 63. Retryable Errors

Examples:

```text
NETWORK_TIMEOUT
PROVIDER_5XX
TEMPORARY_DATABASE_FAILURE
RABBITMQ_CONNECTION_FAILURE
RATE_LIMIT
```

---

# 64. Non-Retryable Errors

Examples:

```text
INVALID_REQUEST
INVALID_APPLICATION_STATE
INVALID_CREDENTIALS
UNSUPPORTED_FIELD
USER_REJECTED
CAPTCHA_REQUIRED
MFA_REQUIRED
```

These should normally become:

```text
ACTION_REQUIRED
```

rather than endlessly retrying.

---

# 65. Dead Letter Handling

Every DLQ message must contain enough metadata to diagnose:

```text
eventId
eventType
applicationId
userId
correlationId
attemptCount
errorCode
errorMessage
occurredAt
```

---

# 66. Consumer Idempotency

Consumers must assume messages can be delivered more than once.

Never assume exactly-once delivery.

Recommended strategy:

```text
eventId
+
consumerName
```

Use an idempotency mechanism.

---

# 67. Duplicate Message Example

Message:

```text
application.submitted
eventId = ABC
```

received twice.

First:

```text
process
store event ID
ACK
```

Second:

```text
event already processed
ACK
```

Do not perform the business action twice.

---

# 68. Transactional Event Publishing

Never do:

```text
UPDATE database

publish RabbitMQ message
```

without transactional protection.

Use:

```text
Database transaction
      ↓
Business update
      ↓
Outbox insert
      ↓
Commit
      ↓
Outbox publisher
      ↓
RabbitMQ
```

---

# 69. Outbox Pattern

The Phase 3 `outbox_events` table is mandatory for critical domain events.

At minimum:

```text
application.*
communication.*
followup.*
interview.*
application.outcome_recorded
```

must use the outbox pattern.

---

# 70. Outbox Publisher

The publisher:

1. Reads pending events.
2. Locks a batch.
3. Publishes to RabbitMQ.
4. Marks event as published.
5. Records failure if publication fails.

---

# 71. Outbox Concurrency

Multiple publisher instances must be supported safely.

Use appropriate MySQL row locking.

For example:

```text
SELECT ... FOR UPDATE SKIP LOCKED
```

where compatible with the project's MySQL version and transaction strategy.

---

# 72. Event Ordering

RabbitMQ does not guarantee global event ordering.

The system must not depend on global ordering.

Ordering may be required per application.

Use:

```text
aggregateId
sequence
```

when ordering matters.

---

# 73. Aggregate Sequence

Important application events may include:

```json
{
  "aggregateSequence": 17
}
```

Consumers can detect:

```text
expected sequence
received sequence
```

where strict ordering is required.

---

# 74. Event Contract Validation

Every incoming event must validate:

* eventId
* eventType
* eventVersion
* occurredAt
* aggregateType
* aggregateId
* correlationId
* payload

Invalid events must not enter business processing.

---

# 75. Unknown Event Versions

If a consumer receives an unsupported version:

```text
eventVersion = 99
```

do not silently process it.

Route to an appropriate compatibility/error path.

---

# 76. Schema Evolution

Events must be backward compatible where possible.

Prefer:

```text
add optional field
```

instead of:

```text
rename required field
```

For breaking changes:

```text
eventVersion 2
```

---

# 77. Sensitive Data

Never publish:

```text
password
JWT
API key
OAuth refresh token
SMTP password
provider credentials
```

Never place secrets inside:

```text
payload
logs
RabbitMQ message headers
```

---

# 78. Resume and Document Events

Do not send the actual resume PDF through RabbitMQ.

Use:

```json
{
  "resumeVersionId": "uuid",
  "storageReference": "minio-object-key"
}
```

Consumers retrieve the document when necessary.

---

# 79. Browser Automation Events

Browser automation must never be triggered merely because an application record exists.

Required chain:

```text
application.approved
        ↓
execution request
        ↓
application.execution_started
```

Only then may the browser worker execute.

---

# 80. Human Intervention

When automation encounters:

```text
CAPTCHA
MFA
Unknown question
Sensitive question
Unsupported form
Login required
```

publish:

```text
application.submission_requires_review
```

and stop execution.

Do not:

```text
retry blindly
```

Do not:

```text
guess
```

Do not:

```text
circumvent security controls
```

---

# 81. Application Execution State Flow

```text
APPROVED
   │
   ▼
EXECUTION_REQUESTED
   │
   ▼
EXECUTION_STARTED
   │
   ├───────────────┐
   │               │
   ▼               ▼
SUBMITTED     REVIEW_REQUIRED
   │               │
   ▼               ▼
VERIFICATION    ACTION_REQUIRED
   │
   ▼
VERIFIED
```

---

# 82. Communication Flow

```text
AI GENERATED
      ↓
CREATED
      ↓
REVIEW_REQUIRED
      ↓
APPROVED
      ↓
QUEUED
      ↓
SENDING
      ↓
SENT
      ↓
DELIVERED
      ↓
REPLIED
```

---

# 83. Follow-Up Flow

```text
APPLICATION_SUBMITTED
        ↓
FOLLOWUP_CREATED
        ↓
FOLLOWUP_SCHEDULED
        ↓
FOLLOWUP_READY
        ↓
Eligibility Check
        │
   ┌────┴────┐
   │         │
   ▼         ▼
SEND       CANCEL
   │
   ▼
FOLLOWUP_SENT
```

---

# 84. Follow-Up Eligibility

Before sending a follow-up, verify:

```text
Application still active?
Recruiter replied?
Interview scheduled?
Offer received?
Application rejected?
User disabled automation?
Daily sending limit reached?
Communication approval required?
```

If any stop condition exists:

```text
followup.cancelled
```

---

# 85. Rate Limiting Events

The communication system must respect configured limits.

Examples:

```text
max emails/day
max applications/day
max follow-ups/day
```

Rate limiting should not rely solely on RabbitMQ.

Persist relevant counters or derive them from MySQL.

Redis may be used for short-lived coordination.

---

# 86. Notification Strategy

Users should receive notifications for important milestones.

Examples:

```text
High-match job found
Application ready for review
Application approved
Application submitted
Application requires manual action
Recruiter replied
Interview detected
Offer detected
Automation failed
```

---

# 87. Telegram Integration

Existing Telegram notifications remain supported.

Telegram should consume events.

Example:

```text
application.submitted
       ↓
notification consumer
       ↓
Telegram
```

Do not make Telegram part of the core application transaction.

---

# 88. AI Event Handling

AI-generated events must clearly identify AI involvement.

Example:

```json
{
  "producer": "ai-orchestrator",
  "payload": {
    "generatedBy": "OLLAMA",
    "model": "local-model"
  }
}
```

AI should never silently mutate critical state.

---

# 89. AI Confidence

Where AI makes a classification, include:

```text
confidence
```

when available.

Example:

```json
{
  "confidence": 0.91
}
```

Low confidence should trigger review where configured.

---

# 90. Event Auditability

For every critical workflow:

```text
What happened?
When?
Why?
Which application?
Which user?
Which worker?
Which event caused it?
Which event did it create?
```

must be answerable.

---

# 91. Correlation Example

Example:

```text
correlationId = C-123
```

Events:

```text
job.matched
application.created
application.prepared
application.approved
application.execution_started
application.submitted
followup.scheduled
communication.sent
```

All can be traced using:

```text
C-123
```

---

# 92. Observability

Every event-processing log should include:

```text
eventId
eventType
eventVersion
aggregateId
correlationId
consumer
attempt
```

Example:

```text
eventType=application.submitted
applicationId=ABC
correlationId=XYZ
consumer=followup-service
attempt=1
```

---

# 93. Metrics

Track:

```text
events_published_total
events_consumed_total
events_failed_total
events_retried_total
events_dlq_total

application_execution_success_total
application_execution_failure_total

communication_sent_total
communication_failure_total

followup_sent_total
followup_cancelled_total

event_processing_latency
```

---

# 94. Health Checks

Application health should verify:

```text
MySQL
RabbitMQ
Redis
Ollama
MinIO
```

where applicable.

RabbitMQ consumer health should be visible.

---

# 95. Event Security

RabbitMQ must not be publicly exposed.

Use:

```text
authenticated RabbitMQ connection
```

and environment variables/secrets for credentials.

Never commit RabbitMQ credentials to Git.

---

# 96. Event Testing

Every event must have tests for:

## Serialization

```text
Java object → JSON → Java object
```

## Validation

Missing required fields must fail.

## Versioning

Version 1 must deserialize correctly.

## Idempotency

Duplicate events must not duplicate business actions.

## Retry

Retryable exceptions must retry.

## DLQ

Non-retryable/final failures must reach DLQ.

---

# 97. Integration Testing

Test complete workflows.

Example:

```text
job.matched
      ↓
application.created
      ↓
application.qualified
      ↓
application.prepared
      ↓
application.approved
      ↓
execution
      ↓
application.submitted
      ↓
followup.scheduled
```

Verify:

* Database state
* Event publication
* Event consumption
* Idempotency
* Notifications

---

# 98. Failure Integration Test

Simulate:

```text
provider timeout
```

Expected:

```text
execution FAILED
       ↓
application FAILED or retryable state
       ↓
retry
       ↓
eventually DLQ
```

No duplicate application should be created.

---

# 99. Human Intervention Integration Test

Simulate:

```text
CAPTCHA encountered
```

Expected:

```text
execution → REQUIRES_REVIEW

application → SUBMISSION_REQUIRES_REVIEW

notification → USER

browser automation → STOP
```

The system must not continue automatically.

---

# 100. Duplicate Execution Test

Send:

```text
application.execution_requested
```

twice.

Expected:

```text
Only one execution should actually occur.
```

---

# 101. Duplicate Communication Test

Send:

```text
communication.approved
```

twice.

Expected:

```text
Only one send operation.
```

---

# 102. Duplicate Follow-Up Test

Send:

```text
followup.ready
```

twice.

Expected:

```text
Only one follow-up communication.
```

---

# 103. Event Ordering Test

Send:

```text
application.submitted
```

before:

```text
application.approved
```

The consumer must validate application state.

Do not blindly trust event ordering.

---

# 104. Event Storage

The primary event audit record is:

```text
outbox_events
```

For consumer-specific processing state, use an appropriate idempotency mechanism.

Do not create a massive event store unless the product actually requires one.

---

# 105. RabbitMQ Is Not the Source of Truth

Important:

```text
RabbitMQ = transport
MySQL = business truth
```

If RabbitMQ is temporarily unavailable:

```text
business transaction still succeeds
```

and the outbox publisher eventually sends the event.

---

# 106. Eventual Consistency

Phase 3 must explicitly support eventual consistency.

Example:

```text
Application submitted
       ↓
MySQL updated immediately
       ↓
Outbox event
       ↓
RabbitMQ
       ↓
Notification
```

Notification may happen milliseconds or seconds later.

That is acceptable.

---

# 107. Eventual Consistency Rule

Never make critical business correctness dependent on immediate event consumption.

For example:

Do not consider an application "submitted" only because a notification was sent.

The application database state is authoritative.

---

# 108. Event Naming Rules

Use lowercase dot-separated names:

```text
application.approved
application.submitted
communication.sent
followup.scheduled
```

Do not use:

```text
ApplicationApproved
APPLICATION_APPROVED
application_approved
```

---

# 109. Command Naming Rules

Commands may use:

```text
application.execute
application.verify
communication.send
followup.process
```

Commands are requests.

Events are facts.

---

# 110. Minimum Phase 3 Event Set

Implementation must support at least:

```text
job.matched

application.created
application.qualified
application.preparation_started
application.prepared
application.review_required
application.approved
application.rejected
application.execution_requested
application.execution_started
application.submission_requires_review
application.submitted
application.verification_started
application.verified
application.failed

communication.created
communication.review_required
communication.approved
communication.queued
communication.sent
communication.failed
communication.received
communication.replied

followup.created
followup.scheduled
followup.ready
followup.sent
followup.failed
followup.cancelled

interview.detected
application.outcome_recorded

notification.requested
notification.sent
notification.failed
```

---

# 111. Definition of Done

Events implementation is complete when:

* RabbitMQ topology exists.
* Event exchange exists.
* Command exchange exists.
* Retry infrastructure exists.
* DLQ infrastructure exists.
* Common event envelope exists.
* Event versioning exists.
* Correlation IDs exist.
* Causation IDs exist.
* Outbox publishing exists.
* Consumer idempotency exists.
* Retry policy exists.
* DLQ handling exists.
* Application lifecycle events exist.
* Communication events exist.
* Follow-up events exist.
* Interview/outcome events exist.
* Notification events integrate with existing Telegram.
* Existing Phase 1 RabbitMQ functionality still works.
* Phase 2 functionality remains compatible.
* Integration tests pass.
* Duplicate delivery tests pass.
* Failure/retry tests pass.
* Human-intervention tests pass.

---

# 112. Golden Rule

The event system must make the application workflow:

```text
observable
recoverable
idempotent
auditable
asynchronous
fault tolerant
```

The system must never depend on:

```text
exactly-once delivery
perfect network connectivity
perfect external providers
AI always being correct
browser automation always succeeding
```

---

# 113. Final Architecture

```text
                         AI CAREER OS
                              │
                              ▼
                       ┌─────────────┐
                       │    MySQL    │
                       │   SOURCE    │
                       │   OF TRUTH  │
                       └──────┬──────┘
                              │
                         OUTBOX EVENT
                              │
                              ▼
                       ┌─────────────┐
                       │  RabbitMQ   │
                       │ EVENT BUS   │
                       └──────┬──────┘
                              │
          ┌───────────────────┼───────────────────┐
          │                   │                   │
          ▼                   ▼                   ▼
     APPLICATION          COMMUNICATION       FOLLOW-UP
       WORKER               WORKER              WORKER
          │                   │                   │
          ▼                   ▼                   ▼
      BROWSER/API           EMAIL             SCHEDULER
          │                   │                   │
          └───────────────────┼───────────────────┘
                              │
                              ▼
                         RESULT EVENT
                              │
                ┌─────────────┼─────────────┐
                ▼             ▼             ▼
           NOTIFICATION    ANALYTICS     WORKSPACE
                │             │             │
                ▼             ▼             ▼
             TELEGRAM       METRICS       DASHBOARD
```

---

# 114. Final Safety Boundary

The automation system must never attempt to bypass:

* CAPTCHA
* MFA
* Authentication challenges
* Security controls
* Anti-bot protections
* Explicit provider restrictions

When encountered:

```text
STOP
 ↓
PERSIST STATE
 ↓
CREATE ACTION_REQUIRED
 ↓
NOTIFY USER
 ↓
WAIT FOR HUMAN INTERVENTION
```

This is a mandatory Phase 3 requirement.

---

# END OF EVENTS SPECIFICATION

````

