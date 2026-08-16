# PHASE 3 — M6-A
# Browser Infrastructure + Read-Only Page/Form Discovery

## STATUS

Phase 3 Milestones 1–5 are completed and approved.

Proceed with ONLY M6-A.

Do NOT implement M6-B or M6-C.

---

# 1. OBJECTIVE

Introduce browser automation infrastructure and read-only page discovery.

The browser must be able to:

1. Start safely.
2. Navigate to a supplied URL.
3. Load the page.
4. Inspect the DOM.
5. Detect forms.
6. Extract form fields.
7. Extract labels/options/required attributes.
8. Convert the discovered form into the existing FormDefinition model.
9. Produce a structured discovery result.

This milestone is READ-ONLY.

The browser must NEVER:

- submit a form
- click a submit button
- send an application
- send email
- upload a resume
- upload a cover letter
- modify external data
- bypass CAPTCHA
- bypass anti-bot systems
- solve challenges
- evade detection

---

# 2. TECHNOLOGY

Inspect the current Gradle project first.

Use Java 21 and the existing Spring Boot architecture.

Prefer Playwright for Java if compatible with the current project.

Add the dependency only if required.

Use a pinned/stable version.

Do NOT introduce Selenium unless Playwright is technically impossible.

Browser binaries must be locally installable.

Document installation requirements.

---

# 3. ARCHITECTURE

Create a provider-independent browser abstraction.

Conceptually:

BrowserSession
BrowserSessionFactory
BrowserPage
BrowserDiscoveryService

Do NOT tightly couple the application domain to Playwright classes.

Use an adapter architecture:

Application
    ↓
Browser Discovery Service
    ↓
Browser Abstraction
    ↓
Playwright Adapter
    ↓
Chromium

The rest of the application must not depend directly on Playwright APIs.

---

# 4. BROWSER SESSION

Create a browser session abstraction supporting:

- launch
- navigate
- current URL
- page title
- page content
- close

The session must implement safe resource cleanup.

Use try/finally or equivalent lifecycle management.

Browser processes must never remain running after the operation finishes.

---

# 5. CONFIGURATION

Create configuration properties.

Example:

app.browser.enabled=false
app.browser.headless=true
app.browser.timeout-ms=30000
app.browser.navigation-timeout-ms=30000
app.browser.max-pages=1

Use safe defaults.

IMPORTANT:

Real browser execution must be disabled by default.

M6-A is an opt-in capability.

---

# 6. SECURITY

Only allow HTTP/HTTPS URLs.

Reject:

- file://
- javascript:
- data:
- localhost unless explicitly enabled for testing
- private network addresses unless explicitly enabled for testing

Prevent obvious SSRF risks.

Do not allow arbitrary internal network access.

Create URL validation.

---

# 7. READ-ONLY NAVIGATION

The browser may:

- navigate
- wait for page load
- inspect DOM
- inspect attributes
- inspect text
- inspect forms

The browser may NOT:

- click submit
- press Enter on forms
- upload files
- enter candidate data
- select candidate answers
- send external requests intentionally beyond normal page navigation/resources

Normal browser page resource loading is acceptable.

---

# 8. FORM DISCOVERY

Create:

BrowserFormDiscoveryService

Input:

URL

Output:

BrowserDiscoveryResult

Conceptually:

BrowserDiscoveryResult
 ├── url
 ├── finalUrl
 ├── title
 ├── forms[]
 ├── discoveredAt
 └── warnings[]

Each discovered form contains:

- form identifier
- action URL
- method
- fields
- buttons
- metadata

---

# 9. FIELD DISCOVERY

Extract:

- name
- id
- tag
- type
- label
- placeholder
- required
- disabled
- readonly
- options
- section
- selector
- aria-label
- description/help text

Supported fields:

input
textarea
select
radio
checkbox
file

Do NOT interact with the fields.

---

# 10. LABEL RESOLUTION

Implement deterministic label resolution.

Priority:

1. explicit <label for="">
2. wrapping <label>
3. aria-label
4. aria-labelledby
5. placeholder
6. nearby descriptive text
7. field name/id

Preserve the source used for the label.

Example:

labelSource = EXPLICIT_LABEL

This will help debugging later.

---

# 11. FIELD TYPE NORMALIZATION

Map discovered HTML fields to the existing:

FieldType

Examples:

<input type="email">
→ EMAIL

<input type="tel">
→ PHONE

<input type="url">
→ URL

<input type="file">
→ FILE

<textarea>
→ TEXTAREA

<select>
→ SELECT

checkbox
→ CHECKBOX

radio
→ RADIO

Unknown types:
→ UNKNOWN

Reuse existing Phase 3 form models.

Do NOT create duplicate FieldType enums.

---

# 12. OPTIONS

For SELECT:

extract:

- value
- visible text
- selected state
- disabled state

For RADIO:

extract available options.

For CHECKBOX:

extract:

- value
- label
- checked state
- required state

Do NOT change state.

---

# 13. REQUIRED FIELD DETECTION

Determine required status from:

- required attribute
- aria-required
- provider-specific semantic hints where safe

Do not guess required status from label text alone.

Preserve:

required
requiredSource

---

# 14. FORM IDENTIFICATION

Forms may not have an HTML id.

Create deterministic internal identifiers.

Never overwrite the provider's original identifiers.

Example:

form-1
form-2

Field selectors should remain stable enough for later interaction.

Prefer:

id
name
CSS selector

Do not rely only on DOM index.

---

# 15. DISCOVERY RESULT

Create a structured result.

Example:

{
  "url": "...",
  "finalUrl": "...",
  "title": "...",
  "forms": [
    {
      "id": "...",
      "action": "...",
      "method": "POST",
      "fields": [
        {
          "id": "...",
          "name": "...",
          "label": "...",
          "fieldType": "EMAIL",
          "required": true,
          "selector": "...",
          "options": []
        }
      ]
    }
  ],
  "warnings": []
}

This is discovery only.

---

# 16. EXISTING FORM PIPELINE

Integrate with the existing:

FormNormalizationService
FieldClassificationService
AnswerMappingService
FormReadinessService

BUT:

M6-A must NOT automatically fill fields.

The output should be compatible with the existing Phase 3 form analysis pipeline.

Conceptually:

Browser
 ↓
Raw FormDefinition
 ↓
Existing Form Normalization
 ↓
Existing Classification
 ↓
Existing Mapping
 ↓
Existing Readiness

Do not bypass existing safety logic.

---

# 17. CAPTCHA DETECTION

Detect obvious CAPTCHA/challenge indicators.

Examples:

- CAPTCHA text
- reCAPTCHA
- hCaptcha
- challenge pages
- "verify you are human"

If detected:

return:

CAPTCHA_DETECTED

Do NOT solve it.

Do NOT bypass it.

Do NOT continue automatically.

---

# 18. LOGIN DETECTION

Detect obvious authentication requirements.

Examples:

- login form
- username/password fields
- authentication redirects

Return:

LOGIN_REQUIRED

Do not attempt login.

Do not store credentials.

---

# 19. EXTERNAL NAVIGATION SAFETY

If the page redirects to a different domain:

record:

originalUrl
finalUrl
redirected=true

Do not automatically trust arbitrary redirects.

Implement an allowlist mechanism for future provider integrations.

For M6-A:

external redirects should be visible in the result.

---

# 20. API

Create an authenticated API:

POST /api/v1/browser/discover

Request:

{
  "url": "https://example.com/job/application"
}

Response:

BrowserDiscoveryResult

The endpoint must:

- validate URL
- authenticate user
- enforce ownership/context where applicable
- start browser
- discover forms
- close browser
- return result

No candidate information should be entered.

---

# 21. APPLICATION-SPECIFIC DISCOVERY

If an application ID is supplied:

POST /api/v1/applications/{id}/browser/discover

Verify:

- application belongs to authenticated user
- job/application URL exists
- URL is valid

Store the discovery result only if persistence is necessary.

Avoid unnecessary persistence.

---

# 22. LOGGING

Never log:

- passwords
- tokens
- cookies
- authorization headers
- resume contents
- cover letter contents
- complete candidate personal data

Log only:

- application ID
- discovery ID
- domain
- status
- duration
- number of forms
- number of fields
- warnings

---

# 23. TESTING

Create tests for:

### URL validation

- HTTPS allowed
- HTTP allowed
- file rejected
- javascript rejected
- data rejected
- unsafe private address rejected

### Browser session

- launch
- navigation
- cleanup

### Form discovery

- simple form
- multiple forms
- textarea
- select
- radio
- checkbox
- file field
- required fields

### Label discovery

- label-for
- wrapped label
- aria-label
- placeholder
- name/id fallback

### CAPTCHA

Must detect CAPTCHA.

Must NOT interact with it.

### Login

Must detect login requirement.

Must NOT login.

### Integration

Use local test HTML fixtures.

Do NOT depend on external websites for tests.

---

# 24. LOCAL TEST FIXTURES

Create local HTML fixtures:

1. simple-form.html
2. complex-form.html
3. labels.html
4. required-fields.html
5. captcha.html
6. login.html
7. multiple-forms.html

Serve fixtures using a local test HTTP server if needed.

No external network dependency.

---

# 25. ZERO-SUBMISSION GUARANTEE

Create an automated test proving:

Discovery cannot submit a form.

The test must verify:

- no submit event
- no form submission
- no button click
- no file upload
- no candidate value entered

---

# 26. BUILD

Run:

./gradlew test

./gradlew build

All existing tests must pass.

Regression:

Phase 1
Phase 2
Phase 3 M1
M2
M3
M4
M5

No regression allowed.

---

# 27. DOCUMENTATION

Update Phase 3 documentation with:

- browser architecture
- Playwright adapter
- security model
- SSRF protection
- discovery model
- CAPTCHA handling
- login handling
- zero-submission guarantee
- configuration
- local installation

---

# 28. STOP CONDITION

STOP AFTER M6-A.

Do NOT implement:

M6-B
M6-C
real form filling
real submission
real job provider

Provide walkthrough:

- architecture
- dependencies
- files created
- files modified
- APIs
- browser configuration
- discovery behavior
- security
- tests
- build
- regression
- git commit
- git push
- limitations

WAIT FOR APPROVAL.