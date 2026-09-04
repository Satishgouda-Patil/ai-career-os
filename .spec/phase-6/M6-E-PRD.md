# Phase 6 — M6-E PRD

## Controlled ATS Execution Operations & Production Safety

**Project:** AI Career OS
**Phase:** 6 — Browser-Based Application Execution
**Milestone:** M6-E
**Status:** Ready for Implementation
**Primary ATS:** Greenhouse
**Execution Model:** Controlled, human-authorized, server-gated
**Default Live Execution:** OFF

---

# 1. Objective

M6-E is the production-hardening milestone for the controlled Greenhouse browser execution system created in M6-A through M6-D.

The goal is to build the **operational control layer** around browser-based ATS execution.

M6-E must provide:

* centralized execution controls
* execution history
* safety status
* provider health
* execution evidence
* failure diagnostics
* rollback/recovery handling
* operator review
* immutable audit visibility
* configuration validation
* execution attempt monitoring
* strict tenant/user isolation
* explicit separation between sandbox, read-only, and live execution

M6-E must **not** enable unrestricted autonomous job application.

The system must remain human-controlled.

---

# 2. Current System State

The following milestones are already implemented and must be reused.

## M6-A — Browser Infrastructure

Implemented:

* provider-agnostic Playwright browser session
* browser safety policy
* domain allowlist
* redirect validation
* read-only browser inspection
* form discovery
* field detection
* stable selectors
* distributed locking
* browser audit logging

Supported domains:

* `greenhouse.io`
* `boards.greenhouse.io`
* `localhost`
* `127.0.0.1`

---

## M6-B — Browser Interaction

Implemented:

* candidate-field mapping
* safe text filling
* email filling
* phone filling
* URL filling
* textarea handling
* select handling
* radio handling
* checkbox handling
* DOM event dispatch
* read-back verification
* unresolved-field detection
* review-required handling
* submit control detection without clicking

---

## M6-C — Sandbox Execution

Implemented:

* sandbox execution runs
* sandbox execution provider
* execution orchestration
* mock submission simulation
* verification service
* execution status
* sandbox audit
* distributed locking

Guarantees:

```text
realSubmissionAttempted = false
emailSent = false
fileUploadedToRealProvider = false
```

---

## M6-D — Controlled Greenhouse Real-ATS Dry Run

Implemented:

* Greenhouse production read-only inspection
* real ATS form fingerprinting
* controlled execution domain
* 17 pre-submission safety checks
* candidate ownership validation
* candidate confirmation state machine
* live execution controller
* live execution status
* production read-only mode
* server-side live submission gate
* execution audit
* execution run persistence

Current mandatory configuration:

```text
ALLOW_LIVE_SUBMISSION = false
AUTO_APPLY = false
AUTO_SEND_EMAIL = false
AUTO_LINKEDIN = false
```

Current live execution result:

```text
BLOCKED
submissionAttempted = false
```

This behavior must remain unchanged after M6-E.

---

# 3. M6-E Core Principle

M6-E is an **operations and safety layer**, not an autonomous application engine.

The system must make it possible to answer:

1. What happened?
2. Who initiated it?
3. Which application was targeted?
4. Which ATS was targeted?
5. Which browser mode was used?
6. Which form fingerprint was used?
7. Which candidate facts were used?
8. Which safety checks passed?
9. Which safety checks failed?
10. Was anything actually submitted?
11. Was any email sent?
12. Was any file uploaded?
13. Why was execution blocked or failed?
14. Can the run be safely retried?
15. What exact state is the application currently in?

---

# 4. Scope

M6-E includes:

### Backend

* execution control service
* execution history
* execution evidence
* provider health
* safety configuration status
* execution diagnostics
* retry eligibility
* recovery handling
* immutable audit records
* execution metrics
* operational APIs

### Frontend

* ATS Operations Center
* execution history
* execution detail
* safety dashboard
* provider health
* blocked execution explanation
* retry/recovery controls
* evidence viewer
* configuration status
* human authorization status

### Security

* authorization
* ownership checks
* tenant isolation
* configuration validation
* audit sanitization
* credential protection
* execution lock protection

---

# 5. Explicit Non-Goals

M6-E must NOT implement:

* autonomous job applications
* autonomous approval
* automatic candidate confirmation
* automatic CAPTCHA solving
* CAPTCHA bypass
* anti-bot bypass
* LinkedIn automation
* outbound email
* automatic resume modification
* automatic cover-letter fabrication
* automatic answers to unknown legal/compliance questions
* credential extraction
* credential logging
* stealth browser behavior
* proxy rotation
* fingerprint evasion
* anti-detection mechanisms
* submission without explicit authorization
* submission when safety configuration is disabled

Do not add any fallback that makes a failed real execution appear successful.

---

# 6. Safety Requirements

These requirements are mandatory.

## 6.1 Hard Live Submission Gate

The following must remain false by default:

```text
ALLOW_LIVE_SUBMISSION=false
AUTO_APPLY=false
AUTO_SEND_EMAIL=false
AUTO_LINKEDIN=false
```

If `ALLOW_LIVE_SUBMISSION=false`:

```text
POST /browser/live/execute
```

must never interact with the external ATS submission control.

Expected result:

```json
{
  "status": "BLOCKED",
  "submissionAttempted": false
}
```

---

# 7. Execution Modes

M6-E must clearly distinguish:

```text
SANDBOX
PRODUCTION_READ_ONLY
PRODUCTION_LIVE
```

## SANDBOX

Allowed:

* local browser execution
* mock ATS behavior
* form interaction simulation
* field mapping
* verification

Forbidden:

* real ATS submission
* real email
* real external file upload

---

## PRODUCTION_READ_ONLY

Allowed:

* real Greenhouse page navigation
* form discovery
* form fingerprinting
* DOM inspection
* field inspection
* read-back verification

Forbidden:

* submit
* apply
* send
* external file upload
* destructive interaction

---

## PRODUCTION_LIVE

Allowed only when every safety gate passes.

However:

```text
ALLOW_LIVE_SUBMISSION=false
```

remains the default.

M6-E must therefore treat live execution as:

```text
CONFIGURED_BUT_DISABLED
```

unless the server configuration explicitly enables it.

---

# 8. Execution Lifecycle

M6-E must standardize execution lifecycle states.

```text
PREPARED
   |
   v
AWAITING_CONFIRMATION
   |
   v
CONFIRMED
   |
   v
SAFETY_VALIDATION
   |
   +----> BLOCKED
   |
   +----> FAILED
   |
   v
EXECUTING
   |
   +----> FAILED
   |
   v
VERIFICATION
   |
   v
APPLIED
```

For the current configuration, the normal production path must stop at:

```text
BLOCKED
```

because:

```text
ALLOW_LIVE_SUBMISSION=false
```

---

# 9. Retry Rules

Retries must be explicit.

A failed or blocked execution must NOT automatically retry.

Retry eligibility must be evaluated.

A run may be retried only when:

* application still exists
* authenticated user owns application
* provider is supported
* job URL remains valid
* form fingerprint is still valid or a new read-only inspection has been completed
* candidate approval remains valid
* required fields are resolved
* no unresolved compliance fields exist
* no conflicting execution lock exists
* retry is explicitly authorized

---

# 10. Form Fingerprint Safety

Every controlled execution must be associated with a form fingerprint.

The fingerprint should include stable information such as:

```text
provider
host
normalized URL
form selector
field identifiers
field types
required field set
submit control metadata
```

Do not include:

* candidate PII
* passwords
* credentials
* resume content
* cover letter content
* access tokens

If the current form fingerprint differs materially from the prepared fingerprint:

```text
execution = BLOCKED
reason = FORM_FINGERPRINT_CHANGED
```

The system must require a fresh read-only preparation.

---

# 11. Execution Evidence

Every execution run must generate an evidence record.

Evidence should include:

```text
executionRunId
applicationId
userId
provider
mode
status
startedAt
completedAt
formFingerprint
fieldsDetected
fieldsMapped
fieldsRequireReview
fieldsUnsupported
safetyChecksPassed
safetyChecksFailed
submissionAttempted
emailSent
fileUploaded
failureReason
retryEligible
```

Never persist raw candidate secrets.

---

# 12. Evidence Redaction

Evidence must never contain:

* passwords
* API keys
* OAuth access tokens
* refresh tokens
* encrypted credential values
* session cookies
* authorization headers
* full resume content
* full cover-letter content
* unnecessary candidate PII

Candidate values must be represented using metadata.

Example:

```text
fieldType = EMAIL
mapped = true
verified = true
valuePresent = true
```

instead of:

```text
email = candidate@example.com
```

---

# 13. Execution Audit

Every significant execution event must be audited.

Required events:

```text
EXECUTION_PREPARED
EXECUTION_CONFIRMATION_REQUESTED
EXECUTION_CONFIRMED
SAFETY_VALIDATION_STARTED
SAFETY_CHECK_FAILED
EXECUTION_BLOCKED
EXECUTION_STARTED
EXECUTION_FAILED
EXECUTION_VERIFICATION_STARTED
EXECUTION_VERIFIED
EXECUTION_RETRY_REQUESTED
EXECUTION_RECOVERY_COMPLETED
```

Audit records must contain:

```text
eventType
executionRunId
applicationId
userId
provider
mode
timestamp
result
sanitizedReason
```

Do not store sensitive values.

---

# 14. Execution Operations Service

Create:

```text
BrowserExecutionOperationsService
```

Suggested package:

```text
com.ai.career.browser.execution
```

Responsibilities:

* retrieve execution history
* retrieve execution details
* evaluate retry eligibility
* expose execution metrics
* expose safety state
* expose provider health
* expose sanitized evidence
* initiate explicitly authorized retry
* recover stale execution runs
* enforce execution ownership

---

# 15. Execution Repository

Extend or create:

```text
BrowserExecutionRunRepository
```

Required queries:

```text
findByIdAndUserId(...)
findByApplicationIdAndUserId(...)
findByUserId(...)
findByStatus(...)
findStaleExecutions(...)
findRecentExecutions(...)
```

All application-level queries must enforce user ownership.

Never expose another user's execution record.

---

# 16. Stale Execution Recovery

A browser execution can become stale because of:

* browser crash
* application restart
* network failure
* provider timeout
* process termination
* lock timeout

M6-E must detect stale executions.

Example:

```text
EXECUTING
```

for longer than the configured execution timeout.

The system should transition it to:

```text
FAILED
```

with:

```text
failureReason = EXECUTION_TIMEOUT_OR_STALE_SESSION
retryEligible = true
```

unless evidence indicates that submission may have succeeded.

---

# 17. Ambiguous Submission Protection

This is critical.

If the browser loses connection immediately after a possible submit interaction, the system must NOT assume:

```text
APPLIED
```

Instead:

```text
status = REQUIRES_REVIEW
```

or:

```text
status = UNKNOWN_OUTCOME
```

depending on the existing domain model.

The system must never create a false success.

The operator must manually verify the ATS result.

---

# 18. Provider Health

Create:

```text
ProviderHealthService
```

Greenhouse health should expose:

```text
provider
reachable
readOnlySupported
liveExecutionEnabled
lastSuccessfulCheck
lastFailure
latency
```

Provider health must not expose credentials.

Example:

```json
{
  "provider": "GREENHOUSE_PRODUCTION",
  "reachable": true,
  "readOnlySupported": true,
  "liveExecutionEnabled": false,
  "status": "HEALTHY"
}
```

---

# 19. Provider Failure Handling

If Greenhouse is unavailable:

```text
FAILED
```

or:

```text
ACTION_REQUIRED
```

must be returned.

Never:

```text
APPLIED
```

Never fall back to sandbox and claim success.

Sandbox fallback may only be offered as a separate explicit simulation.

---

# 20. Safety Configuration Service

Create:

```text
ExecutionSafetyConfigurationService
```

It must expose the current effective configuration.

Example:

```json
{
  "autoApply": false,
  "autoSendEmail": false,
  "autoLinkedIn": false,
  "allowLiveSubmission": false,
  "mode": "PRODUCTION_READ_ONLY"
}
```

The frontend must consume this from the backend.

Do not allow frontend-only safety enforcement.

---

# 21. Configuration Rules

The backend is authoritative.

If frontend says:

```text
ALLOW_LIVE_SUBMISSION=true
```

but backend says:

```text
false
```

the backend wins.

A user must never be able to bypass the server-side gate through:

* browser developer tools
* modified HTTP requests
* frontend state manipulation
* direct API calls
* URL parameters
* local storage
* query parameters

---

# 22. Operations Controller

Create:

```text
BrowserExecutionOperationsController
```

Base path:

```text
/api/v1/browser/execution
```

Endpoints:

### Execution history

```http
GET /api/v1/browser/execution/runs
```

Supports:

```text
status
provider
mode
applicationId
from
to
page
size
```

---

### Execution detail

```http
GET /api/v1/browser/execution/runs/{runId}
```

Returns sanitized execution evidence.

---

### Retry eligibility

```http
GET /api/v1/browser/execution/runs/{runId}/retry-eligibility
```

Example:

```json
{
  "eligible": false,
  "reason": "LIVE_SUBMISSION_DISABLED"
}
```

---

### Explicit retry

```http
POST /api/v1/browser/execution/runs/{runId}/retry
```

Retry must:

* require authentication
* verify ownership
* acquire distributed lock
* revalidate safety
* revalidate form fingerprint
* never bypass configuration
* never bypass candidate confirmation

---

### Provider health

```http
GET /api/v1/browser/execution/providers/health
```

---

### Safety configuration

```http
GET /api/v1/browser/execution/safety
```

---

### Execution metrics

```http
GET /api/v1/browser/execution/metrics
```

---

# 23. Retry API Response

Example:

```json
{
  "runId": "run-123",
  "status": "BLOCKED",
  "retryEligible": false,
  "reason": "LIVE_SUBMISSION_DISABLED",
  "submissionAttempted": false
}
```

---

# 24. Execution Metrics

Expose safe aggregate metrics.

Required metrics:

```text
totalExecutions
sandboxExecutions
readOnlyExecutions
liveExecutions
blockedExecutions
failedExecutions
successfulExecutions
unknownOutcomeExecutions
retryableExecutions
realSubmissions
emailsSent
filesUploaded
```

Current expected values:

```text
realSubmissions = 0
emailsSent = 0
```

while live execution is disabled.

---

# 25. Frontend — ATS Operations Center

Create:

```text
ATSOperationsPage.tsx
```

Suggested route:

```text
/ats-operations
```

The page must contain:

### Safety banner

Example:

```text
LIVE SUBMISSION DISABLED

The server is currently configured with
ALLOW_LIVE_SUBMISSION=false.

No external ATS submission can occur.
```

---

# 26. Operations Dashboard

Display:

```text
Execution Mode
Provider Health
Live Submission
Auto Apply
Auto Email
Auto LinkedIn
Total Runs
Blocked Runs
Failed Runs
Successful Runs
Unknown Outcomes
```

Use clear badges.

Example:

```text
PRODUCTION / READ-ONLY
```

and:

```text
LIVE EXECUTION: OFF
```

---

# 27. Execution History Table

Columns:

```text
Date
Application
Provider
Mode
Status
Safety Result
Submission Attempted
Retry
```

Example statuses:

```text
PREPARED
CONFIRMED
BLOCKED
FAILED
APPLIED
UNKNOWN_OUTCOME
```

---

# 28. Execution Detail Drawer

Selecting an execution should show:

```text
Execution ID
Application
Provider
Mode
Created At
Started At
Completed At
Status
Form Fingerprint
Fields Detected
Fields Mapped
Fields Requiring Review
Safety Checks
Failure Reason
Retry Eligibility
Submission Attempted
Email Sent
File Uploaded
```

---

# 29. Safety Check Viewer

Display every safety check.

Example:

```text
✓ Application exists
✓ User ownership
✓ Greenhouse domain
✓ Provider identity
✓ Candidate approval
✓ Form readiness
✓ Required fields resolved
✓ Field read-back verified
✓ Compliance fields resolved
✓ Local artifacts verified
✓ Distributed lock
✓ Server live-submission configuration
✓ Provider capability
✓ Audit persistence
✓ Form fingerprint
✓ Browser/application binding
```

If a check fails:

```text
✕ LIVE_SUBMISSION_DISABLED
```

with a human-readable explanation.

---

# 30. No Sensitive Values in UI

The operations UI must never display:

* access tokens
* refresh tokens
* passwords
* cookies
* authorization headers
* raw candidate secrets

Candidate field evidence must remain metadata-only.

---

# 31. Manual Recovery UI

For:

```text
UNKNOWN_OUTCOME
```

display:

```text
Submission outcome could not be verified.

Do not retry automatically.

Please verify the application directly in the ATS before continuing.
```

There must be no automatic retry.

---

# 32. Retry UI

Retry button must only appear when:

```text
retryEligible = true
```

Before retry:

* display reason
* display safety checks
* require explicit confirmation
* re-run server-side validation

If live submission is disabled:

```text
Retry unavailable:
Live submission is disabled by server configuration.
```

---

# 33. Distributed Locking

Use a lock such as:

```text
application-browser-operations:{applicationId}
```

or reuse the existing application-level execution lock where appropriate.

The system must prevent:

* duplicate execution
* concurrent retry
* concurrent preparation
* concurrent confirmation
* race-condition state transitions

Locks must be released in `finally`.

---

# 34. Idempotency

Execution operations must be idempotent where possible.

Repeated requests must not produce:

```text
duplicate submission
```

or:

```text
duplicate confirmation
```

or:

```text
duplicate execution
```

For retry operations, use an idempotency key or equivalent server-side protection.

---

# 35. Authorization

Every execution operation must verify:

```text
authenticated user
        ↓
application ownership
        ↓
execution ownership
        ↓
authorization
```

Never rely only on frontend route protection.

---

# 36. Tenant Isolation

If the application supports tenants/workspaces:

Every execution query must include tenant scope.

Required:

```text
tenantId
userId
```

where applicable.

Cross-tenant execution access must return:

```text
404
```

or an equivalent non-disclosing response.

Do not reveal that another tenant's execution exists.

---

# 37. Error Model

Use structured errors.

Examples:

```text
EXECUTION_NOT_FOUND
APPLICATION_NOT_FOUND
UNAUTHORIZED_EXECUTION
PROVIDER_UNAVAILABLE
FORM_FINGERPRINT_CHANGED
CANDIDATE_CONFIRMATION_REQUIRED
REQUIRED_FIELDS_UNRESOLVED
COMPLIANCE_REVIEW_REQUIRED
LIVE_SUBMISSION_DISABLED
EXECUTION_LOCKED
STALE_EXECUTION
UNKNOWN_OUTCOME
RETRY_NOT_ELIGIBLE
```

---

# 38. Logging

Application logs must be sanitized.

Allowed:

```text
executionRunId
applicationId
provider
mode
status
failureReason
```

Forbidden:

```text
password
token
cookie
authorization header
resume content
cover letter content
raw candidate answers
```

---

# 39. Database Migration

Create:

```text
V22__create_browser_execution_operations_schema.sql
```

Only create additional tables/columns that do not already exist.

Recommended table:

```text
browser_execution_events
```

Fields:

```text
id
execution_run_id
application_id
tenant_id
user_id
event_type
event_status
sanitized_reason
created_at
```

Indexes:

```text
execution_run_id
application_id
user_id
tenant_id
created_at
event_type
```

Foreign keys must reference existing domain entities.

---

# 40. Data Retention

Execution evidence must not grow without limit.

Create a configurable retention policy.

Example:

```text
browser.execution.audit-retention-days=365
```

Do not delete evidence required for active investigation.

Do not delete application state when deleting execution evidence.

---

# 41. Security Requirements

M6-E must verify:

* authenticated access
* authorization
* tenant isolation
* ownership
* encrypted credentials remain untouched
* secrets never appear in logs
* secrets never appear in API responses
* secrets never appear in execution evidence
* no client-side live execution override
* server-side configuration gate
* CSRF/authentication protections consistent with existing application
* distributed lock enforcement

---

# 42. Testing Requirements

All existing tests must continue passing.

Run:

```bash
./gradlew test
```

Expected:

```text
BUILD SUCCESSFUL
```

---

# 43. Backend Test Suite

Create:

```text
BrowserExecutionOperationsServiceTest
BrowserExecutionOperationsControllerTest
ExecutionSafetyConfigurationServiceTest
ProviderHealthServiceTest
BrowserExecutionRetryTest
BrowserExecutionRecoveryTest
BrowserExecutionAuthorizationTest
BrowserExecutionTenantIsolationTest
BrowserExecutionAuditTest
```

---

# 44. Mandatory Safety Tests

Test that:

### Test 1

Live execution disabled:

```text
ALLOW_LIVE_SUBMISSION=false
```

Result:

```text
BLOCKED
submissionAttempted=false
```

---

### Test 2

Frontend/client attempts override.

Result:

```text
BLOCKED
```

---

### Test 3

Wrong user accesses execution.

Result:

```text
404/unauthorized
```

---

### Test 4

Wrong tenant accesses execution.

Result:

```text
404
```

---

### Test 5

Fingerprint changes.

Result:

```text
BLOCKED
FORM_FINGERPRINT_CHANGED
```

---

### Test 6

Required field unresolved.

Result:

```text
BLOCKED
```

---

### Test 7

Compliance field unresolved.

Result:

```text
BLOCKED
```

---

### Test 8

Concurrent retry.

Result:

```text
EXECUTION_LOCKED
```

---

### Test 9

Stale execution.

Result:

```text
FAILED
retryEligible=true
```

---

### Test 10

Unknown outcome.

Result:

```text
UNKNOWN_OUTCOME
automaticRetry=false
```

---

### Test 11

Provider unavailable.

Result:

```text
FAILED
```

and never:

```text
APPLIED
```

---

### Test 12

Audit sanitization.

Assert:

```text
no token
no password
no cookie
no authorization header
```

---

# 45. Frontend Testing

Run:

```bash
npm run build
```

Must complete with:

```text
0 TypeScript errors
0 compilation errors
```

Verify:

* ATS Operations page loads
* safety banner is visible
* execution history loads
* execution detail opens
* safety checks display
* provider health displays
* blocked execution displays correctly
* retry button respects backend eligibility
* unknown outcome prevents automatic retry
* sensitive values are not displayed

---

# 46. Browser Verification

Use:

```text
http://localhost:5173
```

Verify:

### Dashboard

ATS Operations Center is accessible.

### Safety

Shows:

```text
LIVE SUBMISSION: OFF
AUTO APPLY: OFF
AUTO EMAIL: OFF
AUTO LINKEDIN: OFF
```

### Execution History

Existing M6-D runs are visible.

### Execution Detail

17 safety checks are visible.

### Blocked Run

A blocked live execution clearly shows:

```text
LIVE_SUBMISSION_DISABLED
submissionAttempted=false
```

### Retry

Retry is unavailable when:

```text
ALLOW_LIVE_SUBMISSION=false
```

### Unknown Outcome

Automatic retry is unavailable.

---

# 47. API Contract Example

## Safety

```http
GET /api/v1/browser/execution/safety
```

Response:

```json
{
  "autoApply": false,
  "autoSendEmail": false,
  "autoLinkedIn": false,
  "allowLiveSubmission": false,
  "executionMode": "PRODUCTION_READ_ONLY"
}
```

---

## Provider Health

```http
GET /api/v1/browser/execution/providers/health
```

Response:

```json
{
  "providers": [
    {
      "provider": "GREENHOUSE_PRODUCTION",
      "status": "HEALTHY",
      "reachable": true,
      "readOnlySupported": true,
      "liveExecutionEnabled": false
    }
  ]
}
```

---

## Execution Detail

```http
GET /api/v1/browser/execution/runs/{runId}
```

Response:

```json
{
  "runId": "run-123",
  "applicationId": "app-123",
  "provider": "GREENHOUSE_PRODUCTION",
  "mode": "PRODUCTION_READ_ONLY",
  "status": "BLOCKED",
  "submissionAttempted": false,
  "emailSent": false,
  "fileUploaded": false,
  "retryEligible": false,
  "failureReason": "LIVE_SUBMISSION_DISABLED"
}
```

---

# 48. Architecture

Target architecture:

```text
Frontend
   |
   v
BrowserExecutionOperationsController
   |
   v
BrowserExecutionOperationsService
   |
   +-----------------------------+
   |                             |
   v                             v
SafetyConfigurationService    ProviderHealthService
   |
   v
LiveSubmissionSafetyGate
   |
   v
LiveBrowserExecutionService
   |
   v
BrowserExecutionRunRepository
   |
   v
Audit / Execution Events
```

Existing M6-A/B/C services must remain reusable.

Do not duplicate browser logic.

---

# 49. Existing Service Reuse

Before creating new services, inspect and reuse:

```text
BrowserSafetyPolicy
BrowserDiscoveryService
BrowserInteractionService
SandboxExecutionService
SandboxSubmissionVerificationService
LiveSubmissionSafetyGate
LiveBrowserExecutionService
BrowserExecutionRunRepository
ProductionProviderRegistry
```

Do not create duplicate implementations of existing safety logic.

---

# 50. Transaction Boundaries

Database state transitions must be transactional.

Examples:

```text
CONFIRMED
→ SAFETY_VALIDATION
```

and:

```text
FAILED
→ retry preparation
```

must not leave inconsistent partial state.

Audit events must be persisted reliably.

---

# 51. Concurrency

Protect against:

* two browser executions for one application
* retry during execution
* confirmation during execution
* execution after application ownership changes
* execution after approval becomes invalid
* simultaneous form preparation and execution

Use distributed locks plus database/state validation.

---

# 52. Observability

Add structured operational logging.

Metrics should allow detection of:

```text
blocked executions
failed executions
provider failures
stale executions
unknown outcomes
retry attempts
safety check failures
```

Do not log candidate secrets.

---

# 53. UX Principles

The UI must make safety obvious.

Never use ambiguous text such as:

```text
Run
Continue
Apply
```

when the operation could be confused with real submission.

Prefer:

```text
Inspect Form
Review Safety
View Execution
Retry Verification
```

When live execution is disabled:

```text
Live submission is disabled by server configuration.
```

must be clearly visible.

---

# 54. No Fake Success

This requirement is absolute.

If an external ATS was not actually submitted:

Never display:

```text
Application Submitted
```

Instead display:

```text
Submission Not Attempted
```

or:

```text
Submission Blocked
```

or:

```text
Outcome Requires Review
```

depending on the actual result.

---

# 55. Definition of Done

M6-E is complete only when:

### Backend

* execution operations service implemented
* execution history implemented
* execution detail implemented
* provider health implemented
* safety configuration API implemented
* retry eligibility implemented
* stale execution handling implemented
* unknown outcome handling implemented
* audit events implemented
* authorization implemented
* tenant isolation implemented
* sanitized logging implemented

### Frontend

* ATS Operations Center implemented
* execution history implemented
* execution detail implemented
* safety check viewer implemented
* provider health implemented
* retry eligibility UI implemented
* blocked execution UI implemented
* unknown outcome UI implemented

### Safety

* `ALLOW_LIVE_SUBMISSION=false`
* `AUTO_APPLY=false`
* `AUTO_SEND_EMAIL=false`
* `AUTO_LINKEDIN=false`
* no autonomous submission
* no autonomous retry
* no email
* no LinkedIn messaging
* no real file upload unless explicitly authorized by an existing safe provider contract
* no secrets in logs
* no secrets in API responses

### Verification

```bash
./gradlew test
npm run build
```

must both pass.

Browser verification must be completed.

---

# 56. Required Final Verification Report

Antigravity must provide a final report containing:

```text
M6-E Implementation Status

Backend Tests:
Frontend Build:
Browser Verification:

Execution Operations:
YES/NO

Provider Health:
YES/NO

Retry Safety:
YES/NO

Unknown Outcome Protection:
YES/NO

Tenant Isolation:
YES/NO

Audit Sanitization:
YES/NO

Live Submission:
0

Emails Sent:
0

LinkedIn Messages Sent:
0

Real File Uploads:
0
```

Also provide:

```text
Files Created
Files Modified
Database Migration
API Endpoints
Frontend Components
Safety Checks
Commit Hash
```

---

# 57. Mandatory Stop Gate

M6-E must end with a stop gate.

Do NOT:

* start M6-F
* enable live submission
* remove `ALLOW_LIVE_SUBMISSION=false`
* add another ATS provider
* add autonomous application
* add LinkedIn automation
* add email sending
* bypass human confirmation

After M6-E verification, stop and report the result.

The next milestone requires explicit approval.

---

# 58. Implementation Order

Implement in this exact order:

## Step 1 — Inspect Existing Architecture

Read:

```text
M6-A implementation
M6-B implementation
M6-C implementation
M6-D implementation
```

Inspect existing:

```text
BrowserExecutionRun
BrowserExecutionRunRepository
LiveSubmissionSafetyGate
LiveBrowserExecutionService
Control Center
Application domain state
audit infrastructure
```

Do not duplicate existing logic.

---

## Step 2 — Database

Create:

```text
V22__create_browser_execution_operations_schema.sql
```

Add only required operational/audit structures.

---

## Step 3 — Backend Services

Implement:

```text
BrowserExecutionOperationsService
ExecutionSafetyConfigurationService
ProviderHealthService
```

---

## Step 4 — Retry & Recovery

Implement:

```text
retry eligibility
stale execution detection
unknown outcome handling
distributed locking
idempotency
```

---

## Step 5 — APIs

Implement:

```text
GET /api/v1/browser/execution/runs
GET /api/v1/browser/execution/runs/{runId}
GET /api/v1/browser/execution/runs/{runId}/retry-eligibility
POST /api/v1/browser/execution/runs/{runId}/retry
GET /api/v1/browser/execution/providers/health
GET /api/v1/browser/execution/safety
GET /api/v1/browser/execution/metrics
```

---

## Step 6 — Tests

Implement all backend safety, authorization, retry, recovery, audit, and tenant-isolation tests.

Run:

```bash
./gradlew test
```

Fix all failures.

---

## Step 7 — Frontend

Create:

```text
ATSOperationsPage.tsx
ExecutionHistoryTable.tsx
ExecutionDetailDrawer.tsx
SafetyCheckViewer.tsx
ProviderHealthCard.tsx
ExecutionSafetyBanner.tsx
```

Reuse existing design system/components.

---

## Step 8 — Frontend Build

Run:

```bash
npm run build
```

Fix all TypeScript/build issues.

---

## Step 9 — Browser Verification

Verify the complete Operations Center at:

```text
http://localhost:5173
```

Confirm live execution remains disabled.

---

## Step 10 — Final Safety Audit

Explicitly verify:

```text
ALLOW_LIVE_SUBMISSION=false
AUTO_APPLY=false
AUTO_SEND_EMAIL=false
AUTO_LINKEDIN=false
```

Verify:

```text
real submissions = 0
emails sent = 0
LinkedIn messages = 0
```

---

## Step 11 — Commit

Create one clean commit:

```text
feat(phase6): implement M6-E ATS execution operations and safety control center
```

---

# 59. Final Acceptance Criteria

M6-E passes only if all are true:

* [ ] Execution history works
* [ ] Execution details work
* [ ] Provider health works
* [ ] Safety configuration works
* [ ] Retry eligibility works
* [ ] Retry cannot bypass safety
* [ ] Stale execution is detected
* [ ] Unknown outcome is protected
* [ ] Form fingerprint changes block execution
* [ ] User ownership is enforced
* [ ] Tenant isolation is enforced
* [ ] Audit events are sanitized
* [ ] Sensitive credentials never appear in evidence
* [ ] No fake success exists
* [ ] No autonomous retry exists
* [ ] Live submission remains disabled
* [ ] Auto apply remains disabled
* [ ] Auto email remains disabled
* [ ] Auto LinkedIn remains disabled
* [ ] Backend tests pass
* [ ] Frontend build passes
* [ ] Browser verification passes
* [ ] Final safety audit passes
* [ ] Commit created
* [ ] Antigravity stops after M6-E

---

# 60. Final Instruction to Antigravity

Implement this milestone completely.

Treat the existing M6-A, M6-B, M6-C, and M6-D implementations as authoritative.

Reuse existing architecture wherever possible.

Do not rewrite working browser infrastructure.

Do not weaken any existing safety gate.

Do not enable live submission.

Do not implement autonomous applications.

Do not send email.

Do not send LinkedIn messages.

Do not bypass candidate confirmation.

Do not fabricate application success.

Do not use sandbox fallback to represent real execution success.

All external execution must remain explicitly controlled and server-gated.

Run the complete backend test suite.

Run the frontend build.

Perform browser verification.

Perform the final safety audit.

Create the M6-E commit.

Then STOP.

Report exactly what was implemented, tested, verified, and the final safety status.
