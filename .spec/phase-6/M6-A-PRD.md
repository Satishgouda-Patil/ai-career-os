# Phase 6 M6-A PRD — Browser Session Infrastructure & Read-Only Form Discovery

## 1. Objective

Build the browser automation foundation for AI Career OS.

M6-A allows the system to safely open real external job/application pages, inspect their structure, discover application forms, and normalize form fields.

M6-A is strictly READ-ONLY.

No real application submission, email sending, candidate-data modification, or automatic file upload is allowed.

---

# 2. Scope

M6-A includes:

1. Playwright browser session infrastructure
2. Browser lifecycle management
3. Application-specific browser sessions
4. Safe URL and domain validation
5. Redirect validation
6. Page navigation
7. Page metadata extraction
8. Application form discovery
9. Form field extraction
10. Field type detection
11. Required-field detection
12. Select/radio/checkbox option extraction
13. File field detection
14. Stable selector generation
15. Integration with existing form domain models
16. Screenshot capture
17. Browser execution audit logging
18. Safety enforcement
19. Unit and integration tests
20. Real browser verification

---

# 3. Explicit Non-Goals

M6-A MUST NOT implement:

- Real application submission
- Clicking Submit/Application buttons
- POST requests for application submission
- Email sending
- LinkedIn messaging
- Automatic candidate-data modification
- Automatic resume upload
- Automatic cover-letter upload
- Final ATS execution
- Unattended application execution
- Bypassing provider authentication
- CAPTCHA bypass
- Anti-bot bypass
- Security-control bypass

M6-A is a discovery and inspection milestone only.

---

# 4. Execution Modes

The browser system must support explicit execution modes.

```text
SANDBOX
READ_ONLY
LIVE




Yes. To start **Phase 6 M6-A**, create this file:

```text
.spec/phase-6/M6-A-PRD.md
```

Paste this content:

````md
# Phase 6 M6-A PRD — Browser Session Infrastructure & Read-Only Form Discovery

## 1. Objective

Build the browser automation foundation for AI Career OS.

M6-A allows the system to safely open real external job/application pages, inspect their structure, discover application forms, and normalize form fields.

M6-A is strictly READ-ONLY.

No real application submission, email sending, candidate-data modification, or automatic file upload is allowed.

---

# 2. Scope

M6-A includes:

1. Playwright browser session infrastructure
2. Browser lifecycle management
3. Application-specific browser sessions
4. Safe URL and domain validation
5. Redirect validation
6. Page navigation
7. Page metadata extraction
8. Application form discovery
9. Form field extraction
10. Field type detection
11. Required-field detection
12. Select/radio/checkbox option extraction
13. File field detection
14. Stable selector generation
15. Integration with existing form domain models
16. Screenshot capture
17. Browser execution audit logging
18. Safety enforcement
19. Unit and integration tests
20. Real browser verification

---

# 3. Explicit Non-Goals

M6-A MUST NOT implement:

- Real application submission
- Clicking Submit/Application buttons
- POST requests for application submission
- Email sending
- LinkedIn messaging
- Automatic candidate-data modification
- Automatic resume upload
- Automatic cover-letter upload
- Final ATS execution
- Unattended application execution
- Bypassing provider authentication
- CAPTCHA bypass
- Anti-bot bypass
- Security-control bypass

M6-A is a discovery and inspection milestone only.

---

# 4. Execution Modes

The browser system must support explicit execution modes.

```text
SANDBOX
READ_ONLY
LIVE
````

For M6-A:

```text
Default Mode = READ_ONLY
LIVE Mode = DISABLED
```

Any attempt to execute a submission action must be blocked.

---

# 5. Browser Session Architecture

Create a browser session abstraction.

Suggested package:

```text
com.ai.career.execution.browser
```

Suggested components:

```text
BrowserSession
BrowserSessionManager
BrowserSessionManagerImpl
BrowserSafetyPolicy
BrowserNavigationService
BrowserDiscoveryService
BrowserScreenshotService
```

The implementation may reuse existing Playwright infrastructure where available.

Do not duplicate existing browser abstractions unnecessarily.

---

# 6. Browser Session Lifecycle

Every browser session must follow:

```text
CREATE
  ↓
VALIDATE TARGET
  ↓
OPEN BROWSER
  ↓
CREATE CONTEXT
  ↓
CREATE PAGE
  ↓
NAVIGATE
  ↓
INSPECT
  ↓
DISCOVER FORM
  ↓
CAPTURE RESULT
  ↓
CLOSE PAGE
  ↓
CLOSE CONTEXT
  ↓
CLOSE BROWSER
```

Browser resources must always be released.

Use safe cleanup with:

```text
try
finally
```

Browser failures must not leave orphaned browser processes.

---

# 7. URL Safety

Before navigation, validate the target URL.

Validation must include:

* Valid URL syntax
* HTTP/HTTPS protocol
* Allowed hostname
* No unsupported protocol
* No dangerous redirect
* No unexpected domain change

Initial supported ATS domain:

```text
greenhouse.io
boards.greenhouse.io
```

The implementation should allow future providers to be added through configuration rather than hardcoding the architecture around one provider.

---

# 8. Redirect Safety

After navigation, validate the final URL.

Example:

```text
Requested:
https://boards.greenhouse.io/example/jobs/123
```

Allowed:

```text
https://boards.greenhouse.io/example/jobs/123
```

Unexpected:

```text
https://unknown-site.example/apply
```

If the final destination is outside the allowed domain:

```text
ACTION_REQUIRED
```

The browser must stop.

Do not continue discovery on an unsafe destination.

---

# 9. Navigation Safety

Configure:

* Navigation timeout
* Page-load timeout
* Browser startup timeout
* Maximum redirects
* Maximum page size where practical

Timeouts must produce controlled errors.

Example:

```text
BROWSER_TIMEOUT
NAVIGATION_FAILED
UNSAFE_REDIRECT
DOMAIN_NOT_ALLOWED
```

Do not silently treat failures as successful discovery.

---

# 10. Read-Only Browser Policy

The browser policy must explicitly prevent dangerous actions.

The following actions are prohibited in M6-A:

```text
SUBMIT
APPLY
SEND
POST
UPLOAD
DELETE
PURCHASE
MESSAGE
```

The discovery engine may perform:

```text
GET
NAVIGATE
READ DOM
QUERY SELECTORS
EXTRACT TEXT
EXTRACT ATTRIBUTES
CAPTURE SCREENSHOT
```

If an element appears to be a submission control, it may be identified but MUST NOT be clicked.

Example:

```text
button[type="submit"]
input[type="submit"]
button:has-text("Apply")
button:has-text("Submit")
```

These controls should be recorded as discovered controls, not executed.

---

# 11. Form Discovery

The discovery service must inspect the page for application forms.

Detect:

```html
<form>
```

and common application-form containers.

The discovery engine should identify:

* Form ID
* Form action
* Form method
* Form location
* Number of fields
* Field labels
* Field types
* Required state
* Options
* Selectors

The form action must only be inspected.

Do not submit the form.

---

# 12. Field Discovery

The engine must discover common HTML fields:

```text
input
textarea
select
button
```

Input types to detect:

```text
text
email
tel
url
number
date
checkbox
radio
file
hidden
password
submit
button
```

Unknown types should become:

```text
UNKNOWN
```

---

# 13. Field Normalization

Reuse the existing:

```text
NormalizedFormField
```

from Phase 3 / Phase 3 M4.

Do not create a duplicate normalized field model unless absolutely necessary.

Each discovered field should contain, where available:

```text
field selector
original field id
name
label
normalized label
field type
required
options
```

Example:

```json
{
  "fieldSelector": "#first_name",
  "originalFieldId": "first_name",
  "normalizedLabel": "first name",
  "type": "TEXT",
  "required": true
}
```

---

# 14. Label Detection

Attempt label discovery in this order:

1. `<label for="...">`
2. `aria-label`
3. `aria-labelledby`
4. `name`
5. `placeholder`
6. surrounding semantic text

If no label can be reliably identified:

```text
label = UNKNOWN
```

Do not invent labels.

---

# 15. Required Field Detection

Detect required state from:

```text
required attribute
aria-required="true"
provider-specific required markers
```

If required status cannot be reliably determined:

```text
required = false
requiredConfidence = LOW
```

Do not assume a field is required simply because it looks important.

---

# 16. Select / Radio / Checkbox Discovery

For:

```text
<select>
```

extract:

```text
option value
option visible text
selected state
```

For:

```text
radio
checkbox
```

extract:

```text
value
label
checked state
```

No option should be selected or changed during M6-A.

---

# 17. File Field Discovery

Detect:

```html
<input type="file">
```

Record:

```text
field selector
field label
accept attribute
required state
```

M6-A MUST NOT upload a file.

Example:

```json
{
  "type": "FILE",
  "label": "Resume",
  "accept": ".pdf,.doc,.docx",
  "required": true
}
```

---

# 18. Stable Selector Strategy

Generate a selector using the safest available identifier.

Preferred order:

```text
id
name
data-* attribute
aria attribute
stable CSS selector
```

Avoid selectors based only on:

```text
nth-child
dynamic class names
random generated IDs
```

The selector must be stored in:

```text
NormalizedFormField.fieldSelector
```

---

# 19. Page Metadata

Capture:

```text
URL
final URL
page title
hostname
HTTP/navigation status where available
timestamp
form count
field count
```

Example:

```json
{
  "url": "https://boards.greenhouse.io/example/jobs/123",
  "finalUrl": "https://boards.greenhouse.io/example/jobs/123",
  "title": "Software Engineer",
  "hostname": "boards.greenhouse.io",
  "formsDetected": 1,
  "fieldsDetected": 14
}
```

---

# 20. Screenshots

Capture screenshots for discovery debugging and audit.

At minimum:

```text
before-discovery.png
after-discovery.png
```

Screenshots must not expose secrets.

Do not intentionally capture:

* passwords
* authentication tokens
* cookies
* API keys
* private credentials

---

# 21. Audit Logging

Reuse the existing integration audit infrastructure from Phase 5 M4.

Record sanitized events such as:

```text
BROWSER_SESSION_CREATED
BROWSER_NAVIGATION
FORM_DISCOVERED
FORM_DISCOVERY_COMPLETED
BROWSER_SESSION_CLOSED
BROWSER_DISCOVERY_FAILED
UNSAFE_REDIRECT_BLOCKED
UNSUPPORTED_DOMAIN_BLOCKED
```

Audit records must not contain:

```text
passwords
API keys
bearer tokens
cookies
session tokens
credential payloads
```

---

# 22. Integration With Existing Form Engine

M6-A must connect browser discovery to the existing Phase 3 M4 form-analysis architecture.

Flow:

```text
Browser Page
     ↓
BrowserDiscoveryService
     ↓
Raw Form Definition
     ↓
FormNormalizationService
     ↓
NormalizedFormField
     ↓
FieldClassificationService
     ↓
AnswerMappingService
     ↓
ApplicationFormPlan
```

M6-A itself should primarily perform:

```text
DISCOVERY
NORMALIZATION INPUT
```

Existing AI mapping logic should remain responsible for classification and candidate mapping.

---

# 23. Application-Level Flow

The browser discovery flow should be available for an application.

Suggested flow:

```text
Application
    ↓
Validate Application
    ↓
Acquire Distributed Execution Lock
    ↓
Validate Target URL
    ↓
Create Browser Session
    ↓
Navigate
    ↓
Validate Final Domain
    ↓
Discover Form
    ↓
Normalize Fields
    ↓
Persist/Return Form Discovery Result
    ↓
Audit
    ↓
Release Lock
    ↓
Close Browser
```

Lock release must happen in:

```text
finally
```

---

# 24. REST API

Add a read-only discovery endpoint.

Suggested:

```http
POST /api/v1/applications/{id}/browser/discover
```

The endpoint must:

1. Authenticate the user
2. Verify application ownership
3. Validate application state
4. Acquire distributed execution lock
5. Launch read-only browser session
6. Discover the form
7. Return normalized fields
8. Persist audit information
9. Release the lock
10. Close browser resources

It must NOT submit the application.

---

# 25. Example Response

```json
{
  "applicationId": 123,
  "status": "DISCOVERED",
  "executionMode": "READ_ONLY",
  "submissionAttempted": false,
  "targetUrl": "https://boards.greenhouse.io/example/jobs/123",
  "finalUrl": "https://boards.greenhouse.io/example/jobs/123",
  "formsDetected": 1,
  "fieldsDetected": 16,
  "fields": [
    {
      "fieldSelector": "#first_name",
      "normalizedLabel": "first name",
      "type": "TEXT",
      "required": true
    },
    {
      "fieldSelector": "#email",
      "normalizedLabel": "email",
      "type": "EMAIL",
      "required": true
    }
  ]
}
```

---

# 26. Error States

Use explicit failure states.

Examples:

```text
DOMAIN_NOT_ALLOWED
UNSAFE_REDIRECT
NAVIGATION_FAILED
BROWSER_TIMEOUT
FORM_NOT_FOUND
DISCOVERY_FAILED
LOCK_NOT_ACQUIRED
AUTHENTICATION_REQUIRED
ACTION_REQUIRED
```

Never return fake successful discovery.

---

# 27. Authentication Handling

If a real external page requires authentication:

```text
AUTHENTICATION_REQUIRED
```

The browser must stop.

M6-A must NOT:

* bypass authentication
* bypass CAPTCHA
* bypass MFA
* steal/session-copy credentials
* attempt credential guessing

---

# 28. Idempotency

Repeated discovery of the same application must not create uncontrolled duplicate execution records.

Use the existing application execution/idempotency architecture where applicable.

Repeated discovery should be safe.

---

# 29. Concurrency

M6-A must reuse:

```text
DistributedExecutionLock
```

Lock key:

```text
application-browser-discovery:{applicationId}
```

Only one browser discovery execution should run for the same application at a time.

If Redis is unavailable:

```text
LOCK_NOT_ACQUIRED
```

and browser execution must NOT begin.

---

# 30. Testing Requirements

Create tests covering:

### Browser Session Tests

* Browser starts
* Browser closes
* Context closes
* Page closes
* Timeout handling
* Cleanup on exception

### URL Safety Tests

* Allowed Greenhouse URL
* Unsupported domain
* Unsafe redirect
* Invalid URL
* HTTP/HTTPS validation

### Form Discovery Tests

* Basic form
* Multiple forms
* Text input
* Email input
* Phone input
* Textarea
* Select
* Radio
* Checkbox
* File input
* Required fields
* Unknown field types

### Selector Tests

* ID selector
* Name selector
* Data attribute selector
* Fallback selector
* Dynamic selector rejection

### Safety Tests

* Submit button is never clicked
* Submit form is never executed
* File is never uploaded
* No outbound email
* No application state transition to APPLIED
* No live ATS submission

### Concurrency Tests

* Same application with multiple requests
* Exactly one discovery execution
* Others fail with `LOCK_NOT_ACQUIRED`

### Failure Tests

* Redis unavailable
* Browser startup failure
* Navigation timeout
* Domain rejection
* Unsafe redirect
* Authentication-required page

---

# 31. Regression Requirements

Run:

```bash
./gradlew test
```

All existing Phase 1–5 tests must continue passing.

No existing safety policy may be weakened.

---

# 32. Browser Verification

After automated tests pass:

1. Start backend.
2. Start frontend.
3. Open the application.
4. Select a supported Greenhouse application.
5. Trigger browser discovery.
6. Verify the browser navigates to the target.
7. Verify form fields are discovered.
8. Verify normalized fields appear.
9. Verify audit event is recorded.
10. Verify browser closes correctly.
11. Verify no submission occurs.

Verify explicitly:

```text
submissionAttempted = false
```

---

# 33. Safety Acceptance Criteria

M6-A is considered complete only if:

* [ ] Browser session infrastructure works.
* [ ] Browser cleanup works.
* [ ] Domain allowlist works.
* [ ] Unsafe redirects are blocked.
* [ ] Greenhouse pages can be inspected.
* [ ] Forms can be discovered.
* [ ] Fields are normalized.
* [ ] Required fields are detected.
* [ ] Select/radio/checkbox options are detected.
* [ ] File inputs are detected without uploading.
* [ ] Screenshots are captured safely.
* [ ] Audit events are persisted.
* [ ] Distributed execution lock is enforced.
* [ ] Redis failure fails closed.
* [ ] Authentication-required pages stop safely.
* [ ] Submit controls are never executed.
* [ ] No real application is submitted.
* [ ] No email is sent.
* [ ] No candidate facts are fabricated.
* [ ] Existing tests remain green.
* [ ] Browser verification passes.

---

# 34. Git Requirements

After successful implementation and verification:

```bash
git status
git add .
git commit -m "feat(phase6-m6-a): implement browser session infrastructure and read-only form discovery"
git push origin main
```

Record the commit hash.

---

# 35. Mandatory Stop Gate

After M6-A is complete:

**STOP.**

Do not implement M6-B automatically.

Provide a walkthrough containing:

* Files created/modified
* Architecture changes
* Browser safety behavior
* Form discovery behavior
* Test results
* Browser verification
* Commit hash
* Confirmation that no real submission occurred

Wait for explicit approval before starting:

```text
Phase 6 M6-B — Sandbox Form Interaction & Verification
```

---

# 36. Definition of Done

M6-A is DONE when the system can:

```text
Open supported real job page
        ↓
Validate domain
        ↓
Validate redirects
        ↓
Inspect page
        ↓
Discover application form
        ↓
Extract fields
        ↓
Normalize fields
        ↓
Generate ApplicationFormPlan input
        ↓
Persist audit
        ↓
Close browser
```

while guaranteeing:

```text
REAL SUBMISSION = NO
EMAIL SENDING = NO
FILE UPLOAD = NO
AUTO APPLY = NO
CANDIDATE DATA MODIFICATION = NO
```

M6-A must end in a safe read-only state.

```

### Then tell Antigravity:

> **Proceed with Phase 6 M6-A using `M6-A-PRD.md`. Implement only M6-A. Do not start M6-B. Follow the mandatory stop gate, run all regression tests and real browser verification, commit and push to `main`, then STOP and give me the walkthrough.**
```
