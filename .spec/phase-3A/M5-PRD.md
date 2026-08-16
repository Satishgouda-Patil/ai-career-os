# M5-PRD.md — Automation Orchestrator & End-to-End Application Pipeline

## 1. Purpose

M5 connects the completed Phase 1, Phase 2, and Phase 3 components into one deterministic, observable application workflow.

The goal is:

Job discovered
→ matched
→ qualified
→ AI workspace generated
→ form analyzed
→ human approval
→ execution plan created
→ execution requested

M5 MUST NOT implement browser automation. Browser execution belongs to M6-A/M6-B.

## 2. Scope

### In scope

- Application automation orchestrator
- End-to-end workflow coordination
- Application readiness evaluation
- Approval validation
- Idempotent workflow execution
- Event publishing
- Retry policy for internal orchestration
- Workflow execution history
- Notification events
- Failure handling
- Human-review routing
- Resume/cover-letter/form-plan artifact validation
- Integration with existing Application, Workspace, Form, Execution, Redis-lock and provider SPI components

### Out of scope

- Browser automation
- CAPTCHA solving
- Real external application submission
- Automatic LinkedIn messaging
- Automatic email sending
- Bypassing anti-bot systems
- Fabricating candidate information

## 3. Core principle

The orchestrator coordinates existing capabilities. It must not duplicate business logic already owned by domain services.

## 4. Workflow

### Standard workflow

1. Load Application.
2. Validate ownership.
3. Validate current ApplicationState.
4. Load Job.
5. Load candidate Profile.
6. Load approved Workspace/artifacts.
7. Validate resume artifact.
8. Validate cover letter artifact where applicable.
9. Load ApplicationFormPlan.
10. Evaluate readiness.
11. If required information is missing → ACTION_REQUIRED / SUBMISSION_REQUIRES_REVIEW.
12. Validate human approval.
13. Create an execution request.
14. Publish execution event.
15. Persist workflow audit information.
16. Notify user of the next action/result.

## 5. Readiness rules

Application may proceed to execution only when:

- Application exists and belongs to authenticated user.
- Application is in an execution-eligible state.
- Workspace artifacts required by the job are available.
- Form plan exists when form analysis is required.
- Form plan is READY, or explicitly approved under an allowed review policy.
- No required USER_REQUIRED field remains unresolved.
- No unsupported required field remains unresolved.
- Required approval exists.
- No active execution already exists.

Never infer missing candidate facts.

## 6. Application state integration

Expected high-level transitions:

DISCOVERED
→ QUALIFIED
→ PREPARING
→ READY_FOR_REVIEW
→ APPROVED
→ APPLYING

If execution cannot safely continue:

APPROVED
→ SUBMISSION_REQUIRES_REVIEW

If a controlled execution succeeds, M6 owns the final execution outcome and may transition:

APPLYING
→ APPLIED

M5 itself must not claim an application was submitted.

## 7. Idempotency

The orchestrator must be safe when the same command/event is received multiple times.

Required safeguards:

- application-level active execution check
- idempotency key
- Redis distributed execution lock
- database persistence
- state validation inside the transaction
- duplicate event protection

Recommended idempotency key:

`applicationId + workflowType + approvalVersion`

## 8. Events

Create domain events where appropriate:

- ApplicationApproved
- ApplicationExecutionRequested
- ApplicationExecutionStarted
- ApplicationExecutionCompleted
- ApplicationExecutionRequiresReview
- ApplicationExecutionFailed

Events should contain IDs, timestamps, user/application IDs and correlation ID.

Never put sensitive resume contents or secrets directly into event payloads.

## 9. Execution request

Create a dedicated internal execution request model containing:

- applicationId
- userId
- jobId
- provider name/capability requirement
- execution plan reference
- form plan reference
- workspace reference
- approval reference
- idempotency key
- correlation ID

## 10. Retry policy

Retry only transient internal failures.

Retryable examples:

- temporary database failure
- transient message broker failure
- temporary provider availability issue

Do not blindly retry:

- validation failure
- missing candidate information
- unsupported form
- human-review requirement
- non-retryable provider error

Use bounded exponential backoff.

## 11. Human review

The orchestrator must produce a clear reason when stopping.

Examples:

- "Work authorization answer required"
- "Unknown required application field"
- "Application provider unsupported"
- "Approval expired"
- "Form plan requires review"

## 12. Notifications

Publish notifications for:

- application ready for review
- approval accepted
- execution started
- execution requires review
- execution failed
- execution completed

Telegram can remain the initial notification channel.

## 13. REST API

Suggested endpoints:

POST `/api/v1/applications/{id}/orchestrate`
POST `/api/v1/applications/{id}/approve-and-prepare`
GET `/api/v1/applications/{id}/workflow`
GET `/api/v1/applications/{id}/readiness`

Do not expose internal provider secrets.

## 14. Observability

Every orchestration run must have:

- correlationId
- applicationId
- workflowRunId
- start/end timestamps
- current stage
- outcome
- failure code
- retry count

Use structured logs.

Never log JWTs, passwords, API keys, resume contents or personal secrets.

## 15. Security

- Enforce authenticated user ownership.
- Never trust application IDs supplied by clients.
- Never bypass approval checks.
- Never fabricate candidate information.
- Never automatically submit from M5.
- Never expose stored files directly without authorization.

## 16. Database

Prefer existing tables where possible.

If persistence is required, add a workflow-run table through Flyway rather than storing workflow state only in memory.

Suggested fields:

- id
- application_id
- workflow_type
- status
- idempotency_key
- correlation_id
- current_stage
- failure_code
- retry_count
- started_at
- completed_at

## 17. Testing

Required:

### Unit

- readiness rules
- state validation
- idempotency
- retry classification
- human-review routing

### Integration

- complete approved workflow
- missing required field
- rejected approval
- duplicate orchestration request
- provider unavailable
- Redis unavailable
- event publishing
- regression against Phase 1–3

### Safety

Prove M5 cannot directly submit an external application.

## 18. Definition of Done

- Existing tests remain green.
- `./gradlew test` succeeds.
- Workflow is idempotent.
- All state transitions are validated.
- Approval is mandatory where configured.
- Missing facts stop execution.
- Events are published reliably.
- No browser automation exists in M5.
- No real application submission occurs in M5.
- Changes are committed and pushed.

## 19. Implementation instruction for AI agent

First inspect the existing codebase and `.spec` documents.

Do not rewrite completed Phase 1–3 components.

Reuse existing services, entities, repositories, events, locks and provider SPI.

Implement M5 incrementally.

Before changing code:

1. Create/update the M5 design/spec files.
2. Inspect current ApplicationState transitions.
3. Inspect ApplicationExecutionService.
4. Inspect ApplicationFormService.
5. Inspect Workspace/AI artifact models.
6. Inspect RabbitMQ configuration.
7. Inspect Redis locking.
8. Identify exact integration points.

After implementation:

- run tests
- add missing tests
- verify no external browser/network submission is introduced
- provide a walkthrough
- commit with `feat(phase-3-m5): ...`
- push to `main`

STOP after M5 and wait for explicit approval before M6.
