# Phase 6 — M6-F PRD

## Controlled End-to-End Application Execution & Final Human-Gated Submission

**Project:** AI Career OS
**Phase:** 6 — Browser-Based Application Execution
**Milestone:** M6-F
**Status:** Ready for Implementation
**Primary ATS:** Greenhouse
**Execution Model:** End-to-End Controlled Human-Gated Execution
**Default Live Execution:** OFF

---

# 1. Objective

M6-F is the final milestone of Phase 6.

The objective is to connect the complete browser-based application workflow into one controlled end-to-end experience:

```text
Job
 ↓
Application
 ↓
Greenhouse Detection
 ↓
Read-Only Form Discovery
 ↓
Candidate Fact Mapping
 ↓
Sandbox Verification
 ↓
Production Read-Only Preparation
 ↓
Form Fingerprint
 ↓
17 Safety Checks
 ↓
Candidate Review
 ↓
Explicit Confirmation
 ↓
Execution Decision
 ↓
Blocked / Sandbox / Controlled Live Path
 ↓
Verification
 ↓
Audit
```

M6-F must provide a single, reliable workflow that allows a user to understand exactly what will happen before any external action is possible.

The system must remain **human-controlled**.

---

# 2. Phase 6 Completion Goal

After M6-F, the system should have a complete browser application execution architecture consisting of:

```text
M6-A
Browser Infrastructure
        ↓
M6-B
Safe Browser Interaction
        ↓
M6-C
Sandbox Execution
        ↓
M6-D
Controlled Greenhouse Preparation
        ↓
M6-E
Execution Operations & Safety Control Center
        ↓
M6-F
End-to-End Controlled Application Workflow
```

M6-F should primarily integrate and harden the existing capabilities rather than duplicate them.

---

# 3. Existing Capabilities

The following are already implemented and must be reused.

## M6-A

* Playwright browser session
* browser safety policy
* domain validation
* redirect validation
* form discovery
* field detection
* selectors
* distributed locking
* audit

## M6-B

* candidate fact mapping
* field interaction
* DOM event dispatch
* read-back verification
* unresolved-field detection
* review-required handling
* submit control detection

## M6-C

* sandbox execution
* sandbox verification
* mock submission
* execution persistence
* sandbox status
* sandbox audit

## M6-D

* Greenhouse production read-only preparation
* real ATS form discovery
* form fingerprint
* preview
* candidate confirmation
* 17 safety checks
* controlled execution endpoint
* live submission gate

## M6-E

* ATS Operations Center
* execution history
* execution evidence
* provider health
* safety configuration
* retry eligibility
* stale execution recovery
* execution metrics
* sanitized audit
* authorization and tenant isolation

---

# 4. Core Safety Principle

M6-F must NOT weaken any existing safety control.

The following must remain the authoritative server configuration:

```text
ALLOW_LIVE_SUBMISSION=false
AUTO_APPLY=false
AUTO_SEND_EMAIL=false
AUTO_LINKEDIN=false
```

No frontend control may override these values.

No API parameter may override them.

No database value may silently override them.

No browser script may bypass them.

No development/demo mode may accidentally enable them.

---

# 5. M6-F Scope

M6-F includes:

### Backend

* end-to-end execution orchestrator
* application workflow state
* execution readiness evaluation
* unified execution session
* final safety gate integration
* sandbox/read-only/live mode routing
* final verification
* end-to-end audit
* execution evidence
* failure handling
* recovery handling

### Frontend

* unified application execution workspace
* workflow stepper
* candidate review
* form review
* safety review
* execution decision
* execution result
* evidence
* links to ATS Operations

### Testing

* full end-to-end backend tests
* API integration tests
* frontend build
* browser verification
* safety regression suite

---

# 6. Explicit Non-Goals

M6-F must NOT implement:

* autonomous job applications
* automatic candidate approval
* automatic candidate confirmation
* automatic live execution
* automatic retry
* CAPTCHA bypass
* bot detection bypass
* stealth browser automation
* proxy rotation
* fingerprint evasion
* LinkedIn messaging
* outbound email
* fabricated candidate answers
* fabricated application success
* automatic legal/compliance answers
* unrestricted file upload
* credential extraction
* credential logging
* secret exposure

---

# 7. Unified Application Execution Workflow

Create a unified workflow:

```text
STEP 1
Application Selected

STEP 2
Provider & URL Validation

STEP 3
Read-Only Form Inspection

STEP 4
Candidate Field Mapping

STEP 5
Sandbox Verification

STEP 6
Production Read-Only Preparation

STEP 7
Safety Review

STEP 8
Candidate Confirmation

STEP 9
Execution Decision

STEP 10
Execution

STEP 11
Verification

STEP 12
Completion / Review Required
```

The workflow must be resumable where safe.

---

# 8. Step 1 — Application Selection

The user selects an existing application.

Validate:

```text
application exists
user owns application
tenant is valid
job exists
job URL exists
application is executable
```

If validation fails:

```text
ACTION_REQUIRED
```

No browser execution should begin.

---

# 9. Step 2 — Provider & URL Validation

Validate:

```text
provider
hostname
protocol
redirect policy
job URL
```

Greenhouse domains:

```text
greenhouse.io
boards.greenhouse.io
```

Only HTTPS external URLs are permitted.

Localhost remains available for development/testing.

Unknown domains must produce:

```text
UNSUPPORTED_PROVIDER
```

No execution.

---

# 10. Step 3 — Read-Only Form Inspection

Invoke existing M6-A/M6-D discovery.

Expected output:

```json
{
  "provider": "GREENHOUSE_PRODUCTION",
  "mode": "PRODUCTION_READ_ONLY",
  "fieldsDetected": 18,
  "requiredFields": 12,
  "submitControlsDetected": 1,
  "submissionAttempted": false
}
```

The submit control may be detected.

It must not be clicked during inspection.

---

# 11. Step 4 — Candidate Field Mapping

Invoke M6-B.

Map only verified candidate facts.

Supported:

```text
text
email
phone
URL
textarea
select
radio
checkbox
```

Every mapping must have:

```text
candidateFact
targetField
mappingReason
verificationStatus
```

Do not invent missing facts.

---

# 12. Unknown Candidate Facts

If a required field cannot be answered from verified candidate data:

```text
REQUIRES_REVIEW
```

Examples:

```text
work authorization
visa sponsorship
veteran status
disability status
salary expectations
legal declarations
custom employer questions
```

The system must never guess.

---

# 13. Step 5 — Sandbox Verification

Invoke M6-C.

Sandbox verification must validate:

```text
field discovery
field mapping
field interaction
read-back verification
required fields
unsupported fields
submission simulation
safety invariants
```

Expected:

```text
realSubmissionAttempted=false
emailSent=false
fileUploadedToRealProvider=false
```

A failed sandbox verification blocks progression.

---

# 14. Step 6 — Production Read-Only Preparation

Invoke M6-D.

Generate:

```text
previewId
formFingerprint
executionRunId
```

The system must capture the current production form structure.

No submission occurs.

---

# 15. Form Fingerprint

The prepared fingerprint must be bound to the execution attempt.

If the form changes after preparation:

```text
FORM_FINGERPRINT_CHANGED
```

The system must stop.

The user must perform a new read-only preparation.

Never blindly continue with stale selectors.

---

# 16. Step 7 — Final Safety Review

Before confirmation, display:

### Application

```text
Company
Job Title
Job URL
Provider
```

### Form

```text
Fields detected
Fields mapped
Fields requiring review
Unsupported fields
```

### Candidate Data

Display metadata only.

Do not expose unnecessary sensitive information.

### Execution Mode

```text
PRODUCTION / READ-ONLY
```

### Safety Flags

```text
AUTO_APPLY: OFF
AUTO_SEND_EMAIL: OFF
AUTO_LINKEDIN: OFF
ALLOW_LIVE_SUBMISSION: OFF
```

---

# 17. Safety Review Must Be Human-Readable

Do not display only:

```text
CHECK_1 = PASS
CHECK_2 = PASS
```

Instead display:

```text
✓ Application belongs to you
✓ Greenhouse provider verified
✓ Job URL validated
✓ Required fields resolved
✓ Candidate mappings verified
✓ Compliance questions reviewed
✓ Form fingerprint current
✓ Browser session bound
✕ Live submission disabled by server configuration
```

---

# 18. Step 8 — Candidate Confirmation

Candidate confirmation must remain explicit.

The UI must require the user to acknowledge:

```text
I reviewed the application data and authorize the next execution step.
```

The confirmation must be persisted server-side.

Frontend-only checkbox state is insufficient.

---

# 19. Confirmation Requirements

Confirmation must bind to:

```text
userId
applicationId
executionRunId
previewId
formFingerprint
timestamp
```

If any of these changes materially:

```text
confirmation invalid
```

The user must confirm again.

---

# 20. Step 9 — Execution Decision

The backend determines the execution path.

Possible outcomes:

```text
SANDBOX
PRODUCTION_READ_ONLY
PRODUCTION_LIVE
BLOCKED
REQUIRES_REVIEW
```

The frontend must not determine the execution mode.

---

# 21. Current Expected Live Decision

Because:

```text
ALLOW_LIVE_SUBMISSION=false
```

the current production result must be:

```text
BLOCKED
```

with:

```text
submissionAttempted=false
```

Example:

```json
{
  "status": "BLOCKED",
  "reason": "LIVE_SUBMISSION_DISABLED",
  "submissionAttempted": false
}
```

---

# 22. Step 10 — Execution

The execution orchestrator must call the existing execution service.

Create:

```text
ControlledApplicationExecutionService
```

Suggested package:

```text
com.ai.career.browser.execution
```

Responsibilities:

* orchestrate the complete workflow
* invoke existing services
* validate state
* enforce ordering
* acquire execution lock
* execute the selected mode
* record audit events
* invoke verification
* return final execution result

---

# 23. Do Not Duplicate Existing Services

Reuse:

```text
BrowserSafetyPolicy
BrowserDiscoveryService
BrowserInteractionService
SandboxExecutionService
SandboxSubmissionVerificationService
LiveSubmissionSafetyGate
LiveBrowserExecutionService
BrowserExecutionOperationsService
ExecutionSafetyConfigurationService
ProviderHealthService
```

M6-F should be an orchestration/integration layer.

Do not reimplement their safety checks independently.

---

# 24. End-to-End Execution API

Create:

```http
POST /api/v1/applications/{id}/browser/execute
```

Request:

```json
{
  "mode": "PRODUCTION_READ_ONLY",
  "previewId": "preview-123",
  "confirmationId": "confirmation-123"
}
```

The backend must ignore any attempt to force an unsafe mode.

---

# 25. Execution API Response

Example:

```json
{
  "executionRunId": "run-123",
  "applicationId": "app-123",
  "provider": "GREENHOUSE_PRODUCTION",
  "mode": "PRODUCTION_READ_ONLY",
  "status": "BLOCKED",
  "reason": "LIVE_SUBMISSION_DISABLED",
  "submissionAttempted": false,
  "emailSent": false,
  "fileUploaded": false,
  "verificationStatus": "NOT_APPLICABLE"
}
```

---

# 26. Execution Status API

Create:

```http
GET /api/v1/applications/{id}/browser/execution/status
```

Return:

```text
currentExecution
latestRun
workflowStep
provider
mode
status
safetyStatus
retryEligibility
verificationStatus
```

---

# 27. Unified Workflow Status

Suggested statuses:

```text
NOT_STARTED
INSPECTING
MAPPING
SANDBOX_VERIFYING
PREPARING
AWAITING_REVIEW
AWAITING_CONFIRMATION
SAFETY_VALIDATING
EXECUTING
VERIFYING
BLOCKED
FAILED
REQUIRES_REVIEW
COMPLETED
```

---

# 28. Execution State Rules

Valid progression:

```text
NOT_STARTED
    ↓
INSPECTING
    ↓
MAPPING
    ↓
SANDBOX_VERIFYING
    ↓
PREPARING
    ↓
AWAITING_REVIEW
    ↓
AWAITING_CONFIRMATION
    ↓
SAFETY_VALIDATING
    ↓
EXECUTING
    ↓
VERIFYING
    ↓
COMPLETED
```

Failure branches:

```text
ANY SAFE STEP
   ↓
FAILED

ANY SAFETY FAILURE
   ↓
BLOCKED

UNKNOWN EXTERNAL OUTCOME
   ↓
REQUIRES_REVIEW
```

---

# 29. No False Completion

The system must never mark:

```text
COMPLETED
```

unless the actual workflow outcome is known.

If live submission is disabled:

```text
BLOCKED
```

If external outcome is ambiguous:

```text
REQUIRES_REVIEW
```

If sandbox only:

```text
SANDBOX_VERIFIED
```

must be distinguishable from actual ATS submission.

---

# 30. Final Verification

Create/reuse:

```text
ApplicationExecutionVerificationService
```

It must verify:

```text
execution completed
expected provider state
submission result
browser state
application state
audit persisted
```

For a real submission, if live execution is ever enabled in a future controlled milestone, verification must be based on actual provider evidence.

Never infer success merely because a browser click occurred.

---

# 31. Unknown Outcome

If browser/network failure occurs after a potential external submission action:

```text
UNKNOWN_OUTCOME
```

must be used.

Do not automatically retry.

Display:

```text
The external application outcome could not be verified.

Do not retry until the application is manually verified in the ATS.
```

---

# 32. Application State Synchronization

The browser execution result must synchronize with the application domain.

Example:

```text
BLOCKED
```

must not set:

```text
APPLIED
```

Example:

```text
SANDBOX_VERIFIED
```

must not set:

```text
APPLIED
```

Only a verified actual provider submission may result in:

```text
APPLIED
```

---

# 33. Audit Trail

The complete workflow must generate correlated audit events.

Required:

```text
APPLICATION_EXECUTION_STARTED
PROVIDER_VALIDATED
FORM_INSPECTION_STARTED
FORM_INSPECTION_COMPLETED
CANDIDATE_MAPPING_COMPLETED
SANDBOX_VERIFICATION_COMPLETED
PRODUCTION_PREPARATION_COMPLETED
SAFETY_REVIEW_STARTED
CANDIDATE_CONFIRMATION_RECORDED
SAFETY_VALIDATION_COMPLETED
EXECUTION_BLOCKED
EXECUTION_STARTED
EXECUTION_FAILED
EXECUTION_VERIFICATION_COMPLETED
APPLICATION_EXECUTION_COMPLETED
```

Every event must reference:

```text
executionRunId
applicationId
userId
tenantId
```

where applicable.

---

# 34. Audit Correlation

All events for a single workflow must share:

```text
executionRunId
```

This enables the Operations Center to display the entire lifecycle as one execution timeline.

---

# 35. Execution Timeline

The Operations Center should display:

```text
10:20 Application selected
10:21 Greenhouse verified
10:21 Form inspected
10:22 Candidate fields mapped
10:23 Sandbox verified
10:24 Production form prepared
10:24 Safety review completed
10:25 Candidate confirmed
10:25 Live execution blocked
```

Do not expose sensitive candidate values.

---

# 36. Frontend — Unified Application Execution Workspace

Create:

```text
ControlledApplicationExecutionPage.tsx
```

Suggested route:

```text
/applications/{id}/execute
```

Reuse existing application workspace components where possible.

---

# 37. Workflow Stepper

Display:

```text
1 Application
2 Inspect
3 Map
4 Sandbox
5 Prepare
6 Review
7 Confirm
8 Execute
9 Verify
```

Current step should be obvious.

Completed steps should show successful verification.

Blocked steps should show the reason.

---

# 38. Review Panel

The review screen must summarize:

```text
Job
Provider
Form
Mapped fields
Unresolved fields
Safety checks
Execution mode
Live execution status
```

The user must not need to inspect backend logs to understand what will happen.

---

# 39. Confirmation Panel

Display:

```text
Candidate Confirmation Required
```

Checkbox:

```text
I reviewed the application data and confirm that the mapped information is correct.
```

Button:

```text
Confirm Application Review
```

The button must not directly submit to the ATS.

---

# 40. Final Execution Panel

Before execution:

```text
Execution Mode:
PRODUCTION / READ-ONLY
```

Display:

```text
LIVE SUBMISSION: DISABLED
```

Button should be:

```text
Run Controlled Validation
```

rather than:

```text
Apply Now
```

when live submission is disabled.

---

# 41. Blocked State

When execution is blocked:

```text
LIVE SUBMISSION BLOCKED
```

Show:

```text
Reason:
Live submission is disabled by server configuration.

No external application was submitted.
No email was sent.
No LinkedIn message was sent.
No real file was uploaded.
```

---

# 42. Sandbox Shortcut

The user may be offered:

```text
Run Sandbox Verification
```

This must be clearly labeled as simulation.

Example:

```text
SANDBOX ONLY

This does not submit an application to Greenhouse.
```

---

# 43. Evidence Panel

After execution display:

```text
Execution ID
Mode
Provider
Status
Safety result
Fields detected
Fields mapped
Fields requiring review
Submission attempted
Email sent
File uploaded
Verification result
```

Provide:

```text
View Full Execution Evidence
```

which opens the sanitized M6-E evidence view.

---

# 44. Operations Center Integration

Add:

```text
View in ATS Operations
```

The user should be able to navigate from an application execution directly to its execution run in:

```text
/ats-operations
```

The execution run must retain correlation.

---

# 45. Provider Health Integration

Before production preparation:

Check:

```text
GREENHOUSE_PRODUCTION
```

If unavailable:

```text
PROVIDER_UNAVAILABLE
```

Stop the workflow.

Do not silently switch to another provider.

---

# 46. Execution Lock

Acquire a distributed lock:

```text
application-browser-end-to-end:{applicationId}
```

The lock must cover the critical execution workflow.

Prevent:

* duplicate execution
* concurrent retry
* conflicting preparation
* conflicting confirmation
* multiple browser sessions against the same application

Always release the lock.

---

# 47. Idempotency

End-to-end execution must be idempotent.

Repeated requests with the same execution context must not create duplicate external actions.

Use:

```text
executionRunId
confirmationId
idempotencyKey
```

as appropriate.

---

# 48. Authorization

Every endpoint must verify:

```text
authenticated user
application ownership
execution ownership
tenant
confirmation ownership
```

Never trust IDs supplied by the client.

---

# 49. Security

Never return:

```text
API keys
OAuth tokens
refresh tokens
passwords
cookies
authorization headers
browser session data
```

Never log them.

Never include them in:

```text
execution evidence
audit events
frontend responses
exception messages
```

---

# 50. Candidate Data Safety

Only verified candidate facts may enter the browser.

For every mapped field:

```text
sourceFactId
verified=true
```

If no verified fact exists:

```text
DO NOT FILL
```

Do not use LLM-generated guesses as candidate facts.

---

# 51. Compliance Question Safety

The system must never automatically answer unknown questions involving:

```text
work authorization
visa sponsorship
veteran status
disability
criminal/legal declarations
salary expectations
citizenship
security clearance
demographic information
other legally sensitive questions
```

These must require explicit review.

---

# 52. File Upload Safety

M6-F must not introduce unrestricted file upload.

If an existing provider capability supports a file:

The system must verify:

```text
local artifact exists
artifact belongs to current application
artifact is approved
artifact path is local
artifact type is allowed
artifact is not remote/untrusted
```

If verification fails:

```text
REQUIRES_REVIEW
```

No upload.

---

# 53. Browser Safety

The browser may only operate within approved domains.

Every navigation and redirect must pass:

```text
BrowserSafetyPolicy
```

Do not bypass this policy.

Do not use:

```text
page.goto(unvalidatedUrl)
```

for external execution.

---

# 54. Error Handling

All workflow failures must produce structured error codes.

Required:

```text
APPLICATION_NOT_FOUND
UNAUTHORIZED_APPLICATION
UNSUPPORTED_PROVIDER
INVALID_JOB_URL
PROVIDER_UNAVAILABLE
FORM_DISCOVERY_FAILED
FORM_FINGERPRINT_CHANGED
FIELD_MAPPING_FAILED
REQUIRED_FIELD_UNRESOLVED
COMPLIANCE_REVIEW_REQUIRED
SANDBOX_VERIFICATION_FAILED
CANDIDATE_CONFIRMATION_REQUIRED
SAFETY_CHECK_FAILED
LIVE_SUBMISSION_DISABLED
EXECUTION_LOCKED
EXECUTION_TIMEOUT
UNKNOWN_OUTCOME
VERIFICATION_FAILED
```

---

# 55. Recovery

Safe recovery actions:

```text
Retry read-only inspection
Refresh form fingerprint
Re-run sandbox verification
Re-review candidate mappings
Re-confirm application
```

Unsafe recovery:

```text
Automatically retry live submission
```

must never occur.

---

# 56. Database Changes

Only add a new migration if required.

Suggested:

```text
V23__create_controlled_application_execution_schema.sql
```

Possible table:

```text
controlled_application_executions
```

Fields:

```text
id
application_id
user_id
tenant_id
execution_run_id
preview_id
confirmation_id
form_fingerprint
workflow_status
execution_mode
current_step
failure_code
failure_reason
verification_status
submission_attempted
email_sent
file_uploaded
created_at
updated_at
completed_at
```

Do not duplicate `browser_execution_runs` unnecessarily.

If the existing M6-D/M6-E schema already contains equivalent fields, extend existing structures instead.

---

# 57. Repository

Create only if needed:

```text
ControlledApplicationExecutionRepository
```

Required operations:

```text
findByIdAndUserId(...)
findByApplicationIdAndUserId(...)
findActiveByApplicationId(...)
findLatestByApplicationId(...)
```

Tenant isolation is mandatory.

---

# 58. End-to-End Service

Create:

```text
ControlledApplicationExecutionService
```

Suggested methods:

```text
startExecution(...)
inspectApplication(...)
mapCandidateFields(...)
runSandboxVerification(...)
prepareProductionExecution(...)
getSafetyReview(...)
confirmCandidate(...)
execute(...)
verify(...)
getStatus(...)
```

Avoid exposing internal browser implementation details to controllers.

---

# 59. Controller

Create:

```text
ControlledApplicationExecutionController
```

Base path:

```text
/api/v1/applications/{applicationId}/browser
```

Endpoints:

```text
POST /execute/start
POST /execute/inspect
POST /execute/sandbox
POST /execute/prepare
GET  /execute/review
POST /execute/confirm
POST /execute/run
GET  /execute/status
GET  /execute/evidence
```

If equivalent endpoints already exist from M6-A through M6-E, consolidate through orchestration rather than creating duplicate APIs.

---

# 60. Recommended API Flow

```text
POST /execute/start
        ↓
POST /execute/inspect
        ↓
POST /execute/sandbox
        ↓
POST /execute/prepare
        ↓
GET /execute/review
        ↓
POST /execute/confirm
        ↓
POST /execute/run
        ↓
GET /execute/status
        ↓
GET /execute/evidence
```

The backend must enforce sequence.

A client cannot skip:

```text
sandbox
preparation
review
confirmation
```

by directly calling execution.

---

# 61. State Transition Protection

Invalid transition:

```text
NOT_STARTED → EXECUTING
```

must be rejected.

Invalid transition:

```text
PREPARING → APPLIED
```

must be rejected.

Invalid transition:

```text
BLOCKED → APPLIED
```

must be rejected.

Only valid transitions defined by the state machine are permitted.

---

# 62. Final Safety Gate

Before `EXECUTING`, run all existing safety validation.

The system must verify:

```text
1 Application exists
2 User ownership
3 Domain validation
4 Provider identity
5 State transition
6 Candidate approval
7 Form readiness
8 Required fields
9 Read-back verification
10 Compliance fields
11 Local artifacts
12 Distributed lock
13 Server configuration
14 Provider capability
15 Audit persistence
16 Form fingerprint
17 Browser/application binding
```

Do not reduce the existing 17 checks.

---

# 63. Safety Check Result

Example:

```json
{
  "passed": 16,
  "failed": 1,
  "total": 17,
  "result": "BLOCKED",
  "failedChecks": [
    {
      "code": "LIVE_SUBMISSION_DISABLED",
      "message": "Live submission is disabled by server configuration."
    }
  ]
}
```

---

# 64. Final Result Types

Use explicit result types:

```text
SANDBOX_VERIFIED
READ_ONLY_VERIFIED
BLOCKED
FAILED
REQUIRES_REVIEW
UNKNOWN_OUTCOME
APPLIED
```

`APPLIED` must only represent an actually verified external application.

---

# 65. Metrics

Extend M6-E metrics with:

```text
endToEndExecutions
sandboxVerified
readOnlyVerified
blocked
failed
requiresReview
unknownOutcome
applied
```

Safety counters remain:

```text
realSubmissions
emailsSent
linkedInMessages
realFilesUploaded
```

while live execution is disabled:

```text
realSubmissions = 0
emailsSent = 0
linkedInMessages = 0
```

---

# 66. Backend Tests

Create:

```text
ControlledApplicationExecutionServiceTest
ControlledApplicationExecutionControllerTest
ControlledApplicationExecutionStateTest
ControlledApplicationExecutionSafetyTest
ControlledApplicationExecutionConcurrencyTest
ControlledApplicationExecutionAuthorizationTest
ControlledApplicationExecutionIntegrationTest
```

---

# 67. Mandatory End-to-End Test

Test:

```text
Application
 ↓
Inspection
 ↓
Mapping
 ↓
Sandbox
 ↓
Preparation
 ↓
Review
 ↓
Confirmation
 ↓
Execution
```

With:

```text
ALLOW_LIVE_SUBMISSION=false
```

Expected:

```text
BLOCKED
submissionAttempted=false
```

---

# 68. Mandatory Safety Regression Tests

Verify:

### Test 1

Attempt direct execution without confirmation.

Expected:

```text
CANDIDATE_CONFIRMATION_REQUIRED
```

### Test 2

Attempt execution with stale fingerprint.

Expected:

```text
FORM_FINGERPRINT_CHANGED
```

### Test 3

Attempt execution with unresolved required field.

Expected:

```text
REQUIRED_FIELD_UNRESOLVED
```

### Test 4

Attempt execution with unresolved compliance question.

Expected:

```text
COMPLIANCE_REVIEW_REQUIRED
```

### Test 5

Attempt execution with wrong user.

Expected:

```text
UNAUTHORIZED
```

### Test 6

Attempt concurrent execution.

Expected:

```text
EXECUTION_LOCKED
```

### Test 7

Attempt client-side live-mode override.

Expected:

```text
LIVE_SUBMISSION_DISABLED
```

### Test 8

Attempt direct `APPLIED` state manipulation.

Expected:

```text
REJECTED
```

### Test 9

Provider unavailable.

Expected:

```text
PROVIDER_UNAVAILABLE
```

### Test 10

Unknown external outcome.

Expected:

```text
UNKNOWN_OUTCOME
automaticRetry=false
```

---

# 69. Frontend Tests

Verify:

* execution workspace loads
* workflow stepper works
* inspection result displays
* field mapping result displays
* sandbox result displays
* production preparation displays
* safety review displays
* confirmation persists
* blocked result displays
* evidence displays
* Operations Center link works
* no Apply button appears while live submission is disabled
* safety flags cannot be changed from frontend

---

# 70. Frontend Build

Run:

```bash
npm run build
```

Expected:

```text
0 TypeScript errors
0 compilation errors
```

---

# 71. Backend Build

Run:

```bash
./gradlew test
```

Expected:

```text
BUILD SUCCESSFUL
```

All previous Phase 1–6 tests must continue passing.

Do not only run the new M6-F tests.

---

# 72. Browser Verification

Verify at:

```text
http://localhost:5173
```

Complete:

```text
Application
 → Execute
 → Inspect
 → Map
 → Sandbox
 → Prepare
 → Review
 → Confirm
 → Run
```

Expected final result:

```text
LIVE SUBMISSION BLOCKED
```

and:

```text
No external application was submitted.
```

---

# 73. Browser Verification Screens

Capture evidence for:

1. Unified execution workspace
2. Form inspection
3. Candidate mapping
4. Sandbox verification
5. Safety review
6. Candidate confirmation
7. Blocked execution
8. Execution evidence
9. Operations Center

---

# 74. Security Verification

Explicitly test attempts to:

```text
modify request mode
modify applicationId
modify confirmationId
modify userId
modify tenantId
modify safety configuration
skip confirmation
skip preparation
skip sandbox
reuse stale fingerprint
reuse old confirmation
execute concurrently
```

All must be rejected safely.

---

# 75. No Frontend Trust

The backend must assume every frontend request can be maliciously modified.

Therefore:

```text
frontend = presentation
backend = authority
```

All safety decisions must happen server-side.

---

# 76. No Automatic Live Enablement

M6-F must NOT introduce:

```text
ALLOW_LIVE_SUBMISSION=true
```

in:

```text
application.yml
application.properties
.env
frontend config
database seed
test configuration
```

unless it is an isolated test fixture that cannot affect runtime.

Production/default configuration must remain:

```text
false
```

---

# 77. Test Fixture Safety

Tests that simulate live execution must use mocks/fakes.

Never connect test execution to a real employer ATS.

Never submit test applications to real Greenhouse boards.

Use:

```text
localhost
mock provider
sandbox provider
test doubles
```

for live-path unit/integration testing.

---

# 78. Real ATS Interaction

M6-F may perform:

```text
real Greenhouse read-only navigation
real form inspection
real form fingerprinting
```

when explicitly invoked in a controlled development environment.

It must not perform real external submission while:

```text
ALLOW_LIVE_SUBMISSION=false
```

---

# 79. Application Status Semantics

Ensure the application domain distinguishes:

```text
PREPARED
READY_FOR_REVIEW
CONFIRMED_SUBMISSION
BLOCKED
FAILED
REQUIRES_REVIEW
APPLIED
```

Do not use `APPLIED` for:

```text
sandbox
read-only
blocked
preview
dry-run
```

---

# 80. Evidence Integrity

Execution evidence must be immutable after completion except through a controlled correction/audit process.

Do not allow frontend users to edit:

```text
execution result
submission attempted
safety result
timestamps
provider
form fingerprint
```

---

# 81. Audit Integrity

Audit records should be append-only.

Do not provide an API for arbitrary audit deletion or modification.

---

# 82. Operational Visibility

The final workflow must be observable from:

```text
Application Workspace
        ↕
Controlled Execution Workspace
        ↕
ATS Operations Center
```

A user must be able to move between these contexts without losing:

```text
applicationId
executionRunId
previewId
```

---

# 83. Final UX Result

For the current safe configuration, the user journey should feel like:

```text
Select Application
      ↓
Inspect Greenhouse
      ↓
Review Fields
      ↓
Run Sandbox
      ↓
Prepare Read-Only Execution
      ↓
Review 17 Safety Checks
      ↓
Confirm
      ↓
Run Controlled Validation
      ↓
LIVE SUBMISSION BLOCKED
      ↓
View Evidence
```

This is the expected Phase 6 completion experience.

---

# 84. Definition of Done

M6-F is complete only when:

### Backend

* [ ] End-to-end orchestration implemented
* [ ] Existing M6-A through M6-E services reused
* [ ] Workflow state machine implemented
* [ ] API sequence enforced
* [ ] Candidate confirmation enforced
* [ ] Final 17 safety checks enforced
* [ ] Form fingerprint enforced
* [ ] Distributed lock enforced
* [ ] Idempotency enforced
* [ ] Authorization enforced
* [ ] Tenant isolation enforced
* [ ] Unknown outcome protected
* [ ] Final verification implemented
* [ ] End-to-end audit implemented

### Frontend

* [ ] Unified execution workspace
* [ ] Workflow stepper
* [ ] Inspection view
* [ ] Mapping view
* [ ] Sandbox view
* [ ] Safety review
* [ ] Candidate confirmation
* [ ] Execution result
* [ ] Evidence view
* [ ] Operations Center integration

### Safety

* [ ] `ALLOW_LIVE_SUBMISSION=false`
* [ ] `AUTO_APPLY=false`
* [ ] `AUTO_SEND_EMAIL=false`
* [ ] `AUTO_LINKEDIN=false`
* [ ] No autonomous submission
* [ ] No automatic retry
* [ ] No CAPTCHA bypass
* [ ] No anti-bot bypass
* [ ] No fabricated answers
* [ ] No fake application success
* [ ] No secret leakage
* [ ] No real test submissions

### Verification

* [ ] Full backend tests pass
* [ ] Frontend build passes
* [ ] Browser verification passes
* [ ] Security regression passes
* [ ] Safety regression passes
* [ ] Evidence captured
* [ ] Final safety audit passes

---

# 85. Final Safety Audit

Before declaring M6-F complete, execute:

```text
ALLOW_LIVE_SUBMISSION=false
AUTO_APPLY=false
AUTO_SEND_EMAIL=false
AUTO_LINKEDIN=false
```

Verify counters:

```text
Real Submissions: 0
Emails Sent: 0
LinkedIn Messages: 0
Real Files Uploaded: 0
```

Verify that:

```text
POST /browser/live/execute
```

still produces:

```text
BLOCKED
submissionAttempted=false
```

---

# 86. Required Final Report

Antigravity must return:

```text
PHASE 6 M6-F IMPLEMENTATION REPORT

Status:
PASS / FAIL

Backend Tests:
<result>

Frontend Build:
<result>

Browser Verification:
<result>

End-to-End Workflow:
PASS / FAIL

17 Safety Checks:
PASS / FAIL

Candidate Confirmation:
PASS / FAIL

Form Fingerprint:
PASS / FAIL

Distributed Lock:
PASS / FAIL

Idempotency:
PASS / FAIL

Authorization:
PASS / FAIL

Unknown Outcome Protection:
PASS / FAIL

Audit:
PASS / FAIL

Operations Center Integration:
PASS / FAIL
```

Then provide:

```text
Files Created:
Files Modified:
Database Migration:
API Endpoints:
Frontend Components:
Tests Added:
Screenshots:
Commit Hash:
```

---

# 87. Mandatory Safety Counters

Report:

```text
ALLOW_LIVE_SUBMISSION = false

Real Submissions Executed = 0
Emails Sent = 0
LinkedIn Messages Sent = 0
Real Files Uploaded = 0
```

These values must be verified rather than assumed.

---

# 88. Git Commit

After all verification succeeds, create:

```text
feat(phase6): complete controlled end-to-end browser execution workflow
```

Keep the commit focused.

Do not mix unrelated refactoring.

---

# 89. Mandatory Stop Gate

M6-F is the final milestone of Phase 6.

After successful completion:

**STOP.**

Do not:

* start Phase 7
* enable live submission
* change `ALLOW_LIVE_SUBMISSION` to true
* add autonomous application logic
* add LinkedIn automation
* add email automation
* bypass candidate confirmation
* bypass the 17 safety checks
* add stealth/anti-detection behavior
* add another ATS provider

Any future live-submission enablement must be treated as a separate, explicitly approved change.

---

# 90. Final Instruction to Antigravity

Implement M6-F completely.

First inspect the existing M6-A, M6-B, M6-C, M6-D, and M6-E implementations.

Treat those implementations as the source of truth.

Do not duplicate working services.

Do not weaken existing safety gates.

Do not remove existing tests.

Do not change the default safety configuration.

Do not enable live submission.

Do not submit applications to real employers during testing.

Use mocks, localhost, and sandbox providers for live-path test simulation.

Connect the complete application workflow into one controlled user experience.

Ensure the backend—not the frontend—is responsible for all safety decisions.

Ensure candidate confirmation is persisted.

Ensure form fingerprints are enforced.

Ensure the 17 safety checks remain mandatory.

Ensure unknown outcomes cannot trigger automatic retries.

Ensure `APPLIED` can only represent a verified real external submission.

Ensure blocked, sandbox, read-only, failed, and unknown outcomes remain distinct.

Run the complete backend test suite.

Run the frontend build.

Perform complete browser verification.

Perform security regression testing.

Perform the final safety audit.

Verify:

```text
ALLOW_LIVE_SUBMISSION=false
AUTO_APPLY=false
AUTO_SEND_EMAIL=false
AUTO_LINKEDIN=false

Real Submissions = 0
Emails Sent = 0
LinkedIn Messages = 0
Real Files Uploaded = 0
```

Create the M6-F commit.

Then STOP.

Do not proceed to Phase 7 without explicit approval.
