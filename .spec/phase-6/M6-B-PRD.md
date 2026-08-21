**Proceed to M6-B now.**

Use the existing architecture from M6-A; don't rebuild the browser infrastructure.

> **Approved — proceed with Phase 6 M6-B: Sandbox Form Interaction & Verification.**
>
> Use the existing M6-A browser session infrastructure, `BrowserSafetyPolicy`, `BrowserDiscoveryService`, `NormalizedFormField`, `ApplicationFormPlan`, `DistributedExecutionLock`, and `IntegrationAuditService`.
>
> Implement **only M6-B**.
>
> ### M6-B goal
>
> Take the form discovered by M6-A and safely interact with it in a **sandbox/mock browser environment**:
>
> ```text
> Discovered Form
>      ↓
> ApplicationFormPlan
>      ↓
> Candidate Field Mapping
>      ↓
> Sandbox Browser
>      ↓
> Populate Safe Fields
>      ↓
> Validate
>      ↓
> Verify DOM/Form State
>      ↓
> Execution Report
>      ↓
> STOP
> ```
>
> ### Must implement
>
> * Sandbox form interaction provider
> * Text/email/phone/URL/textarea handling
> * Select handling
> * Radio handling
> * Checkbox handling
> * Required-field validation
> * Unsupported-field detection
> * Candidate profile → field mapping
> * Resume/cover-letter mapping where applicable
> * Before/after screenshots
> * Field-level execution results
> * Validation errors
> * Audit logging
> * Distributed execution lock
> * Idempotency protection
> * Browser cleanup
>
> ### Strict safety requirements
>
> * **Sandbox/mock execution only**
> * `submissionAttempted = false`
> * NEVER click Submit/Apply
> * NEVER send an application
> * NEVER send email
> * NEVER send LinkedIn messages
> * NEVER perform a real file upload
> * NEVER modify persisted candidate facts
> * NEVER enable `ALLOW_LIVE_SUBMISSION`
> * Preserve `AUTO_APPLY=false`
> * Preserve `AUTO_SEND_EMAIL=false`
> * Preserve `AUTO_LINKEDIN=false`
> * If a required field cannot be safely mapped, mark it `USER_REQUIRED` / `ACTION_REQUIRED`; do not invent a value.
> * Real-provider failures must not be converted into fake sandbox success.
>
> ### Expected result
>
> ```json
> {
>   "status": "READY_FOR_REVIEW",
>   "executionMode": "SANDBOX",
>   "fieldsDetected": 18,
>   "fieldsMapped": 16,
>   "fieldsRequireReview": 2,
>   "fieldsUnsupported": 0,
>   "submissionAttempted": false
> }
> ```
>
> ### Testing
>
> Run:
>
> ```bash
> ./gradlew test
> npm run build
> ```
>
> Add tests for:
>
> * text/email/phone fields
> * textarea
> * select/radio/checkbox
> * required-field validation
> * missing candidate facts
> * unsupported fields
> * selector failures
> * browser failures
> * concurrent execution
> * distributed lock failure
> * idempotency
> * submit-button safety
> * zero real submission
>
> Perform real browser verification against the sandbox/mock form.
>
> Confirm explicitly that:
>
> ```text
> Real application submitted = NO
> Email sent = NO
> LinkedIn message sent = NO
> File uploaded to real provider = NO
> submissionAttempted = false
> ```
>
> Commit and push to `main` only after all tests and browser verification pass.
>
> Commit message:
>
> ```text
> feat(phase6-m6-b): implement sandbox form interaction and verification
> ```
>
> **After M6-B is complete, STOP. Do not start Phase 7 or any live execution work. Provide the full walkthrough, test results, browser verification, commit hash, and safety confirmation.**

### Current status

```text
Phase 1                 ✅
Phase 2                 ✅
Phase 3                 ✅
Phase 4                 ✅
Phase 5 M1              ✅
Phase 5 M2              ✅
Phase 5 M3              ✅
Phase 5 M4-A             ✅
Phase 5 M4-B             ✅
Phase 5 M4-C             ✅
Phase 5 M4-D             ✅
Phase 5 M4-E             ✅
Production Audit         ✅
Phase 6 M6-A             ✅
Phase 6 M6-B             ▶️ NOW
Phase 7                  ⏳
```

**M6-B is the last piece we should build before deciding how to handle controlled real-world execution.**
