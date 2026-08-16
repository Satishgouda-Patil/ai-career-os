# PHASE 3 — M6-C
# ONE REAL PROVIDER + CONTROLLED END-TO-END APPLICATION

## STATUS

M6-A and M6-B must be completed and approved.

Proceed with ONLY M6-C.

This is the first milestone allowed to perform a real external application submission.

---

# 1. OBJECTIVE

Connect ONE real job application provider to the existing execution engine.

The goal is to prove the complete pipeline:

Job
 ↓
Application
 ↓
Form Discovery
 ↓
Form Analysis
 ↓
Candidate Mapping
 ↓
Validation
 ↓
Dry Run
 ↓
User Authorization
 ↓
Browser Interaction
 ↓
Verification
 ↓
FINAL SUBMISSION
 ↓
Confirmation
 ↓
APPLICATION APPLIED

Only ONE provider is allowed in this milestone.

Do not build multi-provider support yet.

---

# 2. PROVIDER SELECTION

Before implementation:

Inspect current job source data and application URLs.

Select the simplest provider that:

- has accessible application forms
- does not require CAPTCHA for the test flow
- does not require login for the test flow
- permits normal browser interaction
- has a stable application workflow

Do NOT attempt to bypass:

- CAPTCHA
- bot protection
- authentication
- paywalls
- anti-bot systems

If the provider cannot be safely automated:

STOP and choose another provider.

---

# 3. PROVIDER ADAPTER

Implement:

ApplicationExecutionProvider

using the existing SPI.

Do NOT create a separate execution architecture.

The provider should expose capabilities such as:

FORM_APPLICATION
RESUME_UPLOAD
COVER_LETTER_UPLOAD
TEXT_FIELDS
SELECT_FIELDS
CHECKBOX_FIELDS
RADIO_FIELDS

Only advertise capabilities actually supported.

---

# 4. PROVIDER REGISTRATION

Register provider through:

ApplicationExecutionProviderRegistry

Do not hard-code provider selection inside ApplicationExecutionService.

The architecture must remain pluggable.

---

# 5. PROVIDER URL VALIDATION

Only allow the configured provider domain.

Example:

allowedDomains:

- provider-domain.example

Do not allow arbitrary URLs.

Reject:

- unknown domain
- redirect to unknown domain
- suspicious URL
- unsupported protocol

---

# 6. APPLICATION STATE

Use the existing ApplicationStateMachine.

Preferred flow:

DISCOVERED
 ↓
QUALIFIED
 ↓
PREPARING
 ↓
READY_FOR_REVIEW
 ↓
APPROVED
 ↓
APPLYING
 ↓
APPLIED

If any stage fails:

FAILED

or:

SUBMISSION_REQUIRES_REVIEW

depending on the failure.

Never mark APPLIED merely because the browser reached a page.

---

# 7. USER AUTHORIZATION

Real submission requires explicit authorization.

The system must verify:

- user owns application
- form validation succeeded
- dry run succeeded
- application approval exists
- execution authorization exists
- application state allows execution

No automatic submission based only on:

- job score
- AI confidence
- readiness
- successful dry run

---

# 8. DISTRIBUTED LOCK

Reuse:

DistributedExecutionLock

Lock:

application-execution:{applicationId}

Fail closed if lock unavailable.

Only one execution may proceed.

---

# 9. EXECUTION PIPELINE

Implement:

prepare
→ validate
→ acquire lock
→ create execution record
→ discover form
→ compare with approved form plan
→ interact
→ verify
→ locate submission control
→ perform final submission
→ verify confirmation
→ persist result
→ release lock

Every stage must be auditable.

---

# 10. FORM CHANGE DETECTION

This is CRITICAL.

The form used for real submission must match the approved form plan closely enough.

Before submission compare:

- fields
- required fields
- labels
- types
- options
- selectors where appropriate

If significant unexpected changes are detected:

STOP.

Return:

FORM_CHANGED

Require re-analysis and approval.

Do NOT submit.

---

# 11. FINAL SUBMISSION GATE

Create a final safety gate immediately before submission.

Conceptually:

FinalSubmissionGate

It must verify again:

1. application ownership
2. provider
3. domain
4. application state
5. approval
6. execution authorization
7. validation status
8. form consistency
9. unresolved fields = 0
10. sensitive review items = 0
11. AI-generated unapproved answers = 0
12. required files available
13. provider capability
14. distributed lock
15. dry run completed
16. no CAPTCHA
17. no login requirement
18. no unexpected form change

If ANY check fails:

DO NOT SUBMIT.

---

# 12. CAPTCHA

If CAPTCHA appears at any stage:

STOP.

Set:

SUBMISSION_REQUIRES_REVIEW

Do not solve.

Do not bypass.

Do not attempt repeated submissions.

---

# 13. LOGIN

If login is required:

STOP.

Return:

LOGIN_REQUIRED

Do not automate credentials.

---

# 14. FINAL SUBMIT ACTION

Only after FinalSubmissionGate succeeds:

perform exactly ONE submit action.

Do not:

- double click
- retry blindly
- submit multiple times

Use an explicit single-submit operation.

---

# 15. SUBMISSION VERIFICATION

After submission:

Verify evidence of success.

Possible signals:

- confirmation page
- confirmation message
- application/reference ID
- URL change
- provider-specific success indicator

Do NOT rely on absence of an error alone.

If confirmation cannot be verified:

state:

SUBMISSION_REQUIRES_REVIEW

Do not mark APPLIED.

---

# 16. IDEMPOTENCY

Prevent duplicate application submission.

Before execution:

check existing application executions.

If already successfully submitted:

do not submit again.

If an uncertain previous submission exists:

SUBMISSION_REQUIRES_REVIEW

Do not retry automatically.

---

# 17. EXECUTION RESULT

Persist:

- provider
- startedAt
- completedAt
- outcomeStatus
- externalApplicationId if available
- externalUrl if available
- errorCode
- retryable
- confirmation evidence metadata

Never store unnecessary page content.

---

# 18. ERROR HANDLING

Use structured error codes.

Examples:

PROVIDER_UNAVAILABLE
FORM_CHANGED
FORM_VALIDATION_FAILED
CAPTCHA_DETECTED
LOGIN_REQUIRED
SUBMISSION_BLOCKED
SUBMISSION_FAILED
SUBMISSION_UNKNOWN
CONFIRMATION_NOT_FOUND
DUPLICATE_APPLICATION
EXECUTION_NOT_AUTHORIZED
LOCK_NOT_ACQUIRED

Never classify unknown submission state as SUCCESS.

---

# 19. UNKNOWN SUBMISSION STATE

This is extremely important.

If the browser submits but the connection fails before confirmation:

DO NOT retry.

Set:

SUBMISSION_UNKNOWN

Then:

SUBMISSION_REQUIRES_REVIEW

The user must inspect the provider before another attempt.

This prevents duplicate applications.

---

# 20. PROVIDER-SPECIFIC ADAPTER

Keep provider-specific logic isolated.

Example:

execution/
 ├── provider/
 │    ├── ApplicationExecutionProvider.java
 │    ├── ProviderRegistry.java
 │    └── <ProviderName>ExecutionProvider.java

Do not put provider-specific selectors throughout generic services.

---

# 21. CONFIGURATION

Provider must be disabled by default.

Example:

app.execution.providers.<provider>.enabled=false

Require explicit enablement.

Use environment configuration.

Never hard-code credentials/secrets.

---

# 22. TEST MODE

Create a provider test mode where possible.

Support:

DRY_RUN
REAL

Default:

DRY_RUN

REAL mode must require explicit configuration.

---

# 23. REAL APPLICATION TEST

The first real application MUST be a deliberately selected test application.

Do NOT immediately run mass applications.

Test with:

1 application

Only.

After success, stop and inspect results.

---

# 24. TEST CHECKLIST

Before real submission:

- [ ] user authenticated
- [ ] application owned by user
- [ ] job URL correct
- [ ] provider recognized
- [ ] provider enabled
- [ ] form analyzed
- [ ] form unchanged
- [ ] validation passed
- [ ] dry run passed
- [ ] resume exists
- [ ] cover letter requirement satisfied
- [ ] sensitive fields reviewed
- [ ] AI answers reviewed
- [ ] application approved
- [ ] execution authorized
- [ ] Redis lock acquired
- [ ] no CAPTCHA
- [ ] no login requirement
- [ ] provider capabilities sufficient

Only then:

SUBMIT.

---

# 25. RATE LIMITING

Implement conservative execution limits.

At minimum:

- max concurrent executions per user = 1
- minimum delay between applications
- configurable daily application limit

Default values must be conservative.

Do NOT implement mass application mode.

---

# 26. RETRY POLICY

Never automatically retry an uncertain submission.

Retry only clearly classified transient failures that occurred BEFORE submission.

Examples:

safe retry:

provider unavailable before submission

unsafe retry:

browser disconnected after clicking submit

For unsafe/unknown states:

SUBMISSION_REQUIRES_REVIEW

---

# 27. NOTIFICATIONS

Use existing notification infrastructure.

Notify user when:

- execution starts
- execution blocked
- CAPTCHA detected
- login required
- submission succeeds
- submission becomes unknown
- submission requires review
- application confirmed

Do not include sensitive content unnecessarily.

---

# 28. API

Add an explicit execution endpoint.

Example:

POST /api/v1/applications/{id}/execute

Request should require explicit confirmation.

Example:

{
  "confirmation": "SUBMIT_APPLICATION"
}

The backend must still perform ALL FinalSubmissionGate checks.

Client confirmation alone is never sufficient.

---

# 29. EXECUTION RESPONSE

Return:

- applicationId
- executionId
- provider
- status
- outcome
- externalApplicationId
- externalUrl
- confirmationStatus
- reviewRequired
- errorCode

Never claim:

APPLIED

without verified evidence.

---

# 30. SECURITY

Verify:

- ownership
- authorization
- provider domain
- allowed URL
- execution state
- approval
- lock

Never log:

- passwords
- cookies
- JWTs
- authorization headers
- resume content
- cover letter content
- sensitive candidate answers

---

# 31. OBSERVABILITY

Add safe metrics:

- executions started
- executions succeeded
- executions failed
- submissions confirmed
- submissions unknown
- CAPTCHA encounters
- login-required encounters
- form-changed encounters
- provider failures
- average execution duration

Do not expose sensitive candidate information.

---

# 32. TESTING

Create:

### Unit tests

- provider capabilities
- URL validation
- final submission gate
- state transitions
- duplicate prevention
- confirmation detection
- error classification

### Integration tests

- complete execution pipeline
- authorization
- ownership
- Redis lock
- form change detection
- provider selection

### Browser tests

Use local test provider wherever possible.

Test:

- successful submission
- form changed
- CAPTCHA
- login
- missing required field
- invalid option
- provider unavailable
- confirmation missing
- duplicate execution
- unknown submission

---

# 33. REAL TEST APPLICATION

For the first real provider execution:

STOP AFTER ONE SUCCESSFUL REAL APPLICATION.

Do not implement:

- bulk application
- automatic job selection
- mass submission
- multi-provider execution
- autonomous recruiter outreach

Those belong to later phases.

---

# 34. REGRESSION

Run:

./gradlew test

./gradlew build

All previous tests must pass:

Phase 1
Phase 2
Phase 3 M1
M2
M3
M4
M5
M6-A
M6-B

No regressions.

---

# 35. DOCUMENTATION

Document:

- provider setup
- provider configuration
- browser requirements
- authorization flow
- final submission gate
- form change detection
- CAPTCHA behavior
- login behavior
- confirmation verification
- duplicate prevention
- unknown submission handling
- rate limits
- troubleshooting

---

# 36. STOP CONDITION

STOP AFTER M6-C.

This completes Phase 3 browser execution.

Do NOT start Phase 4.

Provide a complete walkthrough:

- provider selected
- why it was selected
- architecture
- provider adapter
- execution flow
- final submission gate
- state transitions
- authorization
- locking
- form change detection
- submission verification
- duplicate prevention
- rate limiting
- notifications
- APIs
- database changes
- tests
- build
- regression
- real test result
- git commit
- git push
- known limitations

WAIT FOR EXPLICIT APPROVAL BEFORE PHASE 4.