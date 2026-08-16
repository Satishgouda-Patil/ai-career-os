# PHASE 3 — M6-B
# Controlled Form Interaction + Submission Verification

## STATUS

M6-A must be completed and approved before starting M6-B.

Proceed with ONLY M6-B.

Do NOT implement M6-C.

---

# 1. OBJECTIVE

Build the controlled browser interaction engine.

The system must be able to:

1. Load an already discovered form.
2. Resolve fields against the existing ApplicationFormPlan.
3. Fill safe fields.
4. Upload approved artifacts in a controlled test environment.
5. Verify the resulting field state.
6. Produce a submission preview.
7. Detect mismatches.
8. STOP before actual submission.

This milestone MUST NOT submit a real application.

---

# 2. SAFETY MODEL

The browser execution pipeline is:

Form Discovery
 ↓
Existing Form Plan
 ↓
Validation
 ↓
User Authorization
 ↓
Controlled Interaction
 ↓
Verification
 ↓
Submission Preview
 ↓
STOP

There is NO automatic final submit in M6-B.

---

# 3. REUSE EXISTING SYSTEM

Reuse:

Application
ApplicationFormPlan
FieldAnswerMapping
ApplicationValidationResult
DryRunReport
ApplicationApproval
DistributedExecutionLock
ApplicationStateMachine
ApplicationExecution
ApplicationExecutionProvider
Browser abstraction from M6-A

Do not create duplicate safety mechanisms.

---

# 4. INTERACTION ABSTRACTION

Create:

BrowserFormInteractor

Capabilities:

- fill text
- select option
- check checkbox
- select radio
- upload file
- read current value
- inspect field state

All actions must be explicit.

---

# 5. FIELD ACTION TYPES

Create:

FILL_TEXT
SELECT_OPTION
SELECT_MULTI_OPTION
CHECK
UNCHECK
UPLOAD_FILE
NO_ACTION
REQUIRES_REVIEW
UNSUPPORTED

Map these from existing MappingType/FieldType.

Do not perform unsupported actions.

---

# 6. SAFE FIELD RULES

Automatically interact only with:

- ordinary profile fields
- email
- phone
- name
- location
- portfolio
- LinkedIn
- GitHub
- skills
- resume upload
- cover letter upload

ONLY when:

- mapping is deterministic
- validation passed
- user authorization exists
- provider supports the capability

---

# 7. SENSITIVE FIELDS

Never automatically answer:

- work authorization
- visa status
- legal questions
- demographic questions
- disability
- veteran status
- salary history
- criminal history
- other sensitive personal questions

unless an explicit trusted candidate value and authorization policy exists.

Default:

REQUIRES_REVIEW

---

# 8. AI-GENERATED ANSWERS

AI-generated answers must remain:

REQUIRES_REVIEW

unless explicitly approved.

Never fill an AI-generated answer silently.

The browser must stop before such a field.

---

# 9. FORM FIELD MATCHING

Match ApplicationFormPlan fields to discovered browser fields using:

1. stable field ID
2. name
3. selector
4. normalized label
5. semantic category

Do NOT rely solely on DOM position.

If ambiguous:

REQUIRES_REVIEW

Do not guess.

---

# 10. INTERACTION PLAN

Create:

BrowserInteractionPlan

Containing:

- applicationId
- formId
- actions
- unresolvedFields
- reviewFields
- warnings

Example:

{
  "actions": [
    {
      "field": "email",
      "action": "FILL_TEXT",
      "source": "PROFILE.EMAIL"
    },
    {
      "field": "resume",
      "action": "UPLOAD_FILE",
      "source": "RESUME_VERSION"
    }
  ]
}

---

# 11. PRE-INTERACTION VALIDATION

Before touching the page:

Verify:

- application ownership
- application state
- form plan exists
- validation passed
- provider supports required capabilities
- user authorization exists
- distributed lock acquired
- browser session valid

If any fails:

DO NOT interact.

---

# 12. DISTRIBUTED LOCK

Reuse existing:

DistributedExecutionLock

Lock:

application-execution:{applicationId}

The interaction operation must never run concurrently for the same application.

Fail closed if Redis is unavailable.

---

# 13. FIELD FILLING

For text fields:

- focus
- fill
- read back
- verify

For select:

- select exact option
- read back
- verify

For checkbox:

- set desired state
- read back
- verify

For radio:

- select exact option
- read back
- verify

For files:

- verify local file exists
- verify MIME type
- upload only approved file
- verify selected file metadata where possible

---

# 14. NEVER APPROXIMATE OPTIONS

If the requested value does not exactly match an available option:

STOP.

Return:

OPTION_MISMATCH

Do not choose a "closest" option.

---

# 15. VERIFICATION

After every action:

Verify the resulting DOM state.

Examples:

text:
actual value == expected value

select:
selected option == expected option

checkbox:
checked == expected

radio:
selected == expected

file:
expected file selected

Any mismatch:

INTERACTION_VERIFICATION_FAILED

Stop further interaction.

---

# 16. SUBMISSION PREVIEW

After all safe interactions:

Create:

SubmissionPreview

Containing:

- applicationId
- formId
- fields
- values represented safely
- files
- unresolved fields
- warnings
- validation result
- verification result
- readyForSubmission=false

IMPORTANT:

Always return:

readyForSubmission = false

in M6-B.

---

# 17. NO FINAL SUBMIT

Do NOT:

- click submit
- press Enter to submit
- trigger form.submit()
- call external APIs
- send application

Even if the page appears completely ready.

---

# 18. SUBMIT BUTTON DETECTION

Detect:

- submit button
- submit input
- application buttons

Record:

submitControlDetected=true

but do not interact with it.

---

# 19. CAPTCHA / CHALLENGE

If CAPTCHA appears:

STOP immediately.

Return:

CAPTCHA_DETECTED

Do not solve or bypass.

---

# 20. LOGIN

If authentication is required:

STOP.

Return:

LOGIN_REQUIRED

Do not enter credentials.

---

# 21. BROWSER STATE

Create safe execution context.

Do not persist:

- cookies
- passwords
- session tokens

unless explicitly required later by the approved architecture.

M6-B should use ephemeral sessions.

---

# 22. API

Add:

POST /api/v1/applications/{id}/browser/prepare

POST /api/v1/applications/{id}/browser/execute-interaction

GET /api/v1/applications/{id}/browser/preview

The interaction API must require explicit authorization.

There must be no submit endpoint.

---

# 23. MOCK/LOCAL APPLICATION

Create a local fake job application site.

It should contain:

- name
- email
- phone
- select
- checkbox
- radio
- resume upload
- cover letter upload
- custom question
- sensitive question
- submit button

The application should record whether submit was attempted.

M6-B tests must prove:

submit count = 0

---

# 24. TESTS

Test:

- text fill
- select
- checkbox
- radio
- file upload
- field matching
- ambiguous matching
- option mismatch
- verification mismatch
- missing required field
- sensitive field
- AI-generated field
- CAPTCHA
- login
- Redis lock
- authorization
- ownership
- zero submission

---

# 25. FAILURE BEHAVIOR

Any unexpected browser error must:

1. stop interaction
2. preserve execution record
3. release lock
4. return structured failure
5. never submit

Use try/finally for browser and lock cleanup.

---

# 26. AUDIT

Record:

- interaction started
- actions attempted
- actions completed
- verification results
- stop reason
- timestamp
- application ID

Never log candidate secrets or complete sensitive values.

---

# 27. BUILD

Run:

./gradlew test

./gradlew build

Regression:

Phase 1
Phase 2
Phase 3 M1–M5
M6-A

Everything must pass.

---

# 28. DOCUMENTATION

Document:

- interaction architecture
- action model
- verification
- authorization
- lock usage
- failure behavior
- CAPTCHA behavior
- login behavior
- zero-submit guarantee

---

# 29. STOP CONDITION

STOP AFTER M6-B.

Do NOT implement:

M6-C
real job provider
real application submission

Provide walkthrough:

- architecture
- browser interaction engine
- action model
- verification
- APIs
- tests
- zero-submit proof
- build
- regression
- git commit
- git push
- limitations

WAIT FOR APPROVAL.