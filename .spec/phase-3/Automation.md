# AI Career OS — Phase 3

# Automation.md

## Autonomous Career Application Automation Specification

**Version:** 3.0
**Status:** Implementation Specification
**Depends On:** README.md, PRD.md, Architecture.md, Database.md, Events.md

---

# 1. Purpose

Phase 3 transforms AI Career OS from an AI-assisted career workspace into an **event-driven application automation platform**.

The goal is:

```text
Find Job
   ↓
Evaluate Job
   ↓
Decide Whether to Apply
   ↓
Prepare Application
   ↓
Prepare Resume
   ↓
Prepare Cover Letter
   ↓
Prepare Answers
   ↓
Find Recruiter
   ↓
Prepare Outreach
   ↓
Request Approval
   ↓
Execute Application
   ↓
Verify Submission
   ↓
Schedule Follow-Up
   ↓
Send Follow-Up
   ↓
Monitor Responses
   ↓
Detect Interview
   ↓
Track Outcome
```

The system should automate everything that can be safely automated.

When an external system requires human interaction or presents uncertainty, the system must stop and request human intervention.

---

# 2. Core Principle

The system is **autonomous but controlled**.

It must never interpret:

```text
automation = unlimited autonomous action
```

Instead:

```text
automation
=
rules
+
AI
+
workflow engine
+
approval policy
+
safety boundaries
+
audit trail
```

---

# 3. Automation Philosophy

AI Career OS should behave like a personal career operations agent.

It continuously:

```text
DISCOVER
ANALYZE
PRIORITIZE
PREPARE
APPROVE
EXECUTE
VERIFY
FOLLOW UP
MONITOR
LEARN
```

---

# 4. Automation Levels

The system must support configurable automation levels.

## LEVEL 0 — Manual

System only discovers and recommends.

```text
Job → Match → User decides everything
```

---

## LEVEL 1 — AI Assisted

System prepares:

* Resume
* Cover letter
* Application answers
* Recruiter information
* Cold email
* Follow-up

User approves each application.

---

## LEVEL 2 — Semi Autonomous

System automatically prepares applications and communication.

User approves before external submission.

```text
Job
 ↓
AI preparation
 ↓
Review
 ↓
USER APPROVAL
 ↓
Submit
```

---

## LEVEL 3 — Autonomous With Guardrails

System automatically applies to jobs that satisfy predefined rules.

Example:

```text
Match Score >= 85
ATS Score >= 80
Experience requirement satisfied
Location acceptable
Salary acceptable
No sensitive questions
No CAPTCHA
No MFA
Known provider
Application risk LOW
```

Then:

```text
AUTO SUBMIT
```

---

## LEVEL 4 — Fully Automated Career Agent

Future capability.

System can:

* discover
* qualify
* prepare
* apply
* contact recruiters
* follow up
* monitor replies
* detect interviews
* maintain application pipeline

while still respecting safety boundaries and configured limits.

---

# 5. Default Automation Level

Default:

```text
LEVEL 2
```

Never enable fully autonomous application submission automatically.

The user must explicitly enable higher automation levels.

---

# 6. Automation Configuration

Create an automation settings model.

Example:

```json
{
  "automationLevel": 2,
  "autoPrepareApplications": true,
  "autoGenerateResume": true,
  "autoGenerateCoverLetter": true,
  "autoGenerateAnswers": true,
  "autoFindRecruiters": true,
  "autoPrepareColdEmail": true,
  "autoSubmitApplications": false,
  "autoSendRecruiterEmails": false,
  "autoScheduleFollowups": true,
  "autoSendFollowups": false,
  "requireApprovalForUnknownQuestions": true,
  "requireApprovalForSensitiveQuestions": true,
  "requireApprovalForLowConfidenceAI": true
}
```

---

# 7. User Career Preferences

Automation depends on user-defined preferences.

Preferences may include:

```text
target roles
target locations
remote preference
minimum salary
maximum experience requirement
employment type
preferred industries
excluded companies
excluded job types
required skills
preferred skills
visa/work authorization
notice period
willingness to relocate
```

---

# 8. Job Eligibility Engine

Every discovered job must pass eligibility rules before entering the automation pipeline.

Pipeline:

```text
JOB DISCOVERED
      ↓
DEDUPLICATION
      ↓
ELIGIBILITY
      ↓
MATCHING
      ↓
SCORING
      ↓
AUTOMATION DECISION
```

---

# 9. Eligibility Rules

Evaluate:

```text
Role compatibility
Experience compatibility
Location
Work authorization
Employment type
Salary
Technology stack
Industry
Company exclusions
User preferences
Duplicate application history
```

---

# 10. Hard vs Soft Rules

Rules must be classified.

## Hard Rule

Failure means:

```text
DO NOT APPLY
```

Examples:

```text
Required work authorization unavailable
Job requires 8 years experience and user has 2
Company explicitly excluded
Already applied recently
Location impossible
```

---

## Soft Rule

Failure lowers the score.

Examples:

```text
Preferred skill missing
Industry not preferred
Salary not specified
Secondary technology missing
```

---

# 11. Job Automation Score

Create a composite score.

Example:

```text
Automation Score =
    Match Score
    + ATS Score
    + Preference Score
    + Recruiter Availability
    + Application Simplicity
    - Risk Score
```

Normalize to:

```text
0–100
```

---

# 12. Recommended Decision Bands

```text
90–100  → Excellent
80–89   → Strong
70–79   → Consider
60–69   → Weak
<60     → Reject
```

These values must be configurable.

---

# 13. Automatic Application Threshold

Default:

```text
>= 90
```

But automatic submission also requires all safety conditions to pass.

Therefore:

```text
Score >= threshold
AND
all hard rules passed
AND
application risk LOW
AND
provider supported
AND
no human intervention required
```

---

# 14. Automation Decision

Create an explicit decision object.

Example:

```json
{
  "decision": "AUTO_APPLY",
  "score": 94,
  "confidence": 0.96,
  "reasons": [
    "Strong skill match",
    "Experience requirement satisfied",
    "Preferred location",
    "ATS score above threshold"
  ],
  "blockedReasons": []
}
```

Possible decisions:

```text
AUTO_APPLY
PREPARE_FOR_REVIEW
REJECT
WAIT
ACTION_REQUIRED
```

---

# 15. Automation State Machine

Application automation state:

```text
DISCOVERED
   ↓
QUALIFYING
   ↓
QUALIFIED
   ↓
PREPARING
   ↓
READY
   ↓
WAITING_FOR_APPROVAL
   ↓
APPROVED
   ↓
EXECUTING
   ↓
VERIFYING
   ↓
SUBMITTED
   ↓
FOLLOWUP_SCHEDULED
   ↓
MONITORING
   ↓
OUTCOME
```

Failure paths:

```text
PREPARING → FAILED
EXECUTING → FAILED
VERIFYING → FAILED
ANY STATE → ACTION_REQUIRED
```

---

# 16. Automation Orchestrator

Implement a central orchestration component.

Suggested interface:

```java
public interface AutomationOrchestrator {

    void processJob(UUID jobId, UUID userId);

    void processApplication(UUID applicationId);

    void processApprovedApplication(UUID applicationId);

    void processFollowUp(UUID followUpId);

    void processInboundCommunication(UUID communicationId);
}
```

Actual package names should follow the existing project architecture.

---

# 17. Automation Engine

Suggested components:

```text
AutomationOrchestrator
AutomationDecisionEngine
AutomationPolicyService
ApplicationPreparationService
ApplicationApprovalService
ApplicationExecutionService
ApplicationVerificationService
FollowUpAutomationService
ResponseMonitoringService
AutomationAuditService
```

Keep responsibilities separated.

---

# 18. Event-Driven Automation

Automation must primarily react to events.

Example:

```text
job.matched
     ↓
AutomationOrchestrator
     ↓
Eligibility
     ↓
Decision
```

Then:

```text
application.created
     ↓
Preparation
```

Then:

```text
application.prepared
     ↓
Approval Policy
```

Then:

```text
application.approved
     ↓
Execution
```

---

# 19. Do Not Create a Giant Service

Do not implement all automation in one class such as:

```text
AutomationService.java
```

with thousands of lines.

Use small services.

Recommended:

```text
EligibilityService
DecisionService
PreparationService
ApprovalPolicyService
ExecutionService
VerificationService
FollowUpService
ResponseService
```

---

# 20. Application Preparation Pipeline

When an application qualifies:

```text
CREATE APPLICATION
        ↓
ANALYZE JOB
        ↓
TAILOR RESUME
        ↓
GENERATE COVER LETTER
        ↓
GENERATE APPLICATION ANSWERS
        ↓
DISCOVER RECRUITER
        ↓
GENERATE OUTREACH
        ↓
CALCULATE ATS SCORE
        ↓
CALCULATE CONFIDENCE
        ↓
MARK READY
```

---

# 21. Resume Automation

Use Phase 2 resume generation.

The automation layer should:

1. Retrieve canonical user profile.
2. Retrieve job.
3. Retrieve required skills.
4. Generate tailored resume.
5. Validate required facts.
6. Calculate ATS score.
7. Store version.
8. Upload document to MinIO.
9. Associate resume with application.

---

# 22. Resume Truth Rule

AI must not invent:

* Companies
* Job titles
* Degrees
* Certifications
* Years of experience
* Technologies
* Achievements
* Employment history

The generated resume must derive facts from the canonical profile.

---

# 23. Cover Letter Automation

Use Phase 2 cover letter generator.

Automatically create a cover letter when policy requires it.

Store:

```text
applicationId
coverLetterVersionId
generationModel
promptVersion
confidence
```

---

# 24. Application Question Automation

For known questions:

```text
AI answer
 ↓
Validate
 ↓
Confidence
```

For high-confidence factual questions:

```text
AUTO ANSWER
```

For uncertain questions:

```text
ACTION_REQUIRED
```

---

# 25. Question Classification

Classify questions into:

```text
PERSONAL_FACT
CAREER_FACT
SKILL
TECHNICAL
BEHAVIORAL
SALARY
WORK_AUTHORIZATION
DEMOGRAPHIC
LEGAL
SENSITIVE
UNKNOWN
```

---

# 26. Question Rules

## Personal Fact

Use profile data.

Example:

```text
Years of experience?
```

---

## Skill

Use verified skill profile.

---

## Technical

AI may generate an answer based on profile and known experience.

---

## Behavioral

AI may draft an answer.

Depending on automation level:

```text
REVIEW_REQUIRED
```

may be preferred.

---

## Sensitive

Always require user review by default.

---

## Unknown

Always require user review.

---

# 27. Confidence Thresholds

Default:

```text
>= 0.90
```

High confidence.

```text
0.70–0.89
```

Review recommended.

```text
< 0.70
```

Human intervention required.

These thresholds must be configurable.

---

# 28. Application Execution

The execution engine is responsible for interacting with supported application systems.

Potential providers:

```text
Greenhouse
Lever
Workday
Company career portals
Other supported systems
```

Provider implementations must use a common SPI.

Example:

```java
public interface ApplicationExecutionProvider {

    boolean supports(Job job);

    ExecutionResult execute(ApplicationContext context);

    ExecutionStatus getStatus(UUID executionId);
}
```

---

# 29. Provider Strategy

Do not hard-code provider-specific logic into the orchestrator.

Instead:

```text
ApplicationExecutionProvider
        │
        ├── GreenhouseProvider
        ├── LeverProvider
        ├── WorkdayProvider
        └── GenericProvider
```

The orchestrator only knows the interface.

---

# 30. Provider Capability Detection

Before execution:

```text
detect provider
      ↓
check support
      ↓
check capability
      ↓
execute
```

If unsupported:

```text
ACTION_REQUIRED
```

Do not attempt blind generic automation.

---

# 31. Application Risk Classification

Every application execution receives a risk level.

```text
LOW
MEDIUM
HIGH
BLOCKED
```

---

# 32. LOW Risk

Examples:

```text
Known provider
Known form structure
No sensitive questions
No authentication challenge
All fields mapped
High AI confidence
No CAPTCHA
```

May be automatically submitted depending on automation level.

---

# 33. MEDIUM Risk

Examples:

```text
Unknown optional question
Uncertain field mapping
Moderate AI confidence
New provider
```

Require approval by default.

---

# 34. HIGH Risk

Examples:

```text
Sensitive question
Legal declaration
Unknown authentication flow
Conflicting profile information
Unclear employment authorization question
```

Require human intervention.

---

# 35. BLOCKED

Examples:

```text
CAPTCHA
MFA
Unsupported provider
Credential failure
Security challenge
```

Execution must stop.

---

# 36. CAPTCHA Rule

If CAPTCHA is detected:

```text
STOP EXECUTION
      ↓
SAVE CURRENT STATE
      ↓
CREATE ACTION_REQUIRED
      ↓
NOTIFY USER
```

Never attempt to bypass CAPTCHA.

---

# 37. MFA Rule

If MFA is required:

```text
STOP
 ↓
ACTION_REQUIRED
 ↓
NOTIFY USER
```

Do not attempt to bypass MFA.

---

# 38. Authentication Rule

If the provider requires user login:

```text
if authenticated session exists:
    continue
else:
    ACTION_REQUIRED
```

Do not store raw passwords in application records.

---

# 39. Browser Session

Browser automation should use isolated sessions.

Conceptually:

```text
User
 ↓
Secure Browser Session
 ↓
Provider
```

Session credentials must never be written to logs.

---

# 40. Execution Idempotency

Before starting execution:

```text
Check application state
Check previous executions
Check provider application ID
Check execution lock
```

If already submitted:

```text
DO NOT SUBMIT AGAIN
```

---

# 41. Distributed Lock

Use Redis or database locking to prevent concurrent application execution.

Lock key:

```text
application:execution:{applicationId}
```

TTL must be configurable.

---

# 42. Application Execution Sequence

```text
application.approved
        ↓
Acquire Lock
        ↓
Validate State
        ↓
Select Provider
        ↓
Create Execution Record
        ↓
execution_started
        ↓
Open Provider
        ↓
Authenticate if session exists
        ↓
Map Fields
        ↓
Upload Resume
        ↓
Upload Cover Letter
        ↓
Answer Questions
        ↓
Validate Form
        ↓
Submit
        ↓
Capture Confirmation
        ↓
Persist Result
        ↓
Release Lock
```

---

# 43. Pre-Submission Validation

Before clicking submit:

Verify:

```text
Required fields complete
Resume attached
Cover letter attached if required
Answers present
No unresolved unknown fields
No blocked questions
User profile consistency
Application still active
Provider still supported
```

If validation fails:

```text
DO NOT SUBMIT
```

---

# 44. Final Submission Confirmation

Before submission, if configured:

```text
Review final payload
 ↓
Confirm no blocked state
 ↓
Submit
```

At higher automation levels this may happen automatically.

---

# 45. Submission Verification

Never consider an application successful merely because the submit button was clicked.

Verify using:

```text
confirmation page
provider application ID
confirmation message
email confirmation
provider status endpoint
```

---

# 46. Verification Outcomes

```text
VERIFIED
UNVERIFIED
FAILED
ACTION_REQUIRED
```

If submission occurred but confirmation is unclear:

```text
UNVERIFIED
```

Do not blindly submit again.

---

# 47. Duplicate Application Prevention

Before applying:

```text
Search previous applications
```

Match using:

```text
userId
jobId
company
provider
externalJobId
company job URL
```

If already applied:

```text
REJECT
```

unless user explicitly allows reapplication.

---

# 48. Application Cooldown

Support configurable cooldown:

```text
do not reapply to same job for X days
```

Default:

```text
30 days
```

---

# 49. Recruiter Automation

When a job qualifies:

```text
Find recruiter
      ↓
Validate recruiter
      ↓
Generate personalized message
      ↓
Calculate confidence
      ↓
Approval policy
      ↓
Send or queue
```

---

# 50. Recruiter Discovery Rules

Prioritize:

```text
Hiring Manager
Recruiter
Talent Acquisition
Technical Recruiter
Engineering Recruiter
```

Avoid:

```text
Generic company inbox
Unverified contact
Irrelevant employee
```

---

# 51. Recruiter Contact Confidence

Example:

```text
0.95
```

Known recruiter + verified company domain.

```text
0.60
```

Potential recruiter but uncertain role.

Low confidence:

```text
DO NOT AUTO SEND
```

---

# 52. Cold Email Policy

Default:

```text
GENERATE → REVIEW → SEND
```

Automatic sending should be opt-in.

---

# 53. Email Personalization

The AI may personalize using:

```text
job title
company
recruiter's role
relevant user skill
specific company/job context
```

Do not invent:

```text
personal relationship
previous conversation
company facts
recruiter preferences
```

---

# 54. Follow-Up Automation

After successful application:

```text
application.submitted
       ↓
Create follow-up schedule
```

Example default:

```text
Follow-up 1 → 5 business days
Follow-up 2 → 10 business days
Follow-up 3 → 20 business days
```

Configurable.

---

# 55. Follow-Up Stop Conditions

Cancel pending follow-ups if:

```text
Recruiter replied
Interview scheduled
Application rejected
Offer received
User cancels
Company asks not to contact
Communication bounce
```

---

# 56. Follow-Up Sending

Before every follow-up:

```text
Check application status
Check communication history
Check recruiter response
Check user automation settings
Check daily sending limit
```

Only send if eligible.

---

# 57. Inbound Communication Monitoring

Phase 3 should support inbound response detection.

Potential sources:

```text
Email
Provider notifications
Application portal
```

---

# 58. Email Classification

AI should classify inbound messages into:

```text
REJECTION
INTERVIEW_REQUEST
RECRUITER_INTEREST
REQUEST_FOR_INFORMATION
ASSESSMENT
OFFER
FOLLOW_UP
AUTO_REPLY
OTHER
```

---

# 59. Inbound AI Safety

AI classification should not automatically make irreversible decisions.

Example:

```text
Email looks like interview request
```

should create:

```text
interview.detected
```

but confirmation may remain:

```text
REQUIRES_REVIEW
```

depending on confidence.

---

# 60. Interview Detection

Use:

```text
keywords
AI classification
sender context
calendar information
email context
```

Confidence threshold configurable.

---

# 61. Interview Workflow

```text
Inbound Email
      ↓
Classify
      ↓
INTERVIEW_REQUEST
      ↓
Create Interview
      ↓
Notify User
      ↓
Update Application
      ↓
Cancel Follow-Ups
```

---

# 62. Offer Detection

If an email appears to contain an offer:

```text
offer.detected
```

Notify the user immediately.

Do not automatically accept or reject an offer.

---

# 63. Automation Scheduler

Some automation requires scheduled execution.

Use Spring scheduling or a dedicated scheduler abstraction.

Examples:

```text
job fetching
follow-up processing
retry processing
inbound polling
application verification
stale execution detection
```

---

# 64. Scheduler Rule

Schedulers should create events/commands rather than directly performing large business workflows.

Example:

```text
Scheduler
 ↓
followup.ready
 ↓
FollowUpWorker
```

---

# 65. Stale Execution Detection

If an execution remains:

```text
EXECUTING
```

for longer than configured timeout:

```text
mark stale
 ↓
attempt recovery
 ↓
ACTION_REQUIRED
```

Do not automatically submit again without checking whether submission already occurred.

---

# 66. Recovery Strategy

For execution failure:

```text
Check provider status
      ↓
Check confirmation
      ↓
Check execution record
      ↓
Determine whether retry is safe
```

Only retry if:

```text
retryable = true
AND
submission definitely did not occur
```

---

# 67. AI Retry

AI failures may be retried.

Example:

```text
Ollama timeout
JSON parsing failure
temporary provider failure
```

Use limited retries.

Never retry indefinitely.

---

# 68. AI Fallback

Existing Phase 2 fallback behavior must remain.

If Ollama unavailable:

```text
fallback strategy
```

may be used where safe.

For critical application answers:

```text
AI unavailable
 ↓
do not invent answer
 ↓
ACTION_REQUIRED
```

---

# 69. AI Prompt Versioning

Every generated artifact must record:

```text
model
promptVersion
generationTimestamp
```

Example:

```json
{
  "model": "llama3",
  "promptVersion": "resume-v3",
  "generatedAt": "2026-08-09T18:30:00Z"
}
```

---

# 70. Automation Audit Trail

Every automation decision must be auditable.

Record:

```text
applicationId
decision
score
confidence
policy
reasons
blockedReasons
automationLevel
timestamp
```

---

# 71. Example Audit Record

```json
{
  "applicationId": "APP-123",
  "decision": "PREPARE_FOR_REVIEW",
  "automationLevel": 2,
  "score": 86,
  "confidence": 0.91,
  "reasons": [
    "Strong skill match",
    "ATS score 89"
  ],
  "blockedReasons": [
    "Unknown application question"
  ]
}
```

---

# 72. Human Action Center

The system must create actionable tasks.

Examples:

```text
CAPTCHA encountered
MFA required
Application question needs answer
Low AI confidence
Provider unsupported
Resume missing
Authentication required
Application failed
```

Each task should include:

```text
title
description
applicationId
priority
createdAt
status
actionType
```

---

# 73. Action Priorities

```text
CRITICAL
HIGH
MEDIUM
LOW
```

Examples:

```text
CAPTCHA → HIGH
Interview detected → CRITICAL
Resume issue → HIGH
Low confidence question → MEDIUM
```

---

# 74. User Notification Strategy

Notify immediately for:

```text
Application requires intervention
Application submitted
Application failed
Recruiter replied
Interview detected
Offer detected
```

Do not spam for every internal event.

---

# 75. Automation Dashboard

Phase 3 dashboard should eventually show:

```text
Jobs discovered
Jobs qualified
Applications prepared
Applications awaiting approval
Applications submitted
Applications failed
Follow-ups scheduled
Recruiter responses
Interviews
Offers
Action required
```

---

# 76. Automation Queue Dashboard

Show:

```text
READY
PROCESSING
WAITING
ACTION_REQUIRED
FAILED
COMPLETED
```

---

# 77. Daily Automation Summary

Generate a daily summary.

Example:

```text
AI Career OS — Daily Summary

Jobs discovered: 47
Strong matches: 9
Applications prepared: 7
Applications submitted: 4
Awaiting approval: 3
Recruiter emails prepared: 6
Follow-ups sent: 2
Recruiter replies: 1
Interviews detected: 1
Actions required: 2
```

Send via Telegram and dashboard.

---

# 78. Automation Limits

Never allow unlimited automation.

Configurable:

```text
maxApplicationsPerDay
maxApplicationsPerWeek
maxEmailsPerDay
maxFollowupsPerDay
maxAIRequestsPerHour
maxConcurrentExecutions
```

---

# 79. Default Limits

Suggested development defaults:

```text
maxApplicationsPerDay = 5
maxEmailsPerDay = 10
maxFollowupsPerDay = 5
maxConcurrentExecutions = 1
```

Production values must be configurable.

---

# 80. Rate Limit Handling

If a provider responds with rate limiting:

```text
HTTP 429
```

then:

```text
pause provider
schedule retry
record rate-limit event
```

Do not continuously retry.

---

# 81. Company-Level Limits

Prevent excessive applications to the same company.

Example:

```text
maximum applications/company/day
```

This prevents automation from behaving aggressively.

---

# 82. Communication Quality Guardrail

Before sending recruiter communication:

Validate:

```text
No spam-like repetition
No false claims
No invented experience
No misleading statements
No irrelevant personalization
```

---

# 83. Application Quality Guardrail

Before submission:

```text
Resume truth validated
Application answers validated
Required fields complete
No unsupported claims
No missing mandatory information
```

---

# 84. AI Hallucination Guardrail

Every AI-generated factual claim must be traceable to:

```text
user profile
resume
verified skill
job description
approved company information
```

If unsupported:

```text
remove claim
or
ACTION_REQUIRED
```

---

# 85. Autonomous Decision Example

Input:

```text
Job:
Senior Java Developer

Match:
94

ATS:
92

Experience:
Satisfied

Location:
Satisfied

Required skills:
90% satisfied

Provider:
Known Greenhouse

Questions:
All known

Risk:
LOW

Automation Level:
3
```

Decision:

```text
AUTO_APPLY
```

---

# 86. Manual Review Example

Input:

```text
Match:
91

ATS:
89

Provider:
Known

Question:
"Are you legally authorized to work in..."
```

If profile does not contain verified answer:

```text
ACTION_REQUIRED
```

No submission.

---

# 87. CAPTCHA Example

```text
Application approved
      ↓
Execution started
      ↓
CAPTCHA detected
      ↓
STOP
      ↓
application.submission_requires_review
      ↓
Telegram notification
```

No bypass.

---

# 88. Duplicate Application Example

```text
Job discovered
      ↓
Match = 95
      ↓
Search application history
      ↓
Already submitted 12 days ago
      ↓
REJECT
```

No second submission.

---

# 89. Recruiter Reply Example

```text
Recruiter email received
      ↓
AI classification
      ↓
RECRUITER_INTEREST
      ↓
Update application
      ↓
Cancel pending follow-ups
      ↓
Notify user
```

---

# 90. Interview Example

```text
Recruiter email
      ↓
AI detects interview request
      ↓
Confidence = 96%
      ↓
Create interview
      ↓
Cancel follow-ups
      ↓
Notify user
```

---

# 91. End-to-End Autonomous Workflow

Complete workflow:

```text
                JOB DISCOVERY
                     │
                     ▼
                JOB MATCHED
                     │
                     ▼
              ELIGIBILITY CHECK
                     │
          ┌──────────┴──────────┐
          │                     │
        REJECT               QUALIFIED
                                │
                                ▼
                       AUTOMATION DECISION
                                │
                ┌───────────────┼───────────────┐
                │               │               │
             REJECT          PREPARE         AUTO APPLY
                                │               │
                                ▼               ▼
                           AI PREPARATION   PREPARATION
                                │               │
                                └───────┬───────┘
                                        ▼
                                   VALIDATION
                                        │
                               ┌────────┴────────┐
                               │                 │
                            REVIEW            READY
                               │                 │
                               ▼                 ▼
                            APPROVE          EXECUTE
                               │                 │
                               └────────┬────────┘
                                        ▼
                                    VERIFY
                                        │
                              ┌─────────┴─────────┐
                              │                   │
                           FAILED              SUBMITTED
                                                  │
                                                  ▼
                                           FOLLOW-UP PLAN
                                                  │
                                                  ▼
                                           COMMUNICATION
                                                  │
                                                  ▼
                                          RESPONSE MONITOR
                                                  │
                                      ┌───────────┼───────────┐
                                      │           │           │
                                   NO REPLY    INTERVIEW     OFFER
                                      │           │           │
                                      ▼           ▼           ▼
                                  FOLLOW-UP    USER ALERT   USER ALERT
```

---

# 92. Automation Engine Rules

The engine must always evaluate:

```text
1. Is the job eligible?
2. Is the user allowed to apply?
3. Has the user already applied?
4. Is the application within automation limits?
5. Is the provider supported?
6. Is the application data complete?
7. Are AI outputs trustworthy?
8. Is human approval required?
9. Is execution safe?
10. Was submission verified?
11. Should follow-up be scheduled?
12. Should follow-up be cancelled?
```

---

# 93. State Transition Guard

Every state transition must be validated.

Do not allow:

```text
DRAFT → SUBMITTED
```

without preparation and execution.

Do not allow:

```text
SUBMITTED → APPROVED
```

without a valid workflow reason.

Implement transition validation.

---

# 94. Suggested State Transition Service

```java
public interface ApplicationStateMachine {

    boolean canTransition(
        ApplicationStatus current,
        ApplicationStatus target
    );

    void transition(
        UUID applicationId,
        ApplicationStatus target,
        String reason
    );
}
```

---

# 95. No Silent State Changes

Every state change must record:

```text
oldState
newState
reason
actor
timestamp
correlationId
```

Actor can be:

```text
USER
AI
SYSTEM
WORKER
PROVIDER
```

---

# 96. Automation Actor

For automated actions:

```text
actorType = SYSTEM
```

For AI decisions:

```text
actorType = AI
```

For user approval:

```text
actorType = USER
```

---

# 97. Human Override

User must be able to override automation.

Examples:

```text
Skip application
Approve application
Reject application
Pause automation
Resume automation
Cancel follow-up
Edit generated answer
Regenerate resume
Regenerate cover letter
```

---

# 98. Global Automation Pause

User must be able to:

```text
PAUSE ALL AUTOMATION
```

When paused:

```text
No new external actions
```

Internal processing may finish depending on policy.

External submissions must not begin.

---

# 99. Per-Application Pause

User may pause one application.

```text
application.paused = true
```

Automation must respect it.

---

# 100. Kill Switch

Implement a global emergency stop.

Example:

```text
AUTOMATION_KILL_SWITCH=true
```

When enabled:

```text
No external application execution
No outbound communication
No follow-up sending
```

The system may continue:

```text
job discovery
AI preparation
analytics
```

depending on configuration.

---

# 101. Production Safety Requirement

Before enabling autonomous application submission in production:

Run:

```text
simulation mode
```

for a meaningful period.

---

# 102. Simulation Mode

In simulation mode:

```text
Everything runs
except actual external submission/send.
```

Instead:

```text
LOG WOULD_APPLY
LOG WOULD_SEND
LOG WOULD_FOLLOWUP
```

---

# 103. Dry Run Example

```text
Application:
Java Developer @ Example Corp

Decision:
AUTO_APPLY

Result:
DRY_RUN

Would have submitted:
YES

Would have sent recruiter email:
YES

Would have scheduled follow-up:
YES
```

---

# 104. Production Activation

Autonomous mode should require explicit user action.

Example:

```text
Enable Autonomous Applications
```

Then show:

```text
Daily limit
Email limit
Approval policy
Allowed providers
Excluded companies
Automation level
```

User confirms.

---

# 105. Audit Requirement

Every autonomous external action must be traceable.

For example:

```text
Why was this application submitted?

Answer:

Automation Level: 3
Match Score: 94
ATS Score: 92
Decision: AUTO_APPLY
Policy: DEFAULT_AUTO_APPLY_V1
Provider: GREENHOUSE
Risk: LOW
Approval Required: NO
User: <id>
Timestamp: <timestamp>
Correlation ID: <id>
```

---

# 106. Testing Requirements

Implement unit tests for:

```text
Eligibility rules
Hard rules
Soft rules
Automation score
Automation levels
Approval policy
Risk classification
Question classification
State transitions
Duplicate prevention
Rate limiting
Follow-up eligibility
Kill switch
Pause behavior
```

---

# 107. Integration Tests

At minimum:

```text
Job → Application
Application → Preparation
Preparation → Approval
Approval → Execution
Execution → Verification
Submission → Follow-up
Inbound Email → Interview
```

---

# 108. Failure Tests

Test:

```text
Ollama unavailable
RabbitMQ unavailable
MySQL temporary failure
Provider timeout
Provider 500
Provider 429
CAPTCHA
MFA
Unknown question
Duplicate event
Duplicate execution
Duplicate communication
```

---

# 109. Security Tests

Verify:

```text
No secrets in events
No secrets in logs
No credentials in database plaintext
Unauthorized user cannot execute another user's application
User cannot approve another user's application
Global kill switch works
Automation settings are user-scoped
```

---

# 110. Performance Requirements

Target:

```text
Job qualification:
< 2 seconds excluding external AI/provider latency

Automation decision:
< 1 second

Event processing:
< 2 seconds average

Dashboard:
< 2 seconds for normal queries
```

These are targets, not absolute guarantees.

---

# 111. Scalability

Architecture must support:

```text
1 user
→ 10 users
→ 100 users
→ 1,000 users
```

without rewriting the workflow engine.

Workers should be horizontally scalable.

---

# 112. Worker Concurrency

Application execution must default to:

```text
1 concurrent execution per user
```

and:

```text
configurable global concurrency
```

This prevents uncontrolled provider load.

---

# 113. Observability

Expose metrics:

```text
automation_jobs_processed
automation_applications_prepared
automation_applications_submitted
automation_applications_failed
automation_action_required
automation_followups_sent
automation_interviews_detected
automation_decision_latency
```

---

# 114. Logging

Every automation step should log structured metadata.

Example:

```text
applicationId
jobId
userId
eventId
correlationId
automationLevel
decision
provider
state
```

Never log sensitive form answers or credentials.

---

# 115. Production Readiness Checklist

Before production autonomous mode:

```text
[ ] State machine validated
[ ] Duplicate prevention tested
[ ] Idempotency tested
[ ] Kill switch tested
[ ] Global pause tested
[ ] Per-application pause tested
[ ] Rate limits enabled
[ ] Provider support verified
[ ] CAPTCHA stop verified
[ ] MFA stop verified
[ ] Unknown question stop verified
[ ] Submission verification implemented
[ ] Outbox enabled
[ ] DLQ enabled
[ ] Monitoring enabled
[ ] Telegram alerts enabled
[ ] Audit logs enabled
[ ] Dry-run mode tested
```

---

# 116. Non-Goals

Phase 3 must NOT attempt to:

* bypass CAPTCHA
* bypass MFA
* bypass anti-bot mechanisms
* bypass authentication
* falsify applicant information
* fabricate experience
* impersonate a recruiter
* send unlimited emails
* spam recruiters
* automatically accept offers
* automatically reject offers
* make irreversible career decisions without configured authorization

---

# 117. Success Criteria

Phase 3 automation is successful when the system can:

```text
Automatically discover eligible jobs
        ↓
Automatically evaluate them
        ↓
Automatically prepare applications
        ↓
Automatically generate required artifacts
        ↓
Apply configured approval policies
        ↓
Automatically execute safe applications
        ↓
Verify submissions
        ↓
Schedule follow-ups
        ↓
Cancel follow-ups when appropriate
        ↓
Detect recruiter responses
        ↓
Detect interviews
        ↓
Notify the user
```

while maintaining:

```text
safety
accuracy
auditability
idempotency
user control
```

---

# 118. Golden Rule

The system should maximize automation **without maximizing risk**.

The objective is NOT:

```text
Apply to 1,000 jobs
```

The objective is:

```text
Find the best opportunities
+
prepare high-quality applications
+
submit safely
+
reach the right recruiters
+
follow up intelligently
+
maximize interview probability
```

Quality and relevance are more important than raw application volume.

---

# 119. Final Phase 3 Automation Architecture

```text
                         ┌──────────────────────┐
                         │      JOB SOURCES     │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │   JOB INGESTION      │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │ ELIGIBILITY ENGINE   │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │ MATCH / AI SCORING   │
                         └──────────┬───────────┘
                                    │
                                    ▼
                         ┌──────────────────────┐
                         │ AUTOMATION DECISION  │
                         └──────────┬───────────┘
                                    │
                   ┌────────────────┼────────────────┐
                   │                │                │
                   ▼                ▼                ▼
                REJECT          PREPARE          AUTO APPLY
                                    │                │
                                    ▼                │
                         ┌──────────────────────┐   │
                         │ AI APPLICATION PREP  │   │
                         │ Resume               │   │
                         │ Cover Letter         │   │
                         │ Questions            │   │
                         │ Recruiter            │   │
                         └──────────┬───────────┘   │
                                    │                │
                                    └───────┬────────┘
                                            ▼
                                  ┌──────────────────┐
                                  │ SAFETY / POLICY  │
                                  │ ENGINE           │
                                  └────────┬─────────┘
                                           │
                              ┌────────────┴────────────┐
                              │                         │
                              ▼                         ▼
                         HUMAN REVIEW              AUTO EXECUTE
                              │                         │
                              └────────────┬────────────┘
                                           ▼
                                  ┌──────────────────┐
                                  │ APPLICATION      │
                                  │ EXECUTION        │
                                  └────────┬─────────┘
                                           │
                                           ▼
                                  ┌──────────────────┐
                                  │ VERIFICATION     │
                                  └────────┬─────────┘
                                           │
                                           ▼
                                  ┌──────────────────┐
                                  │ FOLLOW-UP ENGINE │
                                  └────────┬─────────┘
                                           │
                                           ▼
                                  ┌──────────────────┐
                                  │ COMMUNICATION    │
                                  └────────┬─────────┘
                                           │
                                           ▼
                                  ┌──────────────────┐
                                  │ RESPONSE MONITOR │
                                  └────────┬─────────┘
                                           │
                           ┌───────────────┼───────────────┐
                           │               │               │
                           ▼               ▼               ▼
                        REPLY          INTERVIEW         OFFER
                           │               │               │
                           └───────────────┼───────────────┘
                                           ▼
                                  ┌──────────────────┐
                                  │ USER + ANALYTICS │
                                  └──────────────────┘
```

---

# 120. Definition of Done

Phase 3 Automation is considered complete only when:

* [ ] Automation levels implemented
* [ ] User automation settings implemented
* [ ] Job eligibility engine implemented
* [ ] Hard/soft rules implemented
* [ ] Automation scoring implemented
* [ ] Automation decision engine implemented
* [ ] Application state machine implemented
* [ ] Application preparation orchestration implemented
* [ ] Resume automation integrated
* [ ] Cover letter automation integrated
* [ ] Application question classification implemented
* [ ] AI confidence thresholds implemented
* [ ] Application execution SPI implemented
* [ ] Provider capability detection implemented
* [ ] Execution idempotency implemented
* [ ] Distributed execution lock implemented
* [ ] Pre-submission validation implemented
* [ ] Submission verification implemented
* [ ] Duplicate application prevention implemented
* [ ] Recruiter automation integrated
* [ ] Follow-up automation implemented
* [ ] Follow-up cancellation rules implemented
* [ ] Inbound communication classification implemented
* [ ] Interview detection implemented
* [ ] Offer detection implemented
* [ ] Rate limiting implemented
* [ ] Daily automation limits implemented
* [ ] Global automation pause implemented
* [ ] Per-application pause implemented
* [ ] Kill switch implemented
* [ ] Dry-run mode implemented
* [ ] Human action center implemented
* [ ] Automation audit trail implemented
* [ ] Structured logging implemented
* [ ] Metrics implemented
* [ ] Unit tests implemented
* [ ] Integration tests implemented
* [ ] Failure tests implemented
* [ ] Security tests implemented
* [ ] Existing Phase 1 and Phase 2 functionality remains compatible

---

# END OF AUTOMATION SPECIFICATION
