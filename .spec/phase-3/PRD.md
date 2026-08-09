
# AI Career OS — Phase 3

# Product Requirements Document

## Application Automation Engine

Version: 3.0

Status: Specification

---

# 1. Document Purpose

This document defines the product requirements for Phase 3 of AI Career OS.

Phase 3 transforms the existing AI-powered career preparation platform into an application execution and tracking platform.

Phase 1 established the foundation.

Phase 2 established AI-powered job intelligence and application preparation.

Phase 3 introduces controlled automation.

The primary objective is:

> Discover relevant jobs, prepare high-quality applications, obtain required approval, execute approved actions, communicate with recruiters, track every outcome, and continuously improve the job-search strategy.

---

# 2. Existing System

Phase 1 is complete.

Existing capabilities include:

- User registration
- JWT authentication
- Candidate profile
- Skills
- Resume upload
- Job discovery
- Job ingestion
- Job deduplication
- Job matching
- RabbitMQ messaging
- Redis
- MySQL
- MinIO
- Ollama
- Telegram notifications

Phase 2 is complete.

Existing capabilities include:

- AI Orchestrator
- Job analysis
- Resume generation
- ATS analysis
- Resume versioning
- PDF export
- DOCX export
- Cover letter generation
- Cover letter versioning
- Recruiter intelligence
- Recruiter discovery provider SPI
- Cold email generation
- Follow-up message generation
- LinkedIn message generation
- AI Workspace
- Workspace approval/rejection

Phase 3 must reuse these capabilities instead of rebuilding them.

---

# 3. Product Vision

AI Career OS should become an intelligent career agent that continuously helps the candidate manage their job search.

The long-term experience should be:

Candidate configures career preferences once.

↓

AI Career OS continuously discovers jobs.

↓

AI evaluates opportunities.

↓

AI prepares applications.

↓

User approves according to automation settings.

↓

System executes approved actions.

↓

System tracks responses.

↓

System schedules follow-ups.

↓

System records interviews and outcomes.

↓

AI learns from historical outcomes.

↓

Future recommendations improve.

---

# 4. Phase 3 Objective

The objective of Phase 3 is to build the complete application lifecycle.

The system must be able to move an opportunity from:

```text
JOB DISCOVERED
````

to:

```text
APPLICATION
```

and then through:

```text
APPROVAL
→ SUBMISSION
→ FOLLOW-UP
→ RESPONSE
→ INTERVIEW
→ OUTCOME
```

while recording every important event.

---

# 5. Primary User

The primary user is an individual job seeker.

The user may configure:

* Target roles
* Target companies
* Locations
* Remote/hybrid/on-site preferences
* Minimum salary
* Experience range
* Skills
* Automation level
* Daily application limits
* Outreach limits
* Follow-up rules
* Approval requirements

---

# 6. Core User Journey

The primary workflow is:

```text
Job discovered
       ↓
Job matched
       ↓
Job analyzed
       ↓
Application opportunity created
       ↓
AI Workspace generated
       ↓
Application prepared
       ↓
User review
       ↓
Approval
       ↓
Application execution
       ↓
Submission verification
       ↓
Application marked submitted
       ↓
Recruiter outreach
       ↓
Follow-up scheduled
       ↓
Response detected/recorded
       ↓
Interview
       ↓
Outcome
```

---

# 7. Application Domain

The Application is the central entity introduced in Phase 3.

An Application represents a candidate's attempt to apply for a specific job.

An application must contain references to:

* User
* Job
* Company
* Resume version
* Cover letter version
* Recruiter
* Application provider
* Application URL
* Submission information
* Current status
* Approval status
* Automation configuration
* Timestamps
* Outcome

---

# 8. Application Lifecycle

The application lifecycle is:

```text
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
    ↓
FOLLOW_UP_PENDING
    ↓
FOLLOWED_UP
    ↓
RESPONDED
    ↓
INTERVIEW
    ↓
OFFER
```

Terminal states:

```text
REJECTED
WITHDRAWN
NO_RESPONSE
CLOSED
```

The exact transition rules are defined in StateMachine.md.

---

# 9. Application Creation

Applications may be created when:

1. A job meets the candidate's match criteria.
2. A user manually selects a job.
3. An AI recommendation reaches the configured threshold.
4. A scheduled automation workflow selects a qualified job.

The system must prevent duplicate active applications for the same user and job unless explicitly permitted.

---

# 10. Application Preparation

When an application is created, the system should assemble:

* Job information
* Candidate profile
* Job analysis
* Match score
* Missing skills
* Recommended resume
* Resume version
* ATS analysis
* Cover letter
* Recruiter
* Outreach message
* Application questions
* Recommendation
* Confidence score

The result is an Application Workspace.

---

# 11. Application Readiness

An application is considered ready only when required artifacts exist.

Minimum readiness requirements:

* Valid candidate profile
* Job still active/valid
* Resume available
* Resume compatible with job
* Application URL available
* No critical missing candidate information
* Recommendation allows application
* Required approval state satisfied

Optional:

* Cover letter
* Recruiter
* Recruiter email
* LinkedIn message

Optional artifacts must not block an application unless configured by the user.

---

# 12. Human Approval

Human approval is a core requirement.

The default workflow is:

```text
AI Preparation
      ↓
READY_FOR_REVIEW
      ↓
User Review
      ↓
APPROVE
      ↓
Execution
```

The user must be able to:

* Review resume
* Review cover letter
* Review recruiter message
* Review application answers
* Edit content
* Approve
* Reject
* Regenerate
* Cancel

---

# 13. Automation Levels

## LEVEL_0 — Manual

AI prepares recommendations.

No automatic execution.

---

## LEVEL_1 — Approval Required

AI prepares everything.

User approves each application.

---

## LEVEL_2 — Trusted Automation

User defines trusted rules.

Matching applications may proceed automatically if all rules pass.

---

## LEVEL_3 — Advanced Automation

The system may execute predefined workflows automatically.

The user must explicitly enable this mode.

The system must still respect:

* Application limits
* User restrictions
* Required information
* Safety rules
* Provider restrictions

---

# 14. Application Providers

The application system must use a provider abstraction.

Conceptually:

```text
ApplicationProvider
```

Possible implementations:

```text
DirectApplicationProvider
BrowserApplicationProvider
ApiApplicationProvider
ManualApplicationProvider
```

The domain must not depend directly on a specific job platform.

---

# 15. Application Provider Contract

A provider should support:

* Capability discovery
* Application preparation
* Application execution
* Submission verification
* Error reporting

Conceptual interface:

```text
ApplicationProvider

getCapabilities()

prepareApplication()

submitApplication()

verifySubmission()
```

The actual Java interface is defined by Architecture.md.

---

# 16. Browser Automation

Browser automation is an execution mechanism, not the application domain.

The browser engine must:

* Open the application URL
* Inspect the page
* Identify form fields
* Map known candidate data
* Map application questions
* Upload documents
* Validate required fields
* Prepare submission
* Submit only when authorized
* Verify result

The browser engine must never bypass:

* CAPTCHA
* MFA
* Authentication controls
* Security mechanisms
* Access restrictions

If a human interaction is required, the workflow must pause and request user action.

---

# 17. Application Question Engine

Applications frequently contain questions.

Examples:

* Years of experience
* Work authorization
* Sponsorship requirement
* Location
* Salary expectations
* Relocation
* Notice period
* Experience with specific technology

The system must maintain an Answer Bank.

Workflow:

```text
Question detected
       ↓
Normalize question
       ↓
Search Answer Bank
       ↓
Known answer?
   ┌───┴───┐
  YES     NO
   ↓       ↓
Reuse     AI draft
           ↓
        User review
           ↓
        Save answer
```

The system must never invent factual candidate information.

---

# 18. Candidate Answer Bank

Answers may be:

* User-defined
* Previously approved
* AI-generated and approved
* Derived from verified profile data

Each answer should maintain:

* Question pattern
* Answer
* Source
* Approval state
* Last updated
* Confidence

---

# 19. Resume Selection

For each application the system should select the most appropriate resume version.

Selection factors may include:

* Job title
* Required skills
* Experience
* ATS score
* Resume performance
* User preferences

The system must retain the exact resume version used for submission.

---

# 20. Cover Letter Selection

The application may use:

* Existing approved cover letter
* Newly generated cover letter
* No cover letter

The exact version used must be recorded.

---

# 21. Recruiter Outreach

After application submission, the system may prepare recruiter outreach.

Possible channels:

* Email
* LinkedIn message draft
* Other configured communication providers

Initial Phase 3 implementation should prioritize email.

LinkedIn automation should initially remain draft/manual unless a compliant integration is available.

---

# 22. Email Automation

Email workflow:

```text
Application Submitted
        ↓
Recruiter Available?
        ↓
Generate Outreach
        ↓
User Approval
        ↓
Send
        ↓
Record Delivery
        ↓
Schedule Follow-up
```

The system must maintain a complete communication history.

---

# 23. Follow-Up Automation

The user can configure:

* Follow-up delay
* Maximum follow-ups
* Business-day behavior
* Working hours
* Timezone
* Stop conditions

Example:

```text
Application submitted
        ↓
Wait 3 business days
        ↓
No response?
        ↓
Follow-up #1
        ↓
Wait 5 business days
        ↓
No response?
        ↓
Follow-up #2
        ↓
Stop
```

Follow-ups must stop when:

* Recruiter responds
* Application is rejected
* User cancels
* Job closes
* Maximum follow-ups reached

---

# 24. Application Events

Every important action must create an event.

Examples:

```text
APPLICATION_CREATED
APPLICATION_PREPARED
APPLICATION_APPROVAL_REQUESTED
APPLICATION_APPROVED
APPLICATION_REJECTED
APPLICATION_STARTED
APPLICATION_SUBMITTED
APPLICATION_VERIFIED
APPLICATION_FAILED
RECRUITER_FOUND
EMAIL_GENERATED
EMAIL_APPROVED
EMAIL_SENT
FOLLOWUP_SCHEDULED
FOLLOWUP_SENT
RESPONSE_RECORDED
INTERVIEW_SCHEDULED
OFFER_RECEIVED
APPLICATION_CLOSED
```

---

# 25. Notifications

The user must receive notifications for important events.

Examples:

```text
New high-match job

Application ready

Approval required

Application submitted

Application failed

Recruiter discovered

Recruiter email sent

Follow-up due

Recruiter response

Interview detected

Offer recorded
```

Telegram should continue as the initial notification channel.

---

# 26. Application Dashboard

The dashboard must provide a unified view.

Required metrics:

* Total opportunities
* Qualified jobs
* Applications
* Applications pending approval
* Applications submitted
* Interviews
* Offers
* Rejections
* Follow-ups pending
* Response rate
* Interview rate

---

# 27. Application Pipeline

The UI should represent the lifecycle visually.

Example:

```text
Discovered
   ↓
Qualified
   ↓
Preparing
   ↓
Review
   ↓
Approved
   ↓
Applied
   ↓
Follow-up
   ↓
Response
   ↓
Interview
   ↓
Offer
```

Users must be able to inspect individual applications.

---

# 28. Application Detail

Application detail should show:

## Job

* Title
* Company
* Location
* URL
* Source

## AI Analysis

* Match score
* ATS score
* Recommendation
* Missing skills
* Confidence

## Documents

* Resume version
* Cover letter version

## Recruiter

* Name
* Title
* Email
* LinkedIn

## Communication

* Emails
* Follow-ups
* Responses

## Automation

* Current automation level
* Current action
* Next action
* Scheduled time

## History

Complete chronological timeline.

---

# 29. Application Timeline

Example:

```text
09:00
Job discovered

09:02
AI analysis completed

09:04
Resume generated

09:05
Cover letter generated

09:06
Application ready for review

09:10
User approved

09:11
Application started

09:13
Application submitted

09:14
Submission verified

09:15
Recruiter email generated

09:17
Email approved

09:18
Email sent

09:18
Follow-up scheduled
```

---

# 30. Failure Handling

Automation failures must never silently disappear.

Possible failures:

* Application page unavailable
* Form structure changed
* Required question unknown
* Resume upload failed
* Submission failed
* Provider unavailable
* Browser crash
* Network error
* Timeout
* Verification failed

The system must:

1. Record the failure.
2. Stop unsafe execution.
3. Retry when appropriate.
4. Notify the user.
5. Preserve the application state.
6. Allow manual continuation.

---

# 31. Retry Policy

Transient failures may be retried.

Examples:

* Network timeout
* Temporary provider failure
* Browser startup failure

Non-retryable failures include:

* Invalid candidate data
* Missing required information
* CAPTCHA
* Authentication requirement
* User cancellation

Maximum automatic retries must be configurable.

---

# 32. Duplicate Protection

The system must prevent:

* Duplicate applications
* Duplicate emails
* Duplicate follow-ups
* Duplicate submissions

Idempotency keys must be used for critical execution operations.

---

# 33. Rate Limits

The system must enforce:

* Daily application limit
* Hourly application limit
* Daily recruiter outreach limit
* Follow-up limit
* Provider-specific limits

Defaults must be configurable.

---

# 34. User Controls

The user must be able to configure:

* Automation level
* Application limit
* Target job score
* Target locations
* Target companies
* Salary expectations
* Recruiter outreach enabled/disabled
* Follow-ups enabled/disabled
* Maximum follow-ups
* Approval requirement
* Notification preferences

---

# 35. Analytics

Phase 3 must collect outcome data.

Metrics include:

```text
Jobs discovered
Jobs qualified
Applications created
Applications submitted
Applications rejected
Applications withdrawn
Recruiter contacts
Recruiter responses
Interviews
Offers
```

Derived metrics:

```text
Qualification rate
Application rate
Response rate
Interview rate
Offer rate
```

---

# 36. Performance Analytics

The system should eventually compare:

* Resume versions
* Job sources
* Companies
* Job titles
* Skills
* Recruiter outreach
* Email styles
* Cover letters

Example:

```text
Resume A

Applications: 30
Interviews: 2
Interview rate: 6.7%

Resume B

Applications: 25
Interviews: 5
Interview rate: 20%
```

This data becomes input to future AI optimization.

---

# 37. AI Optimization

Phase 3 should collect the data required for future optimization.

The AI may eventually learn:

* Which jobs are worth applying to
* Which resume performs better
* Which skills correlate with interviews
* Which outreach messages receive responses
* Which companies respond
* Which job sources produce better outcomes

AI must not fabricate conclusions when sample sizes are insufficient.

---

# 38. Auditability

Every automated action must be auditable.

Record:

* User
* Application
* Action
* Provider
* Timestamp
* Request ID
* Execution ID
* Result
* Error
* Approval
* Source artifact

---

# 39. Security Requirements

The system must:

* Use JWT authentication.
* Enforce authorization.
* Validate all input.
* Protect sensitive candidate information.
* Encrypt sensitive secrets.
* Never log passwords.
* Never log authentication tokens.
* Never store credentials in plaintext.
* Sanitize external content.
* Protect against prompt injection.
* Protect against malicious job descriptions.
* Protect against malicious application form content.

---

# 40. Privacy

Candidate information must only be used for authorized career workflows.

Sensitive information must not be sent to external providers unless required and explicitly configured.

The system must minimize external data transmission.

---

# 41. AI Safety

AI may:

* Analyze jobs
* Generate resumes
* Generate cover letters
* Generate answers
* Generate outreach
* Recommend actions

AI may not:

* Invent qualifications
* Invent experience
* Invent education
* Invent certifications
* Change factual candidate data without approval
* Claim an application was submitted without verification

---

# 42. Observability

Every automated workflow must be observable.

Metrics:

* Execution count
* Success rate
* Failure rate
* Retry count
* Average execution time
* Provider failure rate
* Application success rate

Logs must contain:

* Correlation ID
* Application ID
* Execution ID
* Event ID

Sensitive information must be excluded.

---

# 43. Notification Strategy

Initial notification channel:

Telegram.

Future channels:

* Email
* Web notifications
* Push notifications

Notification delivery must be asynchronous.

---

# 44. Database Requirements

Phase 3 will introduce database entities for at least:

* Application
* ApplicationEvent
* ApplicationAction
* ApplicationApproval
* ApplicationQuestion
* CandidateAnswer
* ApplicationOutcome
* AutomationRule
* ScheduledAction
* Communication
* CommunicationEvent

Exact schema is defined in Database.md.

---

# 45. RabbitMQ Requirements

Long-running workflows must use RabbitMQ.

Examples:

```text
application.created
application.preparation
application.approved
application.execution
application.submitted
application.failed
communication.send
followup.schedule
followup.execute
```

Exact event contracts are defined in Events.md.

---

# 46. Redis Requirements

Redis may be used for:

* Distributed locks
* Job scheduling state
* Browser session state
* Rate limiting
* Temporary workflow state
* Idempotency keys

Redis must not be treated as the source of truth for application history.

MySQL remains the source of truth.

---

# 47. MinIO Requirements

MinIO stores application artifacts.

Examples:

* Submitted resume
* Cover letter
* Generated documents
* Screenshots where permitted
* Submission evidence

Artifacts must be associated with application IDs.

---

# 48. Manual Recovery

Every failed automation workflow must provide a manual recovery path.

Example:

```text
Automation failed

[Retry]

[Open Application URL]

[Mark Applied Manually]

[Cancel]
```

The user must never become permanently blocked by an automation failure.

---

# 49. No-Guarantee Principle

AI Career OS cannot guarantee:

* Job offers
* Interviews
* Recruiter responses
* Employment

The system objective is measurable improvement in job-search efficiency and outcomes.

Success must be measured using actual results.

---

# 50. Phase 3 Non-Goals

The following are explicitly outside the core Phase 3 scope:

* Guaranteed interviews
* Guaranteed job offers
* CAPTCHA bypass
* MFA bypass
* Security bypass
* Credential harvesting
* Spam campaigns
* Unlimited automated applications
* Fabricated candidate information
* Fake recruiter identities
* Automatic LinkedIn messaging without an appropriate supported integration
* Circumventing website restrictions

---

# 51. Phase 3 Milestones

## Milestone 1

Application Core

Deliver:

* Application entity
* Application repository
* Application service
* Application API
* Application history

---

## Milestone 2

Application State Machine

Deliver:

* State definitions
* Transition rules
* Validation
* State history
* Transition events

---

## Milestone 3

Approval Workflow

Deliver:

* Review state
* Approval
* Rejection
* Regeneration
* Approval history
* Notifications

---

## Milestone 4

Application Orchestrator

Deliver:

* Workflow engine
* Provider interface
* Action execution
* Retry handling
* Failure handling

---

## Milestone 5

Application Provider SPI

Deliver:

* Provider interface
* Manual provider
* Test provider
* Capability model

---

## Milestone 6

Browser Automation

Deliver:

* Browser lifecycle
* Navigation
* Form inspection
* Field mapping
* Document upload
* Submission
* Verification
* Manual intervention handling

---

## Milestone 7

Communication Automation

Deliver:

* Email provider
* Email sending
* Delivery state
* Communication history
* Recruiter outreach

---

## Milestone 8

Follow-Up Engine

Deliver:

* Scheduler
* Follow-up rules
* Business-day calculation
* Stop conditions
* Notifications

---

## Milestone 9

Application Analytics

Deliver:

* Dashboard metrics
* Conversion rates
* Application performance
* Recruiter response metrics
* Interview metrics

---

## Milestone 10

End-to-End Automation

Deliver:

```text
Job
 ↓
Analysis
 ↓
Workspace
 ↓
Application
 ↓
Approval
 ↓
Execution
 ↓
Submission
 ↓
Outreach
 ↓
Follow-up
 ↓
Outcome
```

---

# 52. Phase 3 Definition of Done

Phase 3 is complete only when:

* Application lifecycle is implemented.
* Application state machine is implemented.
* Approval workflow is implemented.
* Application orchestration is implemented.
* Provider abstraction exists.
* Browser automation is implemented within defined safety boundaries.
* Email automation is implemented.
* Follow-up scheduling is implemented.
* Application events are persisted.
* Notifications work.
* Application dashboard data is available.
* Analytics work.
* Failed workflows can be recovered manually.
* Duplicate submissions are prevented.
* Rate limits are enforced.
* Automated tests pass.
* Integration tests pass.
* End-to-end workflow tests pass.
* Docker deployment succeeds.
* OpenAPI documentation is complete.
* Flyway migrations work from a clean database.
* Existing Phase 1 and Phase 2 functionality remains functional.

---

# 53. Final Product Workflow

The completed Phase 3 system should support:

```text
                    JOB DISCOVERY
                         │
                         ▼
                   JOB MATCHING
                         │
                         ▼
                    JOB ANALYSIS
                         │
                         ▼
                  AI WORKSPACE
                         │
                         ▼
                   APPLICATION
                         │
                         ▼
                  PREPARATION
                         │
                         ▼
                  HUMAN REVIEW
                         │
                         ▼
                     APPROVAL
                         │
                         ▼
              APPLICATION ORCHESTRATOR
                         │
             ┌───────────┼───────────┐
             ▼           ▼           ▼
          Browser       API        Manual
             │           │           │
             └───────────┼───────────┘
                         ▼
                    SUBMISSION
                         │
                         ▼
                   VERIFICATION
                         │
                         ▼
                  RECRUITER OUTREACH
                         │
                         ▼
                   FOLLOW-UP
                         │
                         ▼
                    RESPONSE
                         │
                         ▼
                    INTERVIEW
                         │
                         ▼
                     OUTCOME
                         │
                         ▼
                    ANALYTICS
                         │
                         ▼
                 AI OPTIMIZATION
```

---

# 54. Architectural Rule

The most important rule of Phase 3:

> The Application Domain must never depend directly on a specific external job website, browser automation framework, email provider, or AI provider.

All external systems must be accessed through interfaces/adapters.

This allows AI Career OS to evolve without rewriting the core application engine.

---

# 55. Final Product Goal

Phase 3 should make the following experience possible:

> "I tell AI Career OS what kind of job I want. The system continuously finds suitable opportunities, evaluates them against my profile, prepares the complete application, asks for approval according to my automation settings, executes approved actions, contacts relevant recruiters, schedules appropriate follow-ups, tracks responses and interviews, and learns from the results."

This is the foundation for future Phase 4 Interview Intelligence and Phase 5 Career Intelligence.
