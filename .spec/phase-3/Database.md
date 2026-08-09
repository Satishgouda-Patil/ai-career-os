# AI Career OS — Phase 3

# Database Specification

## Application Automation Engine

Version: 3.0

Status: Specification

---

# 1. Purpose

This document defines the MySQL database architecture for Phase 3.

Phase 3 introduces the complete application lifecycle:

```text
JOB
 ↓
APPLICATION
 ↓
PREPARATION
 ↓
APPROVAL
 ↓
EXECUTION
 ↓
SUBMISSION
 ↓
COMMUNICATION
 ↓
FOLLOW-UP
 ↓
RESPONSE
 ↓
INTERVIEW
 ↓
OUTCOME
````

The database must provide:

* Strong relational integrity
* Complete application history
* State transition auditing
* Approval auditing
* Automation execution tracking
* Communication tracking
* Follow-up scheduling
* Application question storage
* Candidate answer storage
* Outcome tracking
* Idempotency
* Auditability
* Safe concurrency
* Future analytics

MySQL remains the **source of truth**.

Redis, RabbitMQ and MinIO must not become authoritative business data stores.

---

# 2. Database Technology

Use:

```text
MySQL 8
Flyway
Spring Data JPA
Hibernate
```

Existing Phase 1 and Phase 2 conventions must be preserved.

Do not replace the existing database architecture.

---

# 3. Database Design Principles

## Principle 1 — Relational Source of Truth

All important business state must be persisted in MySQL.

---

## Principle 2 — Immutable History

Application events and important execution records should be append-only.

Do not overwrite historical facts.

---

## Principle 3 — Versioned Artifacts

Applications must reference exact resume and cover-letter versions used during execution.

---

## Principle 4 — User Isolation

Every user-owned entity must be traceable to a user.

---

## Principle 5 — Idempotency

Critical operations must be protected against duplicate execution.

---

## Principle 6 — State Integrity

Application status changes must happen through validated domain transitions.

---

# 4. Existing Tables

Phase 1 and Phase 2 already contain tables for concepts such as:

* users
* profiles
* skills
* jobs
* job_matches
* notifications
* resume artifacts/versions
* cover letters
* recruiters
* AI workspace data

Do not recreate existing tables.

Before implementation, inspect the current Flyway migrations and adapt the Phase 3 schema to the actual existing names and columns.

If an existing Phase 2 table already represents a required concept, extend it instead of creating a duplicate concept.

---

# 5. New Phase 3 Tables

The minimum Phase 3 schema introduces:

```text
applications
application_state_history
application_approvals
application_executions
application_questions
candidate_answers
application_answers
communications
communication_events
followups
application_outcomes
automation_rules
scheduled_actions
outbox_events
idempotency_records
```

Optional supporting tables may be added when required by implementation.

---

# 6. Entity Relationship Overview

```text
users
  │
  ├──────────────┐
  │              │
  ▼              ▼
jobs        applications
               │
      ┌────────┼────────┬─────────────┐
      │        │        │             │
      ▼        ▼        ▼             ▼
 approvals   history  executions   questions
                                      │
                                      ▼
                                application_answers
                                      │
                                      ▼
                                candidate_answers

applications
      │
      ├──────────────► communications
      │                    │
      │                    ▼
      │             communication_events
      │
      ├──────────────► followups
      │
      ├──────────────► scheduled_actions
      │
      └──────────────► application_outcomes

automation_rules
      │
      ▼
scheduled_actions

outbox_events
      │
      ▼
RabbitMQ
```

---

# 7. Primary Table — applications

This is the central Phase 3 table.

Suggested columns:

```text
applications

id
user_id
job_id

status
application_method

match_score
ats_score
recommendation

resume_version_id
cover_letter_version_id

application_url

provider_name
provider_application_id

automation_level

approval_required

created_at
updated_at

started_at
submitted_at
verified_at
closed_at

version
```

---

# 8. applications.id

Use UUID.

Recommended:

```text
CHAR(36)
```

or the project's existing UUID strategy.

The project must use one consistent identifier strategy.

Do not introduce multiple ID strategies without a strong reason.

---

# 9. applications.user_id

Foreign key to the existing users table.

Required.

Every application must belong to exactly one user.

---

# 10. applications.job_id

Foreign key to the existing jobs table.

Required.

An application represents a candidate applying to a specific job.

---

# 11. applications.status

Use an enum represented safely in MySQL.

Suggested states:

```text
DISCOVERED
QUALIFIED
PREPARING
READY_FOR_REVIEW
APPROVED
APPLYING
SUBMISSION_REQUIRES_REVIEW
APPLIED
FOLLOW_UP_PENDING
FOLLOWED_UP
RESPONDED
INTERVIEW
OFFER
REJECTED
WITHDRAWN
NO_RESPONSE
CLOSED
FAILED
ACTION_REQUIRED
```

The exact Java enum must match the state machine specification.

---

# 12. Application State Rules

The database should not be responsible for implementing all business transition rules.

State transition validation belongs to the domain/service layer.

The database must provide:

* valid storage
* indexing
* optimistic locking
* history

---

# 13. applications.application_method

Suggested values:

```text
DIRECT
BROWSER
API
MANUAL
UNKNOWN
```

This records how the application was submitted.

---

# 14. applications.match_score

Store the match score used when the application was created.

Recommended:

```text
DECIMAL(5,2)
```

Range:

```text
0.00 — 100.00
```

Do not rely only on the current job match record.

The application must preserve the score used for its decision.

---

# 15. applications.ats_score

Store the ATS score associated with the exact application artifact.

Recommended:

```text
DECIMAL(5,2)
```

Nullable.

---

# 16. applications.recommendation

Suggested values:

```text
STRONG_APPLY
APPLY
REVIEW
DO_NOT_APPLY
```

The actual enum may follow the Phase 2 implementation if one already exists.

---

# 17. Artifact References

The application must reference the exact artifact versions used.

Examples:

```text
resume_version_id
cover_letter_version_id
```

These should point to existing Phase 2 versioned artifact records where possible.

Do not duplicate the actual document binary into MySQL.

---

# 18. Application URL

Store:

```text
application_url
```

This is the URL used to access the application.

Do not assume the job URL and application URL are always identical.

---

# 19. Provider Information

Store:

```text
provider_name
provider_application_id
```

Examples:

```text
DIRECT
GREENHOUSE
LEVER
CUSTOM
MANUAL
```

Do not tightly couple the database schema to one external platform.

---

# 20. Automation Level

Suggested values:

```text
LEVEL_0
LEVEL_1
LEVEL_2
LEVEL_3
```

The actual enum must match the application configuration model.

---

# 21. Approval Requirement

Store:

```text
approval_required BOOLEAN
```

This allows the application to preserve the decision policy used at creation time.

---

# 22. Timestamps

Applications should contain:

```text
created_at
updated_at
started_at
submitted_at
verified_at
closed_at
```

All timestamps should use UTC at the persistence layer.

The user's timezone should be handled at the application layer.

---

# 23. Optimistic Locking

Use:

```text
version BIGINT
```

for JPA optimistic locking.

This prevents conflicting updates.

---

# 24. Application Unique Constraint

The system must prevent duplicate active applications.

Recommended logical constraint:

```text
(user_id, job_id)
```

However, if historical re-application is supported, do not blindly enforce a permanent unique constraint.

Instead, implement an active-application uniqueness strategy.

Recommended:

```text
One active application per user + job.
```

The exact implementation may use:

* application-level validation
* unique active marker
* generated column
* dedicated constraint strategy

The implementation must be compatible with MySQL 8.

---

# 25. application_state_history

Purpose:

Record every application status transition.

Columns:

```text
id
application_id

from_status
to_status

reason
trigger_type

actor_type
actor_id

correlation_id

created_at
```

---

# 26. State History Example

```text
READY_FOR_REVIEW
        ↓
APPROVED
```

Record:

```text
from_status = READY_FOR_REVIEW
to_status   = APPROVED
actor_type  = USER
reason      = "User approved application"
```

---

# 27. trigger_type

Suggested:

```text
USER
AI
SYSTEM
AUTOMATION
PROVIDER
SCHEDULER
```

---

# 28. actor_type

Suggested:

```text
USER
SYSTEM
AI
PROVIDER
```

---

# 29. application_approvals

Purpose:

Track human approval decisions.

Columns:

```text
id
application_id

status

requested_at
responded_at

requested_by
responded_by

comments

version

created_at
updated_at
```

---

# 30. Approval Status

Suggested:

```text
PENDING
APPROVED
REJECTED
CANCELLED
EXPIRED
```

---

# 31. Approval History

Do not delete approval records.

Every approval request and decision must remain auditable.

If multiple approval cycles occur:

```text
Approval #1 → REJECTED

Regenerated

Approval #2 → APPROVED
```

both records must remain.

---

# 32. application_executions

Purpose:

Represent one attempt to execute an application.

Columns:

```text
id
application_id

provider_name
execution_type

status

idempotency_key

started_at
completed_at

attempt_number

correlation_id

error_code
error_message

result_reference

created_at
updated_at
```

---

# 33. Execution Status

Suggested:

```text
CREATED
QUEUED
RUNNING
PAUSED
SUCCESS
FAILED
REQUIRES_REVIEW
CANCELLED
```

---

# 34. execution_type

Suggested:

```text
PREPARE
SUBMIT
VERIFY
RETRY
MANUAL_CONTINUATION
```

---

# 35. Execution Idempotency

Create a unique index on:

```text
idempotency_key
```

No two successful executions may use the same idempotency key.

---

# 36. application_questions

Purpose:

Store questions encountered during an application.

Columns:

```text
id
application_id

external_field_id

question_text
normalized_question

field_type

required

options_json

sequence_number

status

created_at
updated_at
```

---

# 37. Question Field Types

Suggested:

```text
TEXT
TEXTAREA
NUMBER
BOOLEAN
SELECT
MULTI_SELECT
RADIO
CHECKBOX
DATE
FILE
UNKNOWN
```

---

# 38. Question Status

Suggested:

```text
UNANSWERED
ANSWERED
SKIPPED
REQUIRES_REVIEW
```

---

# 39. candidate_answers

Purpose:

Reusable approved candidate answers.

Columns:

```text
id
user_id

question_pattern
normalized_question

answer_text

answer_type

source

approval_status

confidence_score

last_used_at

created_at
updated_at
```

---

# 40. Answer Source

Suggested:

```text
USER
PROFILE
APPROVED_AI
IMPORTED
SYSTEM
```

---

# 41. Answer Approval

Suggested:

```text
PENDING
APPROVED
REJECTED
```

AI-generated answers must not automatically become trusted candidate facts.

---

# 42. application_answers

Purpose:

Create an immutable application-specific snapshot of the answer used.

Columns:

```text
id
application_id
question_id
candidate_answer_id

answer_text
answer_source

approved
created_at
```

---

# 43. Why application_answers Exists

Candidate answers may change later.

Example:

```text
Candidate Answer

"5 years"
```

Later:

```text
"6 years"
```

An old application must still preserve:

```text
"5 years"
```

Therefore application_answers stores the exact answer submitted or intended for submission.

---

# 44. communications

Purpose:

Store all communication related to an application.

Examples:

* Recruiter email
* Follow-up email
* LinkedIn draft
* Manual communication record

Columns:

```text
id
application_id

communication_type
channel
direction

recipient
sender

subject
body

status

provider_name
provider_message_id

scheduled_at
sent_at
delivered_at
responded_at

created_at
updated_at
```

---

# 45. communication_type

Suggested:

```text
INITIAL_OUTREACH
FOLLOW_UP
THANK_YOU
RESPONSE
OTHER
```

---

# 46. Communication Channel

Suggested:

```text
EMAIL
LINKEDIN
TELEGRAM
MANUAL
OTHER
```

---

# 47. Communication Direction

Suggested:

```text
OUTBOUND
INBOUND
```

---

# 48. Communication Status

Suggested:

```text
DRAFT
PENDING_APPROVAL
APPROVED
QUEUED
SENDING
SENT
DELIVERED
FAILED
CANCELLED
RECEIVED
```

---

# 49. communication_events

Purpose:

Store delivery and provider events.

Columns:

```text
id
communication_id

event_type

provider_event_id

event_data_json

occurred_at
created_at
```

---

# 50. Communication Events

Examples:

```text
QUEUED
SENT
DELIVERED
BOUNCED
FAILED
OPENED
REPLIED
```

Do not depend on provider-specific event names internally.

Normalize them where practical.

---

# 51. followups

Purpose:

Track follow-up strategy for an application.

Columns:

```text
id
application_id

sequence_number

status

scheduled_at
executed_at
cancelled_at

communication_id

rule_id

failure_reason

created_at
updated_at
```

---

# 52. Follow-Up Status

Suggested:

```text
SCHEDULED
READY
PROCESSING
SENT
FAILED
CANCELLED
SKIPPED
```

---

# 53. Follow-Up Uniqueness

Prevent duplicate follow-ups for:

```text
application_id + sequence_number
```

---

# 54. automation_rules

Purpose:

Store user-configured automation rules.

Columns:

```text
id
user_id

name

enabled

automation_level

minimum_match_score
minimum_ats_score

max_daily_applications
max_daily_emails
max_followups

followup_delay_days

require_approval

conditions_json

created_at
updated_at
```

---

# 55. Rule Design

Rules must be flexible enough to support:

```text
IF match_score >= 85
AND
location matches
AND
salary meets requirement
THEN
application eligible
```

Do not hard-code every future rule as a database column.

Use structured JSON for extensible conditions while keeping important queryable fields as normal columns.

---

# 56. scheduled_actions

Purpose:

Represent future actions.

Examples:

```text
APPLICATION_EXECUTION
EMAIL_SEND
FOLLOWUP_SEND
VERIFICATION
NOTIFICATION
```

Columns:

```text
id
user_id
application_id

action_type

status

scheduled_at

attempt_count

last_attempt_at

completed_at
cancelled_at

idempotency_key

error_code
error_message

created_at
updated_at
```

---

# 57. Scheduled Action Status

```text
SCHEDULED
PROCESSING
COMPLETED
FAILED
CANCELLED
```

---

# 58. scheduled_actions Indexes

Required indexes:

```text
(status, scheduled_at)

application_id

user_id

idempotency_key
```

The scheduler must efficiently find:

```text
WHERE status = 'SCHEDULED'
AND scheduled_at <= NOW()
```

---

# 59. application_outcomes

Purpose:

Record final or intermediate outcomes.

Columns:

```text
id
application_id

outcome_type

source

notes

occurred_at

created_at
updated_at
```

---

# 60. Outcome Types

Suggested:

```text
NO_RESPONSE
REJECTION
INTERVIEW
TECHNICAL_INTERVIEW
HR_INTERVIEW
OFFER
WITHDRAWN
HIRED
CLOSED
OTHER
```

---

# 61. Outcome Source

Suggested:

```text
USER
EMAIL
PROVIDER
SYSTEM
AI
MANUAL
```

AI must not claim an outcome without supporting evidence.

---

# 62. Outbox Table

Create:

```text
outbox_events
```

Purpose:

Guarantee reliable publication of domain events.

Columns:

```text
id

aggregate_type
aggregate_id

event_type
event_version

payload_json

status

attempt_count

available_at

published_at

last_error

created_at
updated_at
```

---

# 63. Outbox Status

```text
PENDING
PROCESSING
PUBLISHED
FAILED
```

---

# 64. Outbox Processing

Workflow:

```text
Database transaction
        ↓
Application update
        ↓
Outbox event inserted
        ↓
Commit
        ↓
Outbox publisher
        ↓
RabbitMQ
        ↓
Mark PUBLISHED
```

---

# 65. Outbox Indexes

Required:

```text
(status, available_at)

aggregate_id

created_at
```

---

# 66. Idempotency Table

Create:

```text
idempotency_records
```

Purpose:

Prevent duplicate critical operations.

Columns:

```text
id

idempotency_key

operation_type

user_id

application_id

status

response_code

response_payload_json

created_at
expires_at
```

---

# 67. Idempotency Status

```text
PROCESSING
COMPLETED
FAILED
```

---

# 68. Idempotency Constraints

Create a unique constraint on:

```text
idempotency_key
```

---

# 69. Foreign Keys

At minimum:

```text
applications.user_id
        → users.id

applications.job_id
        → jobs.id

application_state_history.application_id
        → applications.id

application_approvals.application_id
        → applications.id

application_executions.application_id
        → applications.id

application_questions.application_id
        → applications.id

candidate_answers.user_id
        → users.id

application_answers.application_id
        → applications.id

application_answers.question_id
        → application_questions.id

communications.application_id
        → applications.id

communication_events.communication_id
        → communications.id

followups.application_id
        → applications.id

application_outcomes.application_id
        → applications.id

automation_rules.user_id
        → users.id

scheduled_actions.user_id
        → users.id

scheduled_actions.application_id
        → applications.id

idempotency_records.user_id
        → users.id
```

---

# 70. Delete Strategy

Avoid cascading deletes for important audit records.

Preferred behavior:

```text
User deleted
    ↓
Business data retained/anonymized according to retention policy
```

Do not allow accidental deletion of:

* Application history
* Execution records
* Communication history
* Audit events

Use explicit deletion policies.

---

# 71. Soft Delete

Do not add `deleted_at` to every table automatically.

Use soft deletion only where business requirements justify it.

For example:

```text
automation_rules
```

may use:

```text
enabled = false
```

instead of deletion.

---

# 72. JSON Columns

Use JSON for:

* Provider metadata
* Dynamic application questions/options
* Automation rule conditions
* Provider result metadata
* Event payload
* Error details where schema varies

Do not put core relational business fields into JSON.

---

# 73. Indexing Strategy

Important indexes include:

## applications

```text
(user_id, status)

(user_id, job_id)

(status, updated_at)

(created_at)

(provider_name)
```

## application_state_history

```text
(application_id, created_at)
```

## application_executions

```text
(application_id, created_at)

(status, created_at)

(idempotency_key)
```

## application_questions

```text
(application_id, status)
```

## candidate_answers

```text
(user_id, approval_status)
```

## communications

```text
(application_id, created_at)

(status, scheduled_at)
```

## followups

```text
(application_id, status)

(status, scheduled_at)
```

## scheduled_actions

```text
(status, scheduled_at)

(application_id)

(user_id)
```

---

# 74. Data Types

Recommended conventions:

```text
UUID       → CHAR(36)
Timestamp  → DATETIME(6)
Boolean    → BOOLEAN
Score      → DECIMAL(5,2)
Money      → DECIMAL(12,2)
JSON       → JSON
Version    → BIGINT
Text       → TEXT
Short text → VARCHAR(...)
```

Follow existing project conventions where already established.

---

# 75. Timestamps

Store all server timestamps in UTC.

Recommended:

```text
DATETIME(6)
```

Application layer converts UTC into the user's configured timezone.

---

# 76. Audit Metadata

Where practical, records should contain:

```text
created_at
updated_at
```

Critical workflow records should additionally contain:

```text
correlation_id
```

or reference an execution/event containing it.

---

# 77. Correlation

The following IDs must be traceable:

```text
Application ID
Execution ID
Event ID
Correlation ID
Scheduled Action ID
Communication ID
```

This allows one complete workflow to be reconstructed.

---

# 78. Example Complete Record

An application might produce:

```text
applications
    APP-001

application_state_history
    DISCOVERED → QUALIFIED
    QUALIFIED → PREPARING
    PREPARING → READY_FOR_REVIEW
    READY_FOR_REVIEW → APPROVED
    APPROVED → APPLYING
    APPLYING → APPLIED

application_approvals
    PENDING → APPROVED

application_executions
    EXEC-001 → SUCCESS

communications
    COMM-001 → SENT

followups
    FOLLOWUP-001 → SCHEDULED
```

---

# 79. Transaction Example

Application approval should be:

```text
BEGIN

validate application

validate approval

update application

insert application_approval

insert application_state_history

insert outbox_event

COMMIT
```

RabbitMQ publication occurs after the transaction through the outbox publisher.

---

# 80. Application Submission Example

Do not keep a DB transaction open during browser execution.

Instead:

```text
Transaction #1

APPLICATION
APPROVED
    ↓
APPLYING

Create execution
Create outbox event

COMMIT
```

Then:

```text
Browser execution
```

Then:

```text
Transaction #2

Execution → SUCCESS

Application
APPLYING → APPLIED

Create state history
Create outbox event

COMMIT
```

---

# 81. Migration Strategy

Phase 3 database changes must use Flyway.

Recommended migration sequence:

```text
Vx__create_applications.sql

Vx__create_application_state_history.sql

Vx__create_application_approvals.sql

Vx__create_application_executions.sql

Vx__create_application_questions.sql

Vx__create_candidate_answers.sql

Vx__create_application_answers.sql

Vx__create_communications.sql

Vx__create_communication_events.sql

Vx__create_followups.sql

Vx__create_automation_rules.sql

Vx__create_scheduled_actions.sql

Vx__create_application_outcomes.sql

Vx__create_outbox_events.sql

Vx__create_idempotency_records.sql

Vx__add_phase3_indexes.sql
```

The actual Flyway version numbering must continue from the current project.

Do not assume a specific version number.

---

# 82. Migration Rules

Never:

* Modify an already-applied Flyway migration
* Delete an existing migration
* Rename an applied migration
* Reset production data

Instead create a new migration.

---

# 83. Existing Schema Compatibility

Before creating migrations:

1. Inspect every existing migration.
2. Inspect current JPA entities.
3. Identify existing resume tables.
4. Identify existing cover-letter tables.
5. Identify recruiter tables.
6. Identify workspace tables.
7. Identify notification tables.
8. Reuse existing IDs and relationships.

Do not duplicate existing Phase 2 concepts.

---

# 84. Backward Compatibility

Phase 3 migrations must not break:

* Authentication
* Profiles
* Jobs
* Matching
* Resume generation
* Cover letters
* Recruiters
* AI Workspace
* Notifications

Run all existing integration tests against the migrated schema.

---

# 85. Database Testing

Test:

## Migration

Fresh database:

```text
Flyway migrate
```

must succeed.

## Upgrade

Existing Phase 1 + Phase 2 database:

```text
Flyway migrate
```

must succeed.

## Constraints

Verify:

* Foreign keys
* Unique keys
* Nullability
* Indexes

## Concurrency

Test:

* Duplicate application creation
* Duplicate execution
* Duplicate email
* Duplicate follow-up

---

# 86. Data Integrity Rules

The following must always be true:

```text
Every application belongs to a user.

Every application references a valid job.

Every application transition is auditable.

Every approved execution belongs to an application.

Every application answer belongs to a question.

Every communication belongs to an application.

Every follow-up belongs to an application.

Every execution has an idempotency strategy.

Every important event can be traced.

Every submitted application references the exact artifacts used.
```

---

# 87. Performance Requirements

The database must support efficient queries for:

```text
User's active applications

Pending approvals

Applications ready to execute

Scheduled actions due now

Recent application events

Follow-ups due

Application analytics

Communication history
```

Indexes must support these query patterns.

---

# 88. Pagination

All potentially large collections must support pagination.

Examples:

```text
Applications
Application history
Communications
Questions
Execution history
Notifications
Analytics records where applicable
```

Never return unbounded application history from a REST endpoint.

---

# 89. Retention

Phase 3 should preserve application history by default.

Retention policy must be configurable in the future.

Do not implement automatic destructive cleanup without an explicit requirement.

---

# 90. Privacy

PII should be minimized.

Candidate answers may contain sensitive information.

Do not expose raw answer content in generic analytics tables.

Access must always be scoped to the authenticated user.

---

# 91. Database Definition of Done

The database implementation is complete when:

* All required Phase 3 tables exist.
* Existing Phase 1/2 schema remains compatible.
* Flyway migrations work from a clean database.
* Flyway upgrade works from the existing database.
* Foreign keys are correct.
* Indexes exist for critical queries.
* Unique constraints prevent duplicate critical operations.
* Optimistic locking is implemented where required.
* Application state history is persisted.
* Approval history is persisted.
* Execution history is persisted.
* Communication history is persisted.
* Follow-ups are persisted.
* Application questions and answers are persisted.
* Outbox events are persisted.
* Idempotency records are persisted.
* Existing tests pass.
* Phase 3 database integration tests pass.

---

# 92. Golden Database Rule

The most important database rule:

> The current state tells us what is happening. The history tells us what happened.

For example:

```text
applications.status = APPLIED
```

tells us:

> The application is currently applied.

While:

```text
application_state_history
```

tells us:

```text
DISCOVERED
→ QUALIFIED
→ PREPARING
→ READY_FOR_REVIEW
→ APPROVED
→ APPLYING
→ APPLIED
```

Both are required.

---

# 93. Final Database Model

```text
                         USERS
                           │
                           │
                           ▼
                         JOBS
                           │
                           │
                           ▼
                     APPLICATIONS
                           │
        ┌──────────────────┼───────────────────┐
        │                  │                   │
        ▼                  ▼                   ▼
 STATE_HISTORY        APPROVALS          EXECUTIONS
        │                                      │
        │                                      │
        └──────────────────┬───────────────────┘
                           │
                           ▼
                      APPLICATION
                        QUESTIONS
                           │
                           ▼
                    APPLICATION ANSWERS
                           │
                           ▼
                    CANDIDATE ANSWERS

APPLICATIONS
     │
     ├──────────────► COMMUNICATIONS
     │                      │
     │                      ▼
     │              COMMUNICATION EVENTS
     │
     ├──────────────► FOLLOWUPS
     │
     ├──────────────► OUTCOMES
     │
     └──────────────► SCHEDULED ACTIONS

USERS
  │
  └──────────────► AUTOMATION RULES

APPLICATIONS / SYSTEM
        │
        ▼
   OUTBOX EVENTS
        │
        ▼
     RABBITMQ

CRITICAL OPERATIONS
        │
        ▼
IDEMPOTENCY RECORDS
```

---

# 94. Final Rule

Before implementing any Phase 3 JPA entity or Flyway migration:

> Inspect the actual existing Phase 1 and Phase 2 database schema and reuse existing tables, IDs, relationships, and artifact models wherever possible.

This specification defines the target database model, but the actual implementation must integrate with the existing project's schema rather than blindly creating duplicate tables.

````

