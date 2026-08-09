# AI Career OS — Phase 3

# Architecture Specification

## Application Automation Engine

Version: 3.0

Status: Specification

---

# 1. Purpose

This document defines the technical architecture for Phase 3 of AI Career OS.

Phase 3 introduces the Application Automation Engine.

The architecture must:

- Preserve Phase 1 and Phase 2 functionality.
- Extend the existing modular monolith.
- Keep application execution provider-independent.
- Support asynchronous workflows.
- Support human approval.
- Support browser automation.
- Support email automation.
- Support scheduled follow-ups.
- Provide complete auditability.
- Allow future extraction into microservices.

---

# 2. Architectural Style

AI Career OS remains a:

> Modular Monolith with Event-Driven Asynchronous Processing.

The application is deployed as one Spring Boot application.

Internal modules must remain strongly separated.

RabbitMQ handles asynchronous workflows.

MySQL is the source of truth.

Redis handles transient state, locks, rate limits, and scheduling support.

MinIO stores generated and submitted artifacts.

Ollama remains behind the existing AI Orchestrator.

---

# 3. Existing Technology Stack

Continue using:

- Java 21
- Spring Boot 3
- Spring Security
- Spring Data JPA
- MySQL 8
- Flyway
- RabbitMQ
- Redis
- MinIO
- Ollama
- Gradle
- Docker Compose
- Spring Boot Actuator
- OpenAPI

---

# 4. New Phase 3 Technologies

Browser automation may introduce a browser automation framework.

The framework must be isolated behind a provider interface.

Recommended implementation:

Playwright for Java.

The browser framework must never leak into domain services.

If Playwright is unavailable or unsuitable during implementation, the BrowserProvider interface must still be created first.

---

# 5. High-Level Architecture

```text
                         REST API
                            │
                            ▼
                     Controllers
                            │
                            ▼
                   Application Services
                            │
             ┌──────────────┼──────────────┐
             ▼              ▼              ▼
       Application       Approval       Analytics
          Domain          Domain          Domain
             │              │              │
             └──────────────┼──────────────┘
                            ▼
                  Application Orchestrator
                            │
                    RabbitMQ Commands
                            │
          ┌─────────────────┼─────────────────┐
          ▼                 ▼                 ▼
    Browser Provider   Email Provider    Manual Provider
          │                 │                 │
          ▼                 ▼                 ▼
      Job Website         SMTP/API        User Action
````

---

# 6. Module Structure

Existing modules must remain intact.

Phase 3 adds:

```text
com.ai.career

    application

    automation

    approval

    browser

    communication

    followup

    analytics

    answer

    events
```

---

# 7. Application Module

The Application module owns the application domain.

Suggested structure:

```text
application/

    controller/

    service/

    domain/

        Application.java
        ApplicationStatus.java
        ApplicationMethod.java
        ApplicationResult.java

    repository/

    dto/

    mapper/

    validator/

    exception/

    events/
```

Responsibilities:

* Create application
* Update application
* Retrieve application
* Manage lifecycle
* Manage application metadata
* Record application state

The Application module must not directly execute browser or email operations.

---

# 8. Automation Module

The Automation module coordinates execution.

Suggested structure:

```text
automation/

    orchestrator/

    service/

    provider/

    command/

    result/

    scheduler/

    retry/

    lock/

    exception/
```

Responsibilities:

* Workflow orchestration
* Provider selection
* Execution
* Retry
* Locking
* Failure handling
* Idempotency
* Event publishing

---

# 9. Approval Module

Suggested structure:

```text
approval/

    controller/

    service/

    domain/

    repository/

    dto/

    events/
```

Responsibilities:

* Request approval
* Approve
* Reject
* Cancel
* Record approval history
* Enforce approval requirements

---

# 10. Browser Module

Suggested structure:

```text
browser/

    provider/

    session/

    navigation/

    form/

    field/

    upload/

    verification/

    screenshot/

    exception/
```

The browser module owns all browser framework dependencies.

No other module may import Playwright classes directly.

---

# 11. Communication Module

The communication module manages outbound communication.

Suggested structure:

```text
communication/

    controller/

    service/

    provider/

    domain/

    repository/

    dto/

    scheduler/

    events/
```

Possible providers:

```text
EmailProvider
    ├── SmtpEmailProvider
    └── ApiEmailProvider
```

---

# 12. Follow-Up Module

Responsibilities:

* Follow-up rules
* Scheduling
* Business-day calculations
* Stop conditions
* Execution
* Notifications

Suggested structure:

```text
followup/

    service/

    scheduler/

    rules/

    repository/

    domain/

    events/
```

---

# 13. Answer Module

The Answer module manages application questions.

Responsibilities:

* Normalize questions
* Match known answers
* Store approved answers
* Generate answer drafts
* Track answer source
* Track approval

---

# 14. Analytics Module

Responsibilities:

* Application statistics
* Conversion rates
* Interview metrics
* Recruiter response rates
* Resume performance
* Outreach performance

Analytics must read from persisted application data.

Analytics must not become the source of truth.

---

# 15. Dependency Rules

Allowed:

```text
Controller
    ↓
Application Service
    ↓
Domain
    ↓
Repository
```

External systems:

```text
Application Service
    ↓
Provider Interface
    ↓
Infrastructure Adapter
```

Not allowed:

```text
Controller → Repository

Domain → Browser

Domain → Playwright

Domain → SMTP

Domain → Ollama

Repository → External API

AI Service → Browser
```

---

# 16. Provider Architecture

External execution systems must use interfaces.

Example:

```java
public interface ApplicationProvider {

    ProviderCapabilities getCapabilities();

    ApplicationPreparationResult prepare(
        ApplicationContext context
    );

    ApplicationExecutionResult execute(
        ApplicationContext context
    );

    ApplicationVerificationResult verify(
        ApplicationContext context
    );
}
```

The exact implementation may differ, but the abstraction must remain.

---

# 17. Provider Capabilities

Providers must advertise capabilities.

Example:

```json
{
  "supportsResumeUpload": true,
  "supportsCoverLetter": true,
  "supportsApplicationQuestions": true,
  "supportsSubmission": true,
  "supportsVerification": true,
  "supportsManualIntervention": true
}
```

The orchestrator must check capabilities before execution.

---

# 18. Application Orchestrator

The Application Orchestrator is the central execution component.

Responsibilities:

1. Load application.
2. Validate state.
3. Validate approval.
4. Load workspace.
5. Select provider.
6. Acquire execution lock.
7. Prepare execution context.
8. Execute provider.
9. Verify result.
10. Update application state.
11. Persist result.
12. Publish events.
13. Notify user.

---

# 19. Orchestration Flow

```text
Application Approved
        ↓
Application Orchestrator
        ↓
Load Application
        ↓
Validate
        ↓
Acquire Lock
        ↓
Build ApplicationContext
        ↓
Select Provider
        ↓
Prepare
        ↓
Execute
        ↓
Verify
        ↓
Persist
        ↓
Publish Event
        ↓
Notify
```

---

# 20. Application Context

The orchestrator must create an immutable execution context.

Example:

```text
ApplicationContext

    applicationId
    userId
    jobId
    companyId

    candidateProfile
    resume
    coverLetter

    recruiter
    applicationQuestions
    approvedAnswers

    applicationUrl
    provider

    automationLevel
    approval
```

The context represents the exact information used for execution.

---

# 21. Artifact Version Locking

Once an application is approved, the exact artifacts must be locked.

Example:

```text
Application

resumeVersion = 7
coverLetterVersion = 3
```

If the user generates a newer resume afterward, the existing application must continue referencing version 7.

This ensures reproducibility.

---

# 22. State Management

Application state must be persisted in MySQL.

State transitions must be validated by a dedicated state machine.

No service may directly assign arbitrary states.

Incorrect:

```java
application.setStatus(APPLIED);
```

Correct:

```java
application.transitionTo(APPLIED);
```

or an equivalent domain service.

---

# 23. State Transition Service

Create:

```text
ApplicationStateMachine
```

Responsibilities:

* Validate transition
* Execute transition
* Record history
* Publish transition event

---

# 24. Approval Architecture

Approval is separate from application state.

Example:

```text
Application State

READY_FOR_REVIEW

Approval State

PENDING
```

After approval:

```text
Approval State

APPROVED

Application State

APPROVED
```

This separation allows future approval workflows.

---

# 25. Approval Rules

Before execution:

```text
Application exists
AND
Application is READY_FOR_REVIEW
AND
Approval is APPROVED
AND
Required artifacts exist
AND
Provider exists
AND
No active execution exists
```

Only then can execution begin.

---

# 26. RabbitMQ Architecture

RabbitMQ handles asynchronous execution.

Recommended exchanges:

```text
career.application.exchange
career.communication.exchange
career.followup.exchange
career.notification.exchange
```

Queues:

```text
application.execution.queue

application.verification.queue

communication.send.queue

followup.schedule.queue

followup.execution.queue

notification.queue
```

Dead-letter queues must be configured.

---

# 27. Event-Driven Workflow

Example:

```text
APPLICATION_APPROVED
        ↓
RabbitMQ
        ↓
application.execution.queue
        ↓
ApplicationExecutionListener
        ↓
ApplicationOrchestrator
```

After successful submission:

```text
APPLICATION_SUBMITTED
        ↓
RabbitMQ
        ↓
FOLLOWUP_SCHEDULER
```

---

# 28. Event Design

Events must be immutable.

Every event must contain:

```json
{
  "eventId": "uuid",
  "eventType": "APPLICATION_SUBMITTED",
  "eventVersion": 1,
  "aggregateId": "application-id",
  "userId": "user-id",
  "timestamp": "ISO-8601",
  "correlationId": "uuid",
  "payload": {}
}
```

---

# 29. Event Idempotency

Consumers must be idempotent.

Every event must have an event ID.

Processed event IDs should be tracked.

A duplicate event must not cause duplicate:

* Applications
* Emails
* Follow-ups
* State transitions
* Notifications

---

# 30. Transactional Outbox

For important domain events, use a transactional outbox pattern.

Workflow:

```text
MySQL Transaction
       │
       ├── Update Application
       │
       └── Save Outbox Event
                ↓
          Outbox Publisher
                ↓
             RabbitMQ
```

This prevents database updates from succeeding while event publication fails.

---

# 31. Distributed Locking

Application execution must use a distributed lock.

Redis may be used.

Example key:

```text
lock:application:{applicationId}
```

Only one execution may own the lock.

Lock must expire automatically.

---

# 32. Idempotency

Critical commands must support idempotency.

Examples:

```text
submitApplication
sendEmail
sendFollowup
approveApplication
```

Idempotency key:

```text
applicationId + actionType + executionVersion
```

---

# 33. Retry Architecture

Retry only transient errors.

Examples:

* Network timeout
* Temporary provider failure
* Browser startup failure
* Temporary SMTP failure

Do not retry:

* User cancellation
* Invalid candidate data
* CAPTCHA
* MFA
* Authorization failure
* Missing required answer

Use exponential backoff.

Example:

```text
Attempt 1
↓
5 seconds

Attempt 2
↓
30 seconds

Attempt 3
↓
2 minutes
```

Maximum attempts configurable.

---

# 34. Failure Architecture

When execution fails:

```text
Provider
   ↓
Execution Result
   ↓
Failure Handler
   ↓
Persist failure
   ↓
Application → ACTION_REQUIRED
   ↓
Notification
```

The system must never silently mark a failed application as submitted.

---

# 35. Browser Architecture

Browser automation must be isolated.

```text
BrowserApplicationProvider
        ↓
BrowserSessionManager
        ↓
Browser
        ↓
Page
        ↓
FormInspector
        ↓
FieldMapper
        ↓
DocumentUploader
        ↓
SubmissionHandler
        ↓
VerificationHandler
```

---

# 36. Browser Session

A browser session must have:

* Session ID
* Application ID
* Created time
* Last activity
* Current URL
* Status

Sessions must be cleaned up after execution.

---

# 37. Browser Security

The browser engine must not:

* Bypass CAPTCHA
* Bypass MFA
* Bypass authentication
* Circumvent security controls
* Extract passwords
* Execute arbitrary downloaded code

When human interaction is required:

```text
AUTOMATION_PAUSED
```

and the user must be notified.

---

# 38. Form Inspection

The form inspector should identify:

* Input
* Textarea
* Select
* Checkbox
* Radio
* File upload
* Required fields

It should produce a normalized representation.

Example:

```json
{
  "fieldId": "experience",
  "label": "Years of experience",
  "type": "NUMBER",
  "required": true
}
```

---

# 39. Field Mapping

Mapping priority:

1. Exact known mapping
2. Candidate profile mapping
3. Approved Answer Bank
4. Deterministic semantic mapping
5. AI suggestion
6. Human review

AI must never directly invent values.

---

# 40. Application Question Handling

Unknown questions must produce:

```text
QUESTION_REQUIRES_REVIEW
```

The workflow pauses.

The user can:

* Answer
* Save answer
* Skip if optional
* Cancel application

---

# 41. Document Upload

The browser provider may upload:

* Resume
* Cover letter
* Additional approved documents

Files must originate from MinIO.

The exact artifact version must be recorded.

---

# 42. Submission Verification

Never mark:

```text
APPLIED
```

simply because the submit button was clicked.

Verification may use:

* Confirmation page
* Confirmation message
* Application ID
* Redirect URL
* Provider response
* User confirmation

If verification is inconclusive:

```text
SUBMISSION_REQUIRES_REVIEW
```

---

# 43. Email Architecture

```text
Communication Service
        ↓
EmailProvider
        ↓
SMTP / API Adapter
```

The business layer must never directly use JavaMail or provider-specific APIs.

---

# 44. Email Sending Rules

Before sending:

```text
Recipient valid
AND
Email approved
AND
Application active
AND
No prior conflicting communication
AND
Rate limit available
```

After sending:

```text
Persist communication
Publish EMAIL_SENT
Schedule follow-up
```

---

# 45. Follow-Up Scheduler

Follow-ups must be persisted in MySQL.

Redis may be used for scheduling acceleration.

MySQL remains the source of truth.

Scheduler must support:

* Delayed execution
* Retry
* Cancellation
* Business days
* Timezone
* Stop conditions

---

# 46. Follow-Up Stop Conditions

Cancel scheduled follow-up if:

* Recruiter responds
* Application rejected
* Application withdrawn
* User disables follow-ups
* Maximum follow-ups reached
* Job closes

---

# 47. Rate Limiting

Redis should implement distributed rate limits.

Examples:

```text
applications:user:{userId}:day

emails:user:{userId}:day

followups:user:{userId}:day
```

Limits must be configurable.

---

# 48. Scheduling

Scheduled actions must contain:

```text
scheduledActionId
applicationId
actionType
scheduledAt
status
attemptCount
lastError
```

Statuses:

```text
SCHEDULED
PROCESSING
COMPLETED
FAILED
CANCELLED
```

---

# 49. Analytics Architecture

Analytics should be derived from application events and persisted records.

Example:

```text
Applications
    ↓
Application Events
    ↓
Analytics Service
    ↓
Aggregated Metrics
```

Phase 3 may use SQL queries initially.

Do not introduce a separate analytics database unless necessary.

---

# 50. Notification Architecture

Notifications are asynchronous.

```text
Domain Event
    ↓
Notification Listener
    ↓
Notification Service
    ↓
Telegram Provider
```

Notification failures must not fail the application transaction.

---

# 51. Security Architecture

Use existing Spring Security.

All Phase 3 APIs require authentication unless explicitly marked public.

Authorization must ensure:

```text
User A
cannot access
User B's application.
```

Every application query must be scoped to authenticated user identity.

---

# 52. Sensitive Data

Never log:

* JWT
* Password
* Email credentials
* SMTP credentials
* Browser authentication cookies
* Session secrets
* Private keys

Candidate PII must be minimized in logs.

---

# 53. AI Integration

The existing AI Orchestrator remains the only LLM entry point.

Phase 3 may use AI for:

* Question classification
* Answer generation
* Form field suggestions
* Job application recommendations
* Communication generation

Phase 3 must not create a second LLM client.

---

# 54. AI Boundary

Correct:

```text
Application
    ↓
AnswerService
    ↓
AIOrchestrator
    ↓
Ollama
```

Incorrect:

```text
BrowserProvider
    ↓
OllamaClient
```

---

# 55. Data Ownership

MySQL owns:

* Applications
* Application states
* Application history
* Approvals
* Questions
* Answers
* Communications
* Scheduled actions
* Outcomes

Redis owns transient operational state.

RabbitMQ owns messages, not business state.

MinIO owns binary artifacts.

---

# 56. API Layer

Controllers must remain thin.

Example:

```text
ApplicationController
        ↓
ApplicationService
        ↓
Domain
```

Controllers must not:

* Execute browsers
* Send emails
* Call RabbitMQ directly
* Call Ollama
* Modify repositories directly

---

# 57. Transaction Boundaries

Application state changes should occur inside transactions.

Example:

```text
@Transactional

approveApplication()

    validate transition
    update approval
    update application state
    create event/outbox record
```

Long-running browser operations must never hold a database transaction open.

---

# 58. Long-Running Workflow

Incorrect:

```text
BEGIN TRANSACTION

open browser

fill form

wait

submit

verify

COMMIT
```

Correct:

```text
Transaction
    ↓
Mark APPLYING
    ↓
Commit
    ↓
Browser execution
    ↓
Transaction
    ↓
Persist result
    ↓
Commit
```

---

# 59. Concurrency

The system must protect against:

* Two workers applying the same job
* Two follow-ups sending simultaneously
* Duplicate email delivery
* Duplicate approval
* Duplicate state transition

Use:

* Redis locks
* Database constraints
* Idempotency keys
* Optimistic locking

---

# 60. Database Constraints

Important constraints include:

* Unique user/job active application
* Unique application event ID
* Unique execution ID
* Unique idempotency key
* Foreign key integrity
* Valid state values

Exact schema is defined in Database.md.

---

# 61. Observability

Every request:

```text
Correlation ID
```

Every application workflow:

```text
Application ID
```

Every automation execution:

```text
Execution ID
```

Every event:

```text
Event ID
```

These identifiers must appear in structured logs.

---

# 62. Metrics

Expose metrics through Spring Boot Actuator.

Required metrics:

```text
applications.created
applications.approved
applications.submitted
applications.failed

automation.executions
automation.success
automation.failure

emails.sent
emails.failed

followups.scheduled
followups.sent

browser.sessions
browser.failures
```

---

# 63. Testing Architecture

Each module must have:

## Unit Tests

Domain logic.

## Integration Tests

Database and Spring context.

## Messaging Tests

RabbitMQ consumers/producers.

## Provider Tests

Mock external systems.

## Browser Tests

Use controlled test pages.

Never test against real candidate applications in automated CI.

## End-to-End Tests

Test the complete workflow using fake providers.

---

# 64. Test Provider

A fake provider must exist.

Example:

```text
FakeApplicationProvider
```

It should simulate:

* Success
* Failure
* Timeout
* Unknown question
* Verification failure
* Duplicate submission

This allows the entire orchestration system to be tested without external websites.

---

# 65. Environment Configuration

All provider configuration must use environment variables.

Examples:

```text
BROWSER_ENABLED
BROWSER_HEADLESS
EMAIL_PROVIDER
SMTP_HOST
SMTP_PORT
SMTP_USERNAME
SMTP_PASSWORD
APPLICATION_AUTOMATION_LEVEL
MAX_DAILY_APPLICATIONS
MAX_DAILY_EMAILS
```

No secrets in source code.

---

# 66. Feature Flags

Browser automation should be feature-flagged.

Example:

```text
application.automation.enabled=false
browser.automation.enabled=false
email.automation.enabled=false
```

This allows safe rollout.

---

# 67. Rollout Strategy

Recommended order:

```text
Development
    ↓
Fake Provider
    ↓
Local Test Provider
    ↓
Manual Provider
    ↓
Browser Dry Run
    ↓
Human Approval Mode
    ↓
Limited Production
```

Do not immediately enable unrestricted automation.

---

# 68. Package Dependency Direction

```text
API
 ↓
Application Services
 ↓
Domain
 ↓
Ports
 ↓
Adapters
```

The domain must remain framework-light.

Spring annotations may be used pragmatically, but business logic must remain testable independently.

---

# 69. Architecture Acceptance Criteria

Architecture is considered implemented correctly only if:

* Application module exists.
* Automation module exists.
* Approval module exists.
* Browser module is isolated.
* Communication provider is isolated.
* Application Provider SPI exists.
* Browser framework is not imported outside browser infrastructure.
* Email framework is not imported outside communication infrastructure.
* AI calls go through AI Orchestrator.
* MySQL remains source of truth.
* RabbitMQ handles asynchronous workflows.
* Redis handles transient coordination.
* MinIO handles artifacts.
* Long-running operations do not hold DB transactions.
* Critical operations are idempotent.
* Application execution uses distributed locking.
* Events are auditable.
* Failed workflows can be recovered.
* Existing Phase 1 and Phase 2 modules remain functional.

---

# 70. Final Architecture

The complete Phase 3 architecture is:

```text
                         ┌───────────────────┐
                         │     REST API      │
                         └─────────┬─────────┘
                                   │
                         ┌─────────▼─────────┐
                         │ Application Layer │
                         └─────────┬─────────┘
                                   │
                 ┌─────────────────┼─────────────────┐
                 │                 │                 │
          ┌──────▼──────┐  ┌──────▼──────┐  ┌──────▼──────┐
          │ Application │  │  Approval   │  │  Analytics  │
          │   Domain    │  │   Domain    │  │   Domain    │
          └──────┬──────┘  └─────────────┘  └─────────────┘
                 │
                 ▼
       ┌──────────────────────┐
       │ Application          │
       │ Orchestrator         │
       └──────────┬───────────┘
                  │
             Provider SPI
                  │
       ┌──────────┼───────────┐
       │          │           │
       ▼          ▼           ▼
   Browser      Email       Manual
   Provider    Provider     Provider
       │          │
       ▼          ▼
   External     External
   Website      Email
       │          │
       └────┬─────┘
            ▼
        Result
            │
            ▼
     Application State
            │
      ┌─────┴─────┐
      ▼           ▼
  RabbitMQ      MySQL
      │           │
      ▼           ▼
 Notifications  History
      │
      ▼
   Telegram
```

---

# 71. Architectural Golden Rule

The most important rule of Phase 3:

> The Application Domain controls WHAT should happen. Providers control HOW it happens.

For example:

```text
Application Domain

"Submit this approved application."

Provider

"I know how to submit it through this specific mechanism."
```

This separation must remain intact throughout Phase 3.

---

# 72. Future Migration

The modular monolith must preserve the possibility of extracting:

```text
Application Service
Automation Service
Browser Automation Service
Communication Service
Analytics Service
Notification Service
```

into independent services later.

Do not prematurely introduce microservices in Phase 3.

The modular monolith remains the primary architecture.

````

