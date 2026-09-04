# Phase 6 M6-D PRD

## Controlled Real ATS Execution & Final Submission Verification

**Project:** AI Career OS
**Phase:** 6 — Browser Automation & Controlled Execution
**Milestone:** M6-D
**Status:** READY FOR IMPLEMENTATION
**Depends On:** M6-A, M6-B, M6-C
**Primary Objective:** Safely connect the verified browser execution pipeline to **one real ATS provider** while preserving explicit candidate approval, strict safety gates, auditability, and a mandatory dry-run default.

---

# 1. Objective

M6-D upgrades the browser automation system from:

```text
M6-A → Read-only discovery
M6-B → Sandbox form interaction
M6-C → Controlled sandbox execution
```

to:

```text
M6-D → Controlled real ATS execution
```

The system must be capable of executing an application against **one real ATS provider**, but:

> **REAL LIVE SUBMISSION MUST REMAIN DISABLED BY DEFAULT.**

M6-D must first validate the complete real-provider execution path in a non-submitting mode.

Only after all validation gates pass may the system expose a final candidate-controlled live submission action.

There must be:

* no automatic submission
* no unattended submission
* no automatic approval
* no automatic confirmation
* no automatic file upload
* no automatic email
* no automatic LinkedIn messaging
* no fallback from failed real execution to fake/sandbox success

---

# 2. Scope

M6-D covers:

1. Real ATS provider execution adapter.
2. Real browser navigation.
3. Real form discovery against the configured ATS.
4. Real form field mapping.
5. Safe candidate data population.
6. Read-back verification.
7. Required-field validation.
8. File-upload preparation.
9. Candidate review.
10. Explicit final confirmation.
11. Controlled live execution gate.
12. Submission result verification.
13. Failure handling.
14. Audit logging.
15. Execution locking.
16. Frontend review/confirmation UI.
17. Full automated regression testing.
18. Browser verification.

M6-D must support **one provider only**.

### Required provider

```text
Greenhouse
```

Use the existing:

```text
GreenhouseApplicationProviderImpl
```

and existing:

```text
ApplicationExecutionProvider
BrowserSession
BrowserDiscoveryService
BrowserInteractionService
SandboxExecutionService
ApplicationStateMachine
DistributedExecutionLock
IntegrationAuditService
```

where appropriate.

Do not introduce additional ATS providers in M6-D.

---

# 3. Mandatory Safety Principle

The system must maintain the following invariant:

```text
NO EXPLICIT FINAL USER CONFIRMATION
                ↓
        NO LIVE SUBMISSION
```

The following states must be respected:

```text
READY_FOR_REVIEW
        ↓
APPROVED
        ↓
CONFIRMED_SUBMISSION
        ↓
FINAL SAFETY VALIDATION
        ↓
APPLYING
        ↓
APPLIED
```

Any missing transition must prevent live execution.

---

# 4. Execution Modes

The system must explicitly support three modes.

## Mode 1 — SANDBOX

```text
SANDBOX
```

Behavior:

* local/mock execution
* no real ATS submission
* no real external side effects

This is the existing M6-C behavior.

---

## Mode 2 — PRODUCTION READ-ONLY

```text
PRODUCTION_READ_ONLY
```

Behavior:

* connects to the real ATS
* navigates to the real application page
* discovers forms
* reads labels and controls
* validates selectors
* detects submit controls
* does not submit
* does not upload files unless explicitly required for provider validation and separately gated
* does not create external application side effects

This mode is the default for M6-D development and testing.

---

## Mode 3 — PRODUCTION LIVE

```text
PRODUCTION_LIVE
```

Behavior:

* real browser
* real ATS
* real candidate application
* actual final submission

This mode must be disabled by default.

Configuration:

```properties
app.execution.allow-live-submission=false
```

The system must refuse live execution while this flag is false.

---

# 5. Live Execution Configuration

Required configuration:

```properties
app.execution.allow-live-submission=false
app.execution.mode=PRODUCTION_READ_ONLY
```

Environment variables should override deployment-sensitive values.

Recommended:

```text
ALLOW_LIVE_SUBMISSION=false
```

Never default this value to `true`.

Never silently enable live execution because credentials are present.

Never infer live mode from provider availability.

---

# 6. Real Provider

M6-D supports exactly one real ATS:

```text
Greenhouse
```

The implementation must use:

```text
GreenhouseApplicationProviderImpl
```

or refactor it safely if required.

Provider responsibilities:

1. Validate Greenhouse domain.
2. Create browser session.
3. Navigate to target application URL.
4. Validate redirects.
5. Discover form.
6. Map candidate fields.
7. Populate approved fields.
8. Verify populated values.
9. Detect unresolved fields.
10. Prepare submission.
11. Execute submission only after all gates pass.
12. Verify submission result.
13. Record audit information.
14. Return structured execution result.

---

# 7. Target Domain Safety

Only Greenhouse domains are allowed.

Allowed:

```text
greenhouse.io
boards.greenhouse.io
```

The implementation must reject:

```text
http://
```

if HTTPS is required by the provider/security policy.

The implementation must reject:

* unknown domains
* malicious redirects
* unexpected external domains
* authentication redirects to unapproved domains
* iframe/form actions pointing to unapproved domains

Example:

```text
Greenhouse → allowed
Greenhouse → unknown-domain.com → BLOCK
Greenhouse → malicious-domain.com → BLOCK
```

If an unsafe redirect occurs:

```text
ACTION_REQUIRED
```

and execution must stop.

---

# 8. Candidate Data Safety

Only candidate facts already present in the AI Career OS profile/resume data may be used.

Allowed examples:

```text
name
email
phone
location
LinkedIn URL
GitHub URL
portfolio URL
education
experience
skills
authorized resume
authorized cover letter
```

The system must never invent:

* work experience
* degrees
* certifications
* salary information
* work authorization
* demographic information
* legal information
* personal answers
* application answers

If the system cannot confidently map a required field:

```text
REQUIRES_REVIEW
```

must be returned.

The system must not guess.

---

# 9. Sensitive Question Handling

Certain application fields require explicit candidate input.

Examples:

```text
Are you legally authorized to work?
Will you require sponsorship?
Veteran status
Disability status
Gender
Race/ethnicity
Criminal history
Salary expectations
Relocation willingness
Security clearance
Other legal/compliance questions
```

These must never be automatically fabricated.

They should become:

```text
REQUIRES_REVIEW
```

The candidate must explicitly provide the answer before the application can proceed.

---

# 10. Form Mapping

M6-D must reuse the M6-B mapping engine where possible.

Supported controls:

```text
input[type=text]
input[type=email]
input[type=tel]
input[type=url]
textarea
select
radio
checkbox
file
```

Each mapped field must contain structured metadata:

```json
{
  "fieldId": "...",
  "label": "...",
  "selector": "...",
  "source": "CANDIDATE_PROFILE",
  "valuePresent": true,
  "verificationStatus": "VERIFIED"
}
```

---

# 11. Required Field Gate

Before live submission:

```text
requiredFields == mappedAndVerifiedFields
```

must be true.

If:

```text
required unresolved fields > 0
```

then:

```text
LIVE SUBMISSION = BLOCKED
```

The UI must show the unresolved fields to the candidate.

---

# 12. File Upload Safety

File uploads are permitted only for an explicitly approved candidate artifact.

Examples:

```text
Resume PDF
Cover Letter PDF
```

The system must:

1. Verify the file belongs to the current candidate.
2. Verify the file was generated/stored by AI Career OS.
3. Verify the file path is local/controlled.
4. Verify file type.
5. Verify file exists.
6. Verify file size is within configured limits.
7. Record the upload as an audited action.
8. Never upload arbitrary files.

No arbitrary filesystem paths may be accepted from the frontend.

---

# 13. Three-Step Approval

M6-D must enforce the existing approval lifecycle.

## Step 1

```text
READY_FOR_REVIEW
        ↓
APPROVED
```

Candidate reviews:

* job
* company
* resume
* cover letter
* application workspace
* discovered form
* mapped fields

Candidate clicks:

```text
Approve & Prepare
```

---

## Step 2

System performs form mapping and verification.

State remains:

```text
APPROVED
```

The system generates:

```text
SubmissionPreview
```

containing:

* target URL
* provider
* fields detected
* fields mapped
* fields requiring review
* files selected
* selectors
* validation status
* warnings

---

## Step 3

Candidate reviews the final mapping.

Candidate explicitly clicks:

```text
Confirm & Execute Submission
```

This transitions:

```text
APPROVED
        ↓
CONFIRMED_SUBMISSION
```

Only then can the final execution gate be evaluated.

---

# 14. Final Pre-Submission Safety Gate

Immediately before any live submit action, perform all checks again.

Minimum required checks:

### Check 1

Application exists.

### Check 2

Application belongs to authenticated user.

### Check 3

Target URL is Greenhouse.

### Check 4

Provider is:

```text
GREENHOUSE_PRODUCTION
```

### Check 5

Application state is:

```text
CONFIRMED_SUBMISSION
```

### Check 6

Candidate approval is persisted.

### Check 7

Form discovery is valid.

### Check 8

All required fields are mapped.

### Check 9

All mapped fields passed read-back verification.

### Check 10

No unresolved sensitive fields remain.

### Check 11

Required resume/cover letter artifacts are valid.

### Check 12

Distributed execution lock is acquired.

### Check 13

Live execution configuration is enabled.

### Check 14

Provider capability is available.

### Check 15

Audit record can be persisted.

### Check 16

Target page has not changed unexpectedly.

### Check 17

Current browser session is still bound to the expected application.

If ANY check fails:

```text
DO NOT SUBMIT
```

---

# 15. Double Confirmation Protection

The backend must not trust a frontend boolean such as:

```json
{
  "confirmed": true
}
```

The server must independently verify:

```text
authenticated user
+
application ownership
+
APPROVED state
+
CONFIRMED_SUBMISSION state
+
valid submission preview
+
fresh safety checks
+
live configuration
```

Frontend confirmation alone is never sufficient.

---

# 16. Freshness / Stale Preview Protection

A `SubmissionPreview` must have a creation timestamp/version.

Example:

```text
previewId
applicationId
createdAt
formFingerprint
provider
targetUrl
```

Before live execution:

```text
currentFormFingerprint == preview.formFingerprint
```

must be true.

If the target form changed:

```text
ACTION_REQUIRED
```

and the candidate must regenerate the preview.

This prevents submission against a changed form.

---

# 17. Browser Execution

Use Playwright.

Execution sequence:

```text
Acquire distributed lock
        ↓
Create browser session
        ↓
Navigate to Greenhouse URL
        ↓
Validate final URL
        ↓
Validate provider identity
        ↓
Discover form
        ↓
Compare form fingerprint
        ↓
Populate approved fields
        ↓
Upload approved artifacts if required
        ↓
Read-back verification
        ↓
Run final safety checks
        ↓
Locate submit control
        ↓
FINAL SERVER-SIDE CONFIRMATION
        ↓
Submit
        ↓
Verify result
        ↓
Persist audit
        ↓
Release lock
```

---

# 18. Submit Button Safety

Submit controls must never be clicked during:

```text
SANDBOX
PRODUCTION_READ_ONLY
```

The submit action may only occur in:

```text
PRODUCTION_LIVE
```

after every safety gate passes.

The implementation must not use generic selectors such as:

```text
button:first
input[type=submit]
```

without validating that the control belongs to the intended application form.

---

# 19. Submission Verification

After a real submission attempt, the system must verify evidence of submission.

Possible evidence:

```text
confirmation page
confirmation message
application ID
success status
known Greenhouse confirmation marker
```

The system must not mark an application as:

```text
APPLIED
```

simply because:

```text
button.click()
```

completed.

If submission evidence cannot be verified:

```text
ACTION_REQUIRED
```

rather than falsely marking the application as applied.

---

# 20. Application State Transitions

Allowed:

```text
READY_FOR_REVIEW
        ↓
APPROVED
        ↓
CONFIRMED_SUBMISSION
        ↓
APPLYING
        ↓
APPLIED
```

Failure:

```text
APPLYING
   ↓
FAILED
```

or:

```text
APPLYING
   ↓
ACTION_REQUIRED
```

depending on whether the system can safely determine the outcome.

Never transition:

```text
READY_FOR_REVIEW → APPLIED
```

Never transition:

```text
APPROVED → APPLIED
```

without the required confirmation state.

---

# 21. Critical Unknown Outcome

If the browser loses connection immediately after clicking submit and the result is unknown:

```text
DO NOT RETRY AUTOMATICALLY
```

Set:

```text
ACTION_REQUIRED
```

and record:

```text
SUBMISSION_OUTCOME_UNKNOWN
```

The candidate must manually investigate before retrying.

This prevents duplicate applications.

---

# 22. Failure Handling

Real provider failures must never silently fall back to sandbox/mock execution.

Examples:

```text
HTTP 429
HTTP 500
browser timeout
selector missing
unexpected redirect
authentication failure
form changed
submission confirmation missing
network failure
```

must produce:

```text
FAILED
```

or:

```text
ACTION_REQUIRED
```

with an audit entry.

Never:

```text
REAL FAILURE → MOCK SUCCESS
```

---

# 23. Rate Limiting

Greenhouse execution must have provider-specific rate limits.

Default:

```text
5 execution attempts / minute
```

Rate limit should apply to real provider operations.

When rate limited:

```text
RATE_LIMITED
```

and execution must stop safely.

No automatic live retry that could cause duplicate submissions.

---

# 24. Distributed Lock

Continue using:

```text
DistributedExecutionLock
```

Suggested lock:

```text
application-browser-live-execution:{applicationId}
```

Only one execution may run for an application at a time.

If lock acquisition fails:

```text
ACTION_REQUIRED
```

No browser execution should start.

---

# 25. Audit Logging

Every real-provider execution must create sanitized audit records.

Record:

```text
userId
applicationId
provider
actionType
status
target domain
execution mode
duration
result
error code
timestamp
```

Never record:

```text
password
API token
OAuth token
session cookie
authorization header
candidate secret
full sensitive application answer
```

Audit entries must be sanitized.

---

# 26. Events

Introduce/use:

```text
ExternalProviderInvokedEvent
CandidateSubmissionConfirmedEvent
IntegrationHealthStatusEvent
BrowserExecutionStartedEvent
BrowserExecutionCompletedEvent
BrowserExecutionFailedEvent
```

Events must never contain plaintext credentials.

---

# 27. API Endpoints

Implement or extend:

## Prepare

```http
POST /api/v1/applications/{id}/browser/live/prepare
```

Returns:

```json
{
  "applicationId": 123,
  "mode": "PRODUCTION_READ_ONLY",
  "provider": "GREENHOUSE_PRODUCTION",
  "status": "READY_FOR_REVIEW",
  "targetUrl": "...",
  "fieldsDetected": 18,
  "fieldsMapped": 16,
  "fieldsRequireReview": 2,
  "submissionAttempted": false
}
```

---

## Confirm

```http
POST /api/v1/applications/{id}/browser/live/confirm
```

This endpoint:

* verifies ownership
* verifies `APPROVED`
* verifies preview
* verifies form fingerprint
* verifies all safety requirements
* transitions to `CONFIRMED_SUBMISSION`

It must NOT blindly submit based only on the request body.

---

## Execute

```http
POST /api/v1/applications/{id}/browser/live/execute
```

Server checks:

```text
CONFIRMED_SUBMISSION
+
ALLOW_LIVE_SUBMISSION=true
+
all final safety checks
```

Then executes the real provider.

---

## Status

```http
GET /api/v1/applications/{id}/browser/live/status
```

Returns:

```text
mode
provider
state
executionStatus
lastAuditId
submissionAttempted
submissionVerified
```

---

# 28. Frontend UI

Add a controlled execution workspace.

Suggested page/component:

```text
ControlledSubmissionPage.tsx
```

or:

```text
LiveSubmissionPanel.tsx
```

The UI must show:

### Provider

```text
Greenhouse
```

### Mode

```text
PRODUCTION / READ-ONLY
```

or:

```text
PRODUCTION / LIVE EXECUTION
```

### Safety

```text
AUTO_APPLY: OFF
AUTO_SEND_EMAIL: OFF
AUTO_LINKEDIN: OFF
```

### Form status

```text
18 fields detected
16 mapped
2 require review
```

### Candidate artifacts

```text
Resume: READY
Cover Letter: READY
```

### Confirmation

Display:

```text
I have reviewed the application fields and authorize submission of this application.
```

with an explicit checkbox.

The checkbox is only a UI confirmation.

The backend must independently enforce all state transitions.

---

# 29. Live Submission Button

The button must be disabled unless:

```text
application = CONFIRMED_SUBMISSION
AND
ALLOW_LIVE_SUBMISSION = true
AND
all safety checks = PASS
```

Display warning:

```text
This action will submit your application to the external employer ATS.
```

The action must never be presented as automatic.

---

# 30. Default UI Behavior

Even if the backend supports live execution:

```text
ALLOW_LIVE_SUBMISSION = false
```

must result in:

```text
LIVE SUBMISSION DISABLED
```

The UI should show:

```text
Production execution is currently disabled.
Enable live submission explicitly in server configuration before execution.
```

Do not provide a frontend switch capable of bypassing the backend configuration.

---

# 31. Database

If existing schemas are sufficient, reuse them.

If additional persistence is required, create:

```text
V21__create_live_browser_execution_schema.sql
```

Possible table:

```sql
CREATE TABLE IF NOT EXISTS browser_execution_runs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    application_id BIGINT NOT NULL,
    provider_name VARCHAR(50) NOT NULL,
    execution_mode VARCHAR(30) NOT NULL,
    status VARCHAR(40) NOT NULL,
    preview_id VARCHAR(100),
    form_fingerprint VARCHAR(128),
    submission_attempted BOOLEAN NOT NULL DEFAULT FALSE,
    submission_verified BOOLEAN NOT NULL DEFAULT FALSE,
    error_code VARCHAR(80),
    started_at DATETIME,
    completed_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_browser_execution_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_browser_execution_application
        FOREIGN KEY (application_id) REFERENCES applications(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_browser_execution_application
    ON browser_execution_runs(application_id);

CREATE INDEX idx_browser_execution_user
    ON browser_execution_runs(user_id);
```

Do not create duplicate schema if an equivalent existing table already exists.

---

# 32. Security

Credentials must continue using:

```text
APP_CREDENTIAL_ENCRYPTION_KEY
```

with:

```text
AES-256-GCM
```

Never:

* expose credentials to frontend
* log credentials
* return credentials from APIs
* store plaintext provider secrets
* accept credentials directly from arbitrary frontend fields

---

# 33. Authentication & Authorization

Every browser execution endpoint must:

1. Authenticate user.
2. Load application.
3. Verify application belongs to user.
4. Verify required candidate profile access.
5. Verify provider credential ownership.
6. Enforce all safety states.

No cross-user application execution is allowed.

---

# 34. Idempotency

Live execution must use an idempotency mechanism.

Example:

```text
applicationId + confirmedSubmissionId
```

A previously completed or unknown submission must not automatically execute again.

If an application already has:

```text
APPLIED
```

then:

```text
LIVE EXECUTION = BLOCKED
```

If outcome is:

```text
ACTION_REQUIRED
```

then:

```text
LIVE EXECUTION = BLOCKED
```

until explicitly resolved.

---

# 35. Testing Requirements

## Unit Tests

Test:

* provider detection
* domain validation
* redirect validation
* candidate ownership
* field mapping
* sensitive field blocking
* required field blocking
* file validation
* preview generation
* fingerprint mismatch
* approval transitions
* confirmation transitions
* live configuration gate
* lock acquisition
* rate limiting
* audit sanitization
* idempotency
* failure states

---

# 36. Integration Tests

Test complete lifecycle:

```text
READY_FOR_REVIEW
        ↓
APPROVED
        ↓
Form discovery
        ↓
Field mapping
        ↓
Preview
        ↓
CONFIRMED_SUBMISSION
        ↓
Safety checks
        ↓
Execution
        ↓
Verification
        ↓
APPLIED
```

Also test:

```text
Execution failure → FAILED
Unknown outcome → ACTION_REQUIRED
Form changed → ACTION_REQUIRED
Missing required field → BLOCKED
Live flag false → BLOCKED
Wrong domain → BLOCKED
Wrong user → BLOCKED
Duplicate execution → BLOCKED
```

---

# 37. Critical Safety Tests

These tests are mandatory.

### Test 1

```text
ALLOW_LIVE_SUBMISSION=false
```

Result:

```text
NO SUBMISSION
```

### Test 2

Candidate has not confirmed.

Result:

```text
NO SUBMISSION
```

### Test 3

Required field unresolved.

Result:

```text
NO SUBMISSION
```

### Test 4

Wrong domain.

Result:

```text
NO SUBMISSION
```

### Test 5

Form fingerprint changed.

Result:

```text
NO SUBMISSION
```

### Test 6

Distributed lock unavailable.

Result:

```text
NO SUBMISSION
```

### Test 7

Application already `APPLIED`.

Result:

```text
NO SUBMISSION
```

### Test 8

Unknown outcome after submission attempt.

Result:

```text
ACTION_REQUIRED
```

and:

```text
NO AUTOMATIC RETRY
```

### Test 9

Provider failure.

Result:

```text
FAILED/ACTION_REQUIRED
```

and:

```text
NO MOCK FALLBACK
```

---

# 38. Existing Regression Suite

Run:

```bash
./gradlew test
```

All previous tests must remain green.

M6-D must not break:

```text
Phase 1
Phase 2
Phase 3
Phase 4
Phase 5
M6-A
M6-B
M6-C
```

---

# 39. Frontend Build

Run:

```bash
npm run build
```

Requirement:

```text
0 TypeScript errors
0 compilation errors
```

---

# 40. Browser Verification

Use browser verification against the local application.

Verify:

1. Application workspace opens.
2. Greenhouse provider is displayed.
3. Read-only mode is displayed.
4. Form discovery works.
5. Fields are mapped.
6. Unresolved fields are clearly shown.
7. Resume/cover letter status is displayed.
8. Candidate approval is required.
9. Final confirmation is required.
10. Live submission remains disabled by default.
11. No submission occurs while live flag is false.
12. Audit record is visible.
13. Failed execution produces safe failure state.

### IMPORTANT

Browser verification must NOT enable live submission merely to make the test pass.

The default browser verification must prove:

```text
LIVE SUBMISSION = BLOCKED
```

when:

```text
ALLOW_LIVE_SUBMISSION=false
```

---

# 41. Optional Live-Provider Smoke Test

A live external submission must NOT be performed automatically as part of CI or normal browser verification.

If a real external smoke test is ever performed, it requires:

1. Explicit human instruction.
2. Explicit production configuration.
3. Candidate-controlled confirmation.
4. A dedicated test application/job where appropriate.
5. No automatic retry.
6. Full audit logging.

M6-D is considered complete without making a real external submission.

---

# 42. Definition of Done

M6-D is complete only when all of the following are true:

```text
[ ] Greenhouse real-provider adapter implemented
[ ] Real browser navigation implemented
[ ] Production read-only execution implemented
[ ] Candidate field mapping implemented
[ ] Required-field verification implemented
[ ] Sensitive-field protection implemented
[ ] Controlled file upload implemented
[ ] Submission preview implemented
[ ] Form fingerprint protection implemented
[ ] Three-step approval enforced
[ ] Final server-side safety gate implemented
[ ] Distributed lock enforced
[ ] Rate limiting enforced
[ ] Idempotency protection implemented
[ ] Real submission failure handling implemented
[ ] Unknown outcome handling implemented
[ ] Audit logging implemented
[ ] API endpoints implemented
[ ] Frontend controlled execution UI implemented
[ ] Live execution OFF by default
[ ] No mock fallback after real-provider failure
[ ] Backend tests pass
[ ] Frontend build passes
[ ] Browser verification passes
[ ] No credentials exposed
[ ] No automatic email sending
[ ] No automatic LinkedIn messaging
[ ] No unattended submission
```

---

# 43. Mandatory Final Safety State

At the end of M6-D implementation, the default production configuration MUST remain:

```text
AUTO_APPLY              = false
AUTO_SEND_EMAIL         = false
AUTO_LINKEDIN           = false
ALLOW_LIVE_SUBMISSION   = false
```

Therefore:

```text
REAL ATS SUBMISSION = DISABLED BY DEFAULT
```

The implementation may contain the complete live execution pathway, but the system must remain safe until explicitly enabled.

---

# 44. Mandatory Stop Gate

Antigravity MUST STOP after M6-D implementation and verification.

Do NOT automatically proceed to:

```text
M6-E
Phase 7
additional ATS providers
automatic applications
automatic email sending
automatic LinkedIn messaging
```

After completion, report:

```text
Phase 6 M6-D COMPLETE

Backend Tests: PASS (X/X)
Frontend Build: PASS
Browser Verification: PASS

Greenhouse Provider: IMPLEMENTED
Production Read-Only: VERIFIED
Live Execution Path: IMPLEMENTED
Live Submission Default: OFF

Real External Submission During Verification: NO
Real Email Sent: NO
Real LinkedIn Message Sent: NO
Real File Upload During Verification: NO

AUTO_APPLY: OFF
AUTO_SEND_EMAIL: OFF
AUTO_LINKEDIN: OFF
ALLOW_LIVE_SUBMISSION: OFF

Commit: <hash>
Branch: main

STOP GATE ACTIVE
```

Antigravity must then wait for explicit user approval.

---

# 45. Implementation Order

Implement strictly in this order:

### Step 1 — Execution Model

Create/verify:

```text
BrowserExecutionMode
BrowserExecutionStatus
BrowserExecutionResult
```

---

### Step 2 — Live Execution Service

Implement:

```text
LiveBrowserExecutionService
LiveBrowserExecutionServiceImpl
```

Reuse M6-A/M6-B/M6-C components wherever possible.

---

### Step 3 — Greenhouse Provider

Complete:

```text
GreenhouseApplicationProviderImpl
```

with:

* real navigation
* real discovery
* real mapping
* verification
* controlled execution

---

### Step 4 — Safety Gate

Implement:

```text
LiveSubmissionSafetyGate
```

Centralize all final checks.

No controller should bypass this service.

---

### Step 5 — Preview & Fingerprint

Implement:

```text
SubmissionPreview
FormFingerprint
```

with stale-preview detection.

---

### Step 6 — Approval

Implement:

```text
APPROVED
        ↓
CONFIRMED_SUBMISSION
```

with persisted server-side verification.

---

### Step 7 — Execution Persistence

Create migration only if required:

```text
V21__create_live_browser_execution_schema.sql
```

---

### Step 8 — API

Implement:

```text
POST /api/v1/applications/{id}/browser/live/prepare
POST /api/v1/applications/{id}/browser/live/confirm
POST /api/v1/applications/{id}/browser/live/execute
GET  /api/v1/applications/{id}/browser/live/status
```

---

### Step 9 — Frontend

Implement:

```text
ControlledSubmissionPage.tsx
```

or equivalent.

Make the safety state extremely clear.

---

### Step 10 — Tests

Run:

```bash
./gradlew test
npm run build
```

Fix all failures.

---

### Step 11 — Browser Verification

Verify the complete workflow in:

```text
PRODUCTION_READ_ONLY
```

with:

```text
ALLOW_LIVE_SUBMISSION=false
```

Prove that the final submission is blocked.

---

### Step 12 — Commit

Use:

```bash
git status
git add .
git commit -m "feat(phase6-m6-d): implement controlled real greenhouse execution"
git push origin main
```

---

# 46. Final Instruction to Antigravity

Implement **ONLY Phase 6 M6-D** according to this PRD.

Do not skip safety gates.

Do not simplify the approval flow.

Do not enable live submission by default.

Do not use mock data to hide real-provider failures.

Do not send real emails.

Do not send LinkedIn messages.

Do not automatically submit applications.

Do not perform a real external submission during normal tests or browser verification.

Use existing M6-A, M6-B, M6-C, and Phase 5 architecture wherever possible instead of duplicating functionality.

Run the complete regression suite.

Run the frontend build.

Run browser verification.

Commit and push only after all checks pass.

Then **STOP** and wait for explicit user approval.
