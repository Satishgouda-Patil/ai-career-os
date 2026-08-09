# AI Career OS — Phase 3

# Execution.md

## Application Execution Engine Specification

**Version:** 3.0
**Status:** Implementation Specification
**Depends On:** README.md, PRD.md, Architecture.md, Database.md, Events.md, Automation.md

---

# 1. Purpose

The Execution Engine is responsible for converting a fully prepared and approved application into an actual application submission on a supported external job platform.

The engine sits between:

```text
Application Preparation
        ↓
Approval / Automation Policy
        ↓
APPLICATION EXECUTION ENGINE
        ↓
External Job Portal
        ↓
Submission Verification
```

The execution engine must be:

* safe
* deterministic
* auditable
* idempotent
* provider-independent
* recoverable
* observable
* human-interruptible

---

# 2. Core Principle

The execution engine must NEVER blindly submit an application.

Every execution must follow:

```text
VALIDATE
   ↓
LOCK
   ↓
SELECT PROVIDER
   ↓
CREATE EXECUTION
   ↓
OPEN SESSION
   ↓
DISCOVER FORM
   ↓
MAP FIELDS
   ↓
VALIDATE FORM
   ↓
HANDLE SAFE QUESTIONS
   ↓
UPLOAD DOCUMENTS
   ↓
PRE-SUBMISSION CHECK
   ↓
SUBMIT
   ↓
VERIFY
   ↓
PERSIST RESULT
```

---

# 3. Execution Boundary

The execution engine starts only when:

```text
ApplicationStatus = APPROVED
```

or when:

```text
AutomationDecision = AUTO_APPLY
```

and all policy conditions are satisfied.

It must never directly decide whether a job is suitable.

That responsibility belongs to:

```text
Eligibility Engine
Automation Decision Engine
Approval Policy
```

---

# 4. Execution Lifecycle

```text
READY
  ↓
VALIDATING
  ↓
LOCKED
  ↓
PROVIDER_SELECTED
  ↓
SESSION_STARTED
  ↓
FORM_DISCOVERED
  ↓
FORM_MAPPED
  ↓
FORM_VALIDATED
  ↓
READY_TO_SUBMIT
  ↓
SUBMITTING
  ↓
VERIFYING
  ↓
SUBMITTED
```

Failure states:

```text
FAILED
ACTION_REQUIRED
BLOCKED
CANCELLED
UNVERIFIED
```

---

# 5. Execution State Machine

Allowed transitions:

```text
READY → VALIDATING

VALIDATING → LOCKED
VALIDATING → FAILED
VALIDATING → ACTION_REQUIRED

LOCKED → PROVIDER_SELECTED
LOCKED → FAILED

PROVIDER_SELECTED → SESSION_STARTED
PROVIDER_SELECTED → ACTION_REQUIRED

SESSION_STARTED → FORM_DISCOVERED
SESSION_STARTED → ACTION_REQUIRED
SESSION_STARTED → FAILED

FORM_DISCOVERED → FORM_MAPPED
FORM_DISCOVERED → ACTION_REQUIRED

FORM_MAPPED → FORM_VALIDATED
FORM_MAPPED → ACTION_REQUIRED

FORM_VALIDATED → READY_TO_SUBMIT
FORM_VALIDATED → ACTION_REQUIRED
FORM_VALIDATED → FAILED

READY_TO_SUBMIT → SUBMITTING

SUBMITTING → VERIFYING
SUBMITTING → FAILED
SUBMITTING → UNVERIFIED

VERIFYING → SUBMITTED
VERIFYING → UNVERIFIED
VERIFYING → ACTION_REQUIRED
```

No arbitrary state transitions are allowed.

---

# 6. Execution Request

The execution engine should receive a structured request.

Example:

```json
{
  "applicationId": "APP-123",
  "userId": "USER-123",
  "jobId": "JOB-123",
  "automationLevel": 3,
  "dryRun": false
}
```

Do not trust client-provided values blindly.

The backend must load authoritative data from the database.

---

# 7. Execution Context

Create an internal execution context containing:

```text
executionId
applicationId
userId
jobId
provider
jobUrl
resume
coverLetter
applicationAnswers
automationLevel
dryRun
correlationId
```

Do not expose sensitive session information through this context.

---

# 8. Execution Provider SPI

The execution engine must use a provider abstraction.

Suggested interface:

```java
public interface ApplicationExecutionProvider {

    boolean supports(Job job);

    ProviderType getProviderType();

    ExecutionCapabilities getCapabilities();

    ExecutionSession startSession(ExecutionContext context);

    FormDefinition discoverForm(
        ExecutionSession session,
        ExecutionContext context
    );

    FormResult fillForm(
        ExecutionSession session,
        FormDefinition form,
        ExecutionContext context
    );

    SubmissionResult submit(
        ExecutionSession session,
        ExecutionContext context
    );

    VerificationResult verify(
        ExecutionSession session,
        ExecutionContext context
    );

    void closeSession(ExecutionSession session);
}
```

The exact package and implementation should follow the existing architecture.

---

# 9. Provider Implementations

The architecture must allow:

```text
ApplicationExecutionProvider
        │
        ├── GreenhouseExecutionProvider
        ├── LeverExecutionProvider
        ├── WorkdayExecutionProvider
        └── GenericExecutionProvider
```

Do not implement every provider immediately.

Build the SPI first.

Then implement providers incrementally.

---

# 10. Provider Detection

Provider detection should inspect:

```text
job URL
domain
URL path
known platform signatures
HTML metadata
```

Example:

```text
greenhouse.io → GREENHOUSE
lever.co → LEVER
workdayjobs.com → WORKDAY
```

Unknown:

```text
UNKNOWN
```

---

# 11. Provider Capability Model

Each provider exposes capabilities.

Example:

```json
{
  "supportsResumeUpload": true,
  "supportsCoverLetterUpload": true,
  "supportsApplicationAnswers": true,
  "supportsStatusVerification": false,
  "supportsExternalId": true,
  "supportsSessionReuse": true
}
```

---

# 12. Unknown Provider

If provider is unknown:

```text
do not automatically submit
```

Create:

```text
ACTION_REQUIRED
```

with:

```text
reason = UNSUPPORTED_PROVIDER
```

The system may still allow the user to manually continue.

---

# 13. Execution Risk Assessment

Before provider execution, calculate:

```text
ExecutionRisk
```

Possible values:

```text
LOW
MEDIUM
HIGH
BLOCKED
```

---

# 14. LOW Risk

Requirements:

```text
known provider
supported operation
known form structure
all required fields mapped
high confidence
no sensitive questions
no CAPTCHA
no MFA
no unresolved validation
```

---

# 15. MEDIUM Risk

Examples:

```text
new form structure
unknown optional field
provider partially supported
moderate confidence
```

Require approval unless the user explicitly allows it.

---

# 16. HIGH Risk

Examples:

```text
sensitive question
legal declaration
unknown required field
conflicting user data
provider authentication issue
unexpected page
```

Stop execution.

Create human action.

---

# 17. BLOCKED

Examples:

```text
CAPTCHA
MFA challenge
bot detection
unsupported authentication
security challenge
provider explicitly blocks automation
```

Execution must immediately stop.

---

# 18. CAPTCHA Handling

If CAPTCHA is detected:

```text
EXECUTION
   ↓
CAPTCHA DETECTED
   ↓
STOP
   ↓
SAVE STATE
   ↓
ACTION_REQUIRED
   ↓
NOTIFY USER
```

Never:

* solve CAPTCHA automatically
* bypass CAPTCHA
* rotate identity to bypass CAPTCHA
* circumvent anti-bot controls

---

# 19. MFA Handling

If MFA is required:

```text
STOP
 ↓
ACTION_REQUIRED
 ↓
NOTIFY USER
```

The system may allow the user to complete MFA manually and then resume the workflow if supported.

---

# 20. Authentication

Supported authentication model:

```text
existing authenticated browser session
```

If authentication is missing:

```text
ACTION_REQUIRED
```

Do not store raw credentials in the application database.

---

# 21. Browser Session

If browser automation is used, sessions must be isolated.

Conceptually:

```text
User
 ↓
Browser Session
 ↓
Provider
```

Each execution should have a unique session identifier.

---

# 22. Session Security

Never log:

```text
password
session cookies
access tokens
refresh tokens
authentication headers
MFA codes
```

Session secrets must be encrypted or managed through the secure runtime environment.

---

# 23. Session Lifecycle

```text
CREATE
 ↓
AUTHENTICATE / REUSE
 ↓
EXECUTE
 ↓
CAPTURE REQUIRED METADATA
 ↓
CLOSE
```

Always close sessions after execution.

---

# 24. Session Timeout

Sessions must have configurable timeout.

Example:

```text
executionSessionTimeout = 10 minutes
```

If exceeded:

```text
STOP
 ↓
ACTION_REQUIRED
```

Do not leave browser sessions running indefinitely.

---

# 25. Form Discovery

After opening the application page:

```text
Discover:
- forms
- inputs
- labels
- textareas
- selects
- radio buttons
- checkboxes
- file inputs
- required markers
```

Create a normalized:

```text
FormDefinition
```

---

# 26. Form Definition

Example:

```json
{
  "fields": [
    {
      "fieldId": "first_name",
      "label": "First Name",
      "type": "TEXT",
      "required": true
    },
    {
      "fieldId": "resume",
      "label": "Resume",
      "type": "FILE",
      "required": true
    }
  ]
}
```

---

# 27. Field Types

Support:

```text
TEXT
TEXTAREA
EMAIL
PHONE
NUMBER
DATE
SELECT
MULTI_SELECT
RADIO
CHECKBOX
FILE
URL
UNKNOWN
```

---

# 28. Field Mapping

Map provider fields to canonical user/application data.

Example:

```text
"First Name"
        ↓
profile.firstName

"Email"
        ↓
user.email

"Phone"
        ↓
profile.phone

"Resume"
        ↓
application.resume
```

---

# 29. Field Mapping Confidence

Each mapping must have confidence.

Example:

```json
{
  "field": "years_experience",
  "mappedTo": "profile.totalExperience",
  "confidence": 0.97
}
```

---

# 30. Mapping Thresholds

Default:

```text
>= 0.95 → automatic
0.80–0.94 → review
< 0.80 → action required
```

These values must be configurable.

---

# 31. Deterministic First

Field mapping should prioritize deterministic methods:

```text
1. provider-specific mapping
2. known field name
3. HTML name/id
4. label matching
5. semantic mapping
6. AI-assisted mapping
```

AI should not be the first mechanism when deterministic mapping is possible.

---

# 32. AI-Assisted Field Mapping

AI may help map ambiguous fields.

Input:

```text
field label
field type
nearby text
job context
profile schema
```

Output:

```text
canonical field
confidence
reason
```

If confidence is low:

```text
ACTION_REQUIRED
```

---

# 33. Required Field Validation

Before filling:

```text
identify required fields
```

Then verify every required field has a valid source.

If not:

```text
STOP
```

Do not submit incomplete forms.

---

# 34. User Data Authority

The canonical profile is authoritative for:

```text
name
email
phone
location
experience
education
skills
work authorization
links
```

AI-generated content cannot overwrite canonical profile facts.

---

# 35. Resume Upload

The execution engine must use the approved resume artifact associated with the application.

Before upload:

```text
file exists
file is readable
file type supported
file belongs to current application
```

---

# 36. Resume Format

Preferred:

```text
PDF
```

Optional:

```text
DOCX
```

Provider-specific requirements must be respected.

---

# 37. Cover Letter Upload

If the application requires a cover letter:

```text
retrieve approved cover letter
validate file
upload
```

If optional:

follow automation policy.

---

# 38. Application Answers

Answers must come from:

```text
approved application answer set
```

Never regenerate answers during submission without going through validation.

---

# 39. Answer Provenance

Each answer should have:

```text
answerId
question
answer
source
confidence
approved
```

Possible source:

```text
PROFILE
RESUME
AI
USER
JOB_DESCRIPTION
```

---

# 40. Sensitive Questions

Sensitive questions must default to:

```text
ACTION_REQUIRED
```

Examples include:

```text
legal declarations
work authorization
demographic information
criminal/legal declarations
medical information
other highly sensitive information
```

The user may explicitly configure handling for some categories.

---

# 41. Unknown Questions

If the system cannot confidently classify a required question:

```text
STOP
```

Create human action:

```text
UNKNOWN_APPLICATION_QUESTION
```

---

# 42. Optional Unknown Fields

If an unknown field is optional:

```text
do not guess
leave empty
continue
```

unless provider requires otherwise.

---

# 43. Checkbox Handling

Checkboxes must not be blindly selected.

Classify:

```text
REQUIRED_AGREEMENT
OPTIONAL
MARKETING
LEGAL
UNKNOWN
```

Legal or unknown checkboxes require review.

---

# 44. Marketing Consent

Do not automatically opt the user into:

```text
marketing emails
SMS marketing
third-party promotions
newsletter subscriptions
```

unless the user explicitly configured this.

Default:

```text
unchecked
```

---

# 45. Legal Agreement

If a required legal agreement is detected:

```text
ACTION_REQUIRED
```

unless the user has explicitly authorized that exact category of agreement.

---

# 46. Location Fields

Normalize:

```text
country
state
city
postal code
address
```

Use canonical profile values.

Do not invent location information.

---

# 47. Salary Fields

If salary is requested:

Use configured user preference.

If no preference exists:

```text
ACTION_REQUIRED
```

Do not invent a salary expectation.

---

# 48. Experience Fields

Use canonical profile data.

If job asks:

```text
How many years of Java experience?
```

Only answer from verified profile/resume information.

---

# 49. Work Authorization

Never infer this from nationality, location, or guesswork.

Use explicit profile data.

If missing:

```text
ACTION_REQUIRED
```

---

# 50. File Validation

Before upload:

```text
extension
MIME type
size
readability
file existence
storage availability
```

Reject invalid files.

---

# 51. Form Filling Strategy

Fill fields in deterministic order:

```text
Personal Information
 ↓
Contact Information
 ↓
Professional Information
 ↓
Application Questions
 ↓
Documents
 ↓
Preferences
 ↓
Optional Fields
```

---

# 52. Form Filling Safety

After filling each section:

```text
validate current field
```

If provider rejects a value:

```text
capture error
attempt safe correction if deterministic
otherwise ACTION_REQUIRED
```

---

# 53. Client-Side Validation

Handle:

```text
required
email format
phone format
number range
date format
file type
character limits
```

before submission.

---

# 54. Provider Validation Errors

Example:

```text
"Please enter a valid phone number"
```

The engine may correct formatting.

Example:

```text
+91 9876543210
```

→ provider-compatible format.

Do not change the underlying user data.

---

# 55. Pre-Submission Validation

Before submit:

```text
ALL REQUIRED FIELDS COMPLETE
AND
ALL FILES ATTACHED
AND
ALL REQUIRED QUESTIONS ANSWERED
AND
NO UNKNOWN REQUIRED FIELD
AND
NO BLOCKED STATE
AND
AUTOMATION POLICY ALLOWS SUBMISSION
```

Only then:

```text
READY_TO_SUBMIT
```

---

# 56. Final Application Snapshot

Before submission, create an execution snapshot.

It should record:

```text
applicationId
provider
field mappings
answers
uploaded artifacts
automation policy
risk score
timestamp
```

This provides auditability.

Do not store sensitive information unnecessarily.

---

# 57. Dry Run Mode

When:

```text
dryRun = true
```

the engine must:

```text
open provider
discover form
map fields
validate
prepare submission
```

but:

```text
DO NOT SUBMIT
```

Instead create:

```text
WOULD_SUBMIT
```

---

# 58. Dry Run Output

Example:

```json
{
  "status": "WOULD_SUBMIT",
  "provider": "GREENHOUSE",
  "fieldsMapped": 18,
  "fieldsMissing": 0,
  "risk": "LOW",
  "validation": "PASSED"
}
```

---

# 59. Submission Lock

Before execution:

```text
Acquire distributed lock
```

Recommended Redis key:

```text
application:execution:{applicationId}
```

If lock cannot be acquired:

```text
DO NOT EXECUTE
```

---

# 60. Idempotency

Every execution must have:

```text
idempotencyKey
```

Example:

```text
applicationId + executionAttempt
```

Before submitting:

```text
check previous execution
check provider application ID
check confirmation
check application status
```

---

# 61. Duplicate Prevention

If the application has already been verified as submitted:

```text
DO NOT SUBMIT
```

If previous execution is ambiguous:

```text
UNVERIFIED
```

Do not automatically retry.

---

# 62. Submission Action

Only the execution provider may perform the actual submit action.

The orchestrator must not contain browser-specific submit logic.

---

# 63. Submission Result

Provider returns:

```json
{
  "status": "SUBMITTED",
  "externalApplicationId": "GH-12345",
  "confirmationUrl": "...",
  "submittedAt": "..."
}
```

Only persist fields that are safe and necessary.

---

# 64. Submission Verification

After submission:

```text
verify confirmation
```

Possible signals:

```text
confirmation page
confirmation text
external application ID
provider API
email confirmation
application portal status
```

---

# 65. Verification Priority

Prefer:

```text
1. provider application ID
2. provider confirmation page
3. provider API status
4. confirmation email
5. other reliable evidence
```

---

# 66. Verification Outcomes

```text
VERIFIED
UNVERIFIED
FAILED
```

---

# 67. Verified Submission

Set:

```text
ApplicationStatus = SUBMITTED
ExecutionStatus = VERIFIED
```

Publish:

```text
application.submitted
```

---

# 68. Unverified Submission

If submit action happened but verification failed:

```text
ExecutionStatus = UNVERIFIED
```

Do not retry automatically.

Create:

```text
ACTION_REQUIRED
```

---

# 69. Failed Submission

If provider clearly confirms that submission did not happen:

```text
ExecutionStatus = FAILED
```

If retryable:

```text
schedule retry
```

Otherwise:

```text
ACTION_REQUIRED
```

---

# 70. Retry Policy

Retry only when:

```text
failure is known to be retryable
AND
submission definitely did not occur
AND
retry limit not exceeded
```

Retryable examples:

```text
temporary network failure
provider 503
temporary timeout before submission
```

Non-retryable:

```text
CAPTCHA
MFA
invalid required answer
unsupported provider
authentication failure
unknown submission state
```

---

# 71. Retry Limits

Default:

```text
maxExecutionRetries = 2
```

Configurable.

Never retry forever.

---

# 72. Exponential Backoff

Suggested:

```text
Attempt 1 → immediate
Attempt 2 → 30 seconds
Attempt 3 → 2 minutes
```

Values must be configurable.

---

# 73. Provider Rate Limits

If provider responds:

```text
429
```

then:

```text
pause execution
respect Retry-After if available
schedule retry
```

Do not hammer the provider.

---

# 74. Browser Errors

Examples:

```text
page timeout
element missing
navigation failure
browser crash
```

Classify as:

```text
RETRYABLE
or
ACTION_REQUIRED
```

based on whether submission could have occurred.

---

# 75. Navigation Safety

Before every navigation:

```text
validate target URL
validate provider domain
```

Do not follow arbitrary redirects without validation.

---

# 76. Domain Allowlist

Each provider should define allowed domains.

Example:

```text
GREENHOUSE:
greenhouse.io
boards.greenhouse.io

LEVER:
lever.co
jobs.lever.co
```

Actual implementation should use a configurable provider registry.

---

# 77. External Redirect

If redirected to an unexpected domain:

```text
STOP
```

Create:

```text
UNEXPECTED_DOMAIN
```

human action.

---

# 78. File Upload Safety

Only upload:

```text
approved application documents
```

Never upload arbitrary files from the filesystem.

---

# 79. Download Safety

Do not automatically download arbitrary files from external providers.

If a download is necessary:

```text
validate domain
validate file type
validate size
scan if available
```

---

# 80. Browser Automation Isolation

Each execution should have:

```text
isolated browser context
```

Avoid sharing state between users.

---

# 81. Browser Cleanup

After execution:

```text
close pages
close browser context
release session
release locks
```

Cleanup must run even after failures.

---

# 82. Execution Timeout

Total execution timeout:

```text
10 minutes default
```

If exceeded:

```text
STOP
```

Then determine whether submission occurred.

Never blindly retry.

---

# 83. Heartbeat

Long-running execution should update:

```text
lastHeartbeatAt
```

This allows stale execution detection.

---

# 84. Stale Execution

If:

```text
now - lastHeartbeatAt > timeout
```

then:

```text
mark execution stale
```

Before retrying:

```text
verify provider submission state
```

---

# 85. Execution Events

Publish events at important stages.

Required events:

```text
application.execution.started
application.execution.validated
application.execution.provider_selected
application.execution.form_discovered
application.execution.form_mapped
application.execution.validation_failed
application.execution.action_required
application.execution.submitting
application.execution.submitted
application.execution.verification_failed
application.execution.failed
application.execution.completed
```

Use the event architecture defined in `Events.md`.

---

# 86. Correlation ID

Every execution must have:

```text
correlationId
```

The same ID should be propagated through:

```text
logs
events
execution records
notifications
```

---

# 87. Audit Record

For every execution store:

```text
executionId
applicationId
provider
startedAt
completedAt
status
risk
dryRun
retryCount
externalApplicationId
correlationId
failureReason
```

---

# 88. Sensitive Data Rule

Do not store:

```text
passwords
MFA codes
session cookies
authentication tokens
full sensitive answers
```

in ordinary logs or audit records.

---

# 89. Error Classification

Use standardized categories:

```text
VALIDATION_ERROR
AUTHENTICATION_ERROR
AUTHORIZATION_ERROR
PROVIDER_UNSUPPORTED
CAPTCHA
MFA_REQUIRED
RATE_LIMITED
NETWORK_ERROR
TIMEOUT
FORM_MAPPING_ERROR
MISSING_DATA
FILE_ERROR
SUBMISSION_ERROR
VERIFICATION_ERROR
UNKNOWN
```

---

# 90. Error Recovery

Every failure must answer:

```text
Did submission definitely happen?
```

Possible:

```text
YES
NO
UNKNOWN
```

This determines recovery.

---

# 91. Recovery Matrix

```text
YES
→ mark submitted
→ verify
→ no retry

NO + retryable
→ retry

NO + non-retryable
→ action required

UNKNOWN
→ action required
→ do not retry automatically
```

---

# 92. Human Action Record

When stopping for human intervention create:

```text
ActionRequired
```

with:

```text
type
priority
applicationId
executionId
message
status
createdAt
```

Example:

```json
{
  "type": "CAPTCHA_DETECTED",
  "priority": "HIGH",
  "applicationId": "APP-123",
  "executionId": "EXE-123",
  "message": "Manual browser action required."
}
```

---

# 93. User Resume

After human intervention:

```text
ACTION_REQUIRED
      ↓
USER_RESOLVED
      ↓
REVALIDATE
      ↓
RESUME
```

Never resume from the previous step blindly.

Revalidate the page and application state first.

---

# 94. User Cancellation

User can cancel an execution.

Then:

```text
EXECUTING
 ↓
CANCELLED
```

The engine must stop safely.

If submission already happened:

```text
do not claim cancellation
```

Instead:

```text
SUBMITTED
```

---

# 95. Global Kill Switch

Execution must check:

```text
global automation kill switch
```

before:

```text
provider navigation
form submission
external communication
```

If enabled:

```text
STOP
```

---

# 96. Per-User Pause

Before execution:

```text
check user automation status
```

If paused:

```text
WAIT
```

Do not start external execution.

---

# 97. Per-Application Pause

Before every major execution stage:

```text
check application paused state
```

If paused:

```text
STOP
SAVE STATE
```

---

# 98. Final Submission Gate

The final submit action must pass all of:

```text
application approved
automation enabled
automation level allows submission
global kill switch OFF
user automation not paused
application not paused
provider supported
risk acceptable
all required fields valid
no unresolved action
no CAPTCHA
no MFA
no unknown required field
documents valid
duplicate check passed
execution lock acquired
dryRun = false
```

Only then:

```text
SUBMIT
```

---

# 99. Execution Decision Example

```text
Application:
Java Developer @ Example

Automation Level:
3

Provider:
GREENHOUSE

Risk:
LOW

Match:
94

ATS:
92

Fields:
18/18 mapped

Required Questions:
5/5 answered

Resume:
VALID

Cover Letter:
VALID

CAPTCHA:
NO

MFA:
NO

Duplicate:
NO

Kill Switch:
OFF

Dry Run:
FALSE

Decision:
SUBMIT
```

---

# 100. Execution Block Example

```text
Application:
Java Developer @ Example

Provider:
UNKNOWN

Decision:
BLOCKED

Reason:
Unsupported provider

Action:
Human review required
```

---

# 101. CAPTCHA Example

```text
Application:
Java Developer

Provider:
GREENHOUSE

Execution:
FORM_VALIDATED

CAPTCHA:
DETECTED

Decision:
STOP

State:
ACTION_REQUIRED

No submission attempt
```

---

# 102. Unknown Submission Example

```text
Submit button clicked

Browser:
connection lost

Provider status:
unknown

Decision:
UNVERIFIED

Action:
manual verification required

Retry:
NO
```

---

# 103. Execution API

Suggested internal REST endpoints:

```text
POST /api/v1/applications/{id}/execute

POST /api/v1/applications/{id}/dry-run

GET /api/v1/applications/{id}/execution

GET /api/v1/executions/{id}

POST /api/v1/executions/{id}/cancel

POST /api/v1/executions/{id}/resume

GET /api/v1/executions/{id}/actions
```

Exact endpoint design must follow the existing API conventions.

---

# 104. Execute Endpoint Rules

The endpoint must:

```text
authenticate user
 ↓
authorize application ownership
 ↓
validate application state
 ↓
check automation policy
 ↓
create execution command
 ↓
return execution ID
```

The HTTP request must NOT perform the entire browser workflow synchronously.

---

# 105. Asynchronous Execution

Execution must run asynchronously.

Recommended:

```text
REST
 ↓
Command/Event
 ↓
RabbitMQ
 ↓
Execution Worker
 ↓
Provider
```

This prevents HTTP timeouts.

---

# 106. Execution Queue

Recommended queue:

```text
application.execution.queue
```

Dead-letter queue:

```text
application.execution.dlq
```

---

# 107. Message Payload

Example:

```json
{
  "eventId": "EVT-123",
  "eventType": "application.execution.requested",
  "executionId": "EXE-123",
  "applicationId": "APP-123",
  "userId": "USER-123",
  "correlationId": "COR-123"
}
```

---

# 108. Idempotent Consumer

The execution worker must detect duplicate messages.

If:

```text
eventId
```

was already processed:

```text
ignore safely
```

---

# 109. Outbox Requirement

If the system writes database state and publishes an event, use the outbox pattern where appropriate.

Example:

```text
DB Transaction
    │
    ├── update execution
    │
    └── write outbox event
             ↓
       Outbox Publisher
             ↓
          RabbitMQ
```

---

# 110. Dead Letter Queue

Messages that repeatedly fail should move to:

```text
application.execution.dlq
```

Do not retry indefinitely.

---

# 111. Monitoring Metrics

Expose:

```text
execution_started_total
execution_completed_total
execution_failed_total
execution_action_required_total
execution_submitted_total
execution_unverified_total
execution_duration_seconds
execution_retry_total
execution_captcha_total
execution_mfa_total
execution_provider_error_total
```

---

# 112. Provider Metrics

Track per provider:

```text
success rate
failure rate
average execution duration
rate-limit count
CAPTCHA count
MFA count
verification success
```

---

# 113. Provider Health

Each provider should expose health information:

```text
AVAILABLE
DEGRADED
UNAVAILABLE
```

If unavailable:

```text
do not start new executions
```

Existing executions may finish if safe.

---

# 114. Provider Circuit Breaker

If a provider repeatedly fails:

```text
OPEN CIRCUIT
```

Temporarily stop new executions.

After cooldown:

```text
HALF_OPEN
```

Then test.

If successful:

```text
CLOSED
```

---

# 115. Browser Worker Architecture

Recommended:

```text
Spring Boot
     │
     ▼
Execution Queue
     │
     ▼
Browser Worker
     │
     ├── Browser Manager
     ├── Session Manager
     ├── Provider Adapter
     ├── Form Mapper
     └── Verification Engine
```

The browser worker should remain isolated from the main application logic.

---

# 116. Technology Guidance

The backend remains:

```text
Java 21
Spring Boot
MySQL
Redis
RabbitMQ
MinIO
Ollama
```

Browser execution technology may be selected during implementation after evaluating:

```text
Playwright
Selenium
```

Prefer a maintained, testable, headless-browser-compatible solution.

Do not introduce paid services unless explicitly approved.

---

# 117. Local Development

Developers must be able to run execution in:

```text
DRY_RUN
```

without submitting anything externally.

Production provider credentials/sessions must never be committed to Git.

---

# 118. Mock Provider

Implement a mock execution provider first.

Example:

```text
MockApplicationExecutionProvider
```

It should simulate:

```text
form discovery
mapping
validation
submission
verification
failure
CAPTCHA
MFA
timeout
```

This allows the entire workflow to be tested safely.

---

# 119. Mock Provider Scenarios

Support:

```text
SUCCESS
MISSING_FIELD
UNKNOWN_FIELD
CAPTCHA
MFA
TIMEOUT
PROVIDER_ERROR
SUBMISSION_UNKNOWN
RATE_LIMITED
```

---

# 120. Testing Strategy

## Unit Tests

Test:

```text
provider selection
risk calculation
field mapping
confidence thresholds
state transitions
pre-submit validation
retry policy
error classification
```

---

# 121. Integration Tests

Test:

```text
REST
 ↓
RabbitMQ
 ↓
Execution Worker
 ↓
Mock Provider
 ↓
Database
```

---

# 122. End-to-End Test

Required scenario:

```text
Job
 ↓
Qualified
 ↓
Application Prepared
 ↓
Approved
 ↓
Execution Requested
 ↓
Mock Provider
 ↓
Form Filled
 ↓
Submitted
 ↓
Verified
 ↓
Application SUBMITTED
```

---

# 123. CAPTCHA Test

Required:

```text
Execution
 ↓
CAPTCHA
 ↓
ACTION_REQUIRED
```

Assert:

```text
no submission
no retry
notification generated
state persisted
```

---

# 124. Unknown Submission Test

Required:

```text
Submit
 ↓
connection failure
 ↓
submission state UNKNOWN
```

Assert:

```text
UNVERIFIED
no automatic retry
human action created
```

---

# 125. Duplicate Execution Test

Run the same execution command twice.

Expected:

```text
one actual execution
```

Second command:

```text
ignored or safely attached to existing execution
```

---

# 126. Kill Switch Test

Enable kill switch.

Attempt execution.

Expected:

```text
NO EXTERNAL SUBMISSION
```

---

# 127. Dry Run Test

Run:

```text
dryRun = true
```

Expected:

```text
form discovery
field mapping
validation
```

but:

```text
NO SUBMISSION
```

---

# 128. Security Test

Verify:

```text
User A cannot execute User B application
User A cannot inspect User B execution
User A cannot resume User B execution
```

---

# 129. Browser Isolation Test

Verify:

```text
User A browser context
≠
User B browser context
```

No session leakage.

---

# 130. Data Retention

Execution artifacts should have configurable retention.

Avoid storing:

```text
entire webpage
entire browser history
unnecessary personal information
```

Store only what is required for:

```text
audit
debugging
verification
recovery
```

---

# 131. Screenshots

Screenshots may be captured only when useful for:

```text
failure debugging
human action
submission verification
```

Avoid storing screenshots containing unnecessary sensitive information.

---

# 132. Failure Screenshot

If execution fails:

```text
capture screenshot
```

only when policy allows.

Store in MinIO with:

```text
executionId
timestamp
retention policy
```

---

# 133. Human Review UI

When action is required, display:

```text
Application
Execution
Provider
Current State
Problem
Recommended Action
Resume Button
Cancel Button
```

Example:

```text
CAPTCHA detected.

The application is ready but requires manual browser interaction.

[Open / Continue]
[Cancel]
```

---

# 134. Resume Safety

When user resumes:

```text
verify execution still valid
verify application still approved
verify provider still correct
verify no duplicate submission
verify browser session
```

Then continue.

---

# 135. Application Expiry

Before execution verify the job is still active.

If:

```text
job.closed = true
```

then:

```text
CANCEL EXECUTION
```

Do not submit to closed jobs.

---

# 136. Job URL Validation

Before execution:

```text
job.url != null
job.url valid
provider recognized
domain allowed
```

---

# 137. External Job ID

If provider supplies:

```text
externalJobId
```

store it.

Use it for:

```text
deduplication
tracking
verification
```

---

# 138. Application Confirmation

After successful submission attempt, capture:

```text
provider
externalApplicationId
confirmationUrl if safe
confirmation timestamp
```

---

# 139. Confirmation Email

If an email confirmation is later received:

```text
match email
 ↓
application
 ↓
mark verification stronger
```

Do not create duplicate application records.

---

# 140. Post-Submission Actions

After verified submission:

```text
ApplicationStatus = SUBMITTED
        ↓
publish application.submitted
        ↓
schedule follow-up
        ↓
notify user
        ↓
update analytics
```

---

# 141. Post-Submission Notification

Example:

```text
✅ Application Submitted

Role:
Java Backend Developer

Company:
Example Corp

Match:
94%

ATS:
92%

Provider:
Greenhouse

Application ID:
GH-12345

Next follow-up:
5 business days
```

---

# 142. Execution Completion

Execution is complete only when:

```text
provider interaction finished
AND
browser session closed
AND
execution state persisted
AND
lock released
AND
event published
```

---

# 143. Lock Cleanup

Always release execution locks.

Even if:

```text
exception
timeout
provider error
browser crash
```

Use guaranteed cleanup.

---

# 144. Transaction Boundary

Do not hold a database transaction open during the entire browser interaction.

Bad:

```text
DB transaction
 ↓
10 minute browser interaction
 ↓
commit
```

Instead:

```text
persist state
 ↓
browser interaction
 ↓
persist result
```

---

# 145. Database Consistency

Execution state transitions should be transactional.

External browser operations are NOT part of a database transaction.

Use:

```text
state persistence
+
idempotency
+
outbox
```

for consistency.

---

# 146. Concurrency

Prevent:

```text
two workers
→ same application
→ same time
→ two submissions
```

Use:

```text
distributed lock
+
database state check
+
provider verification
```

---

# 147. Multi-Worker Support

The architecture must support:

```text
Worker 1
Worker 2
Worker 3
```

without duplicate application submission.

---

# 148. Execution Queue Priority

Support priorities:

```text
CRITICAL
HIGH
NORMAL
LOW
```

Interview-related or user-resumed actions may receive higher priority.

---

# 149. Fairness

One user must not consume the entire execution worker pool.

Implement per-user concurrency limits.

Default:

```text
1 active execution/user
```

---

# 150. Production Readiness Checklist

Before enabling real submissions:

```text
[ ] Mock provider passes
[ ] Dry-run passes
[ ] State machine passes
[ ] Idempotency passes
[ ] Distributed lock passes
[ ] Duplicate prevention passes
[ ] CAPTCHA stop passes
[ ] MFA stop passes
[ ] Unknown question stop passes
[ ] Unknown submission recovery passes
[ ] Kill switch passes
[ ] User pause passes
[ ] Provider allowlist passes
[ ] Authentication safety passes
[ ] Rate limiting passes
[ ] Retry policy passes
[ ] DLQ passes
[ ] Audit trail passes
[ ] Metrics passes
[ ] Browser isolation passes
[ ] Security tests pass
[ ] End-to-end tests pass
```

---

# 151. Definition of Done

Execution Engine is complete when:

* [ ] ApplicationExecutionProvider SPI exists
* [ ] Provider registry exists
* [ ] Provider detection exists
* [ ] Provider capability model exists
* [ ] Execution state machine exists
* [ ] Execution context exists
* [ ] Execution records exist
* [ ] Execution queue exists
* [ ] Execution worker exists
* [ ] Distributed locking exists
* [ ] Idempotency exists
* [ ] Form discovery exists
* [ ] Field mapping exists
* [ ] Mapping confidence exists
* [ ] Deterministic mapping exists
* [ ] AI-assisted mapping exists where required
* [ ] Required-field validation exists
* [ ] Resume upload exists
* [ ] Cover-letter upload exists
* [ ] Application answer handling exists
* [ ] Sensitive-question protection exists
* [ ] CAPTCHA detection exists
* [ ] MFA detection exists
* [ ] Authentication handling exists
* [ ] Provider domain validation exists
* [ ] Pre-submission validation exists
* [ ] Dry-run exists
* [ ] Submission exists for at least one supported provider
* [ ] Submission verification exists
* [ ] Retry policy exists
* [ ] Failure classification exists
* [ ] Human action workflow exists
* [ ] Resume-after-human-action exists
* [ ] Kill switch exists
* [ ] Per-user pause exists
* [ ] Per-application pause exists
* [ ] Provider circuit breaker exists
* [ ] Rate limiting exists
* [ ] Outbox integration exists
* [ ] DLQ exists
* [ ] Metrics exist
* [ ] Audit logging exists
* [ ] Mock provider exists
* [ ] Unit tests exist
* [ ] Integration tests exist
* [ ] End-to-end tests exist
* [ ] Security tests exist

---

# 152. Golden Rule

The execution engine must optimize for:

```text
SAFE SUBMISSION
```

not:

```text
MAXIMUM SUBMISSION
```

If the system is uncertain whether a submission occurred:

```text
STOP
VERIFY
ASK USER
```

Never risk duplicate applications.

If the system encounters:

```text
CAPTCHA
MFA
UNKNOWN REQUIRED QUESTION
UNKNOWN PROVIDER
UNKNOWN SUBMISSION STATE
```

the default behavior is:

```text
STOP
PERSIST
NOTIFY
WAIT FOR HUMAN
```

---

# END OF EXECUTION SPECIFICATION
