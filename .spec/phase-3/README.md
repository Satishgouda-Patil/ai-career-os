# AI Career OS — Phase 3

## Application Automation Engine

Version: 3.0

Status: Specification

---

# 1. Purpose

Phase 3 transforms AI Career OS from an AI-powered job preparation platform into an application automation platform.

Phase 1 provides:

- Authentication
- Candidate profile
- Job discovery
- Job ingestion
- Job matching
- Notifications
- Infrastructure

Phase 2 provides:

- Job intelligence
- Resume generation
- ATS analysis
- Cover letters
- Recruiter intelligence
- Cold email generation
- AI workspace

Phase 3 adds:

- Application tracking
- Application lifecycle management
- Application preparation
- Human approval workflows
- Application submission infrastructure
- Browser automation
- Email outreach automation
- Follow-up automation
- Application analytics
- Automation scheduling
- End-to-end application orchestration

---

# 2. Product Vision

The user should be able to configure their career preferences once and allow AI Career OS to continuously discover relevant opportunities, prepare applications, request approval where necessary, execute approved actions, track outcomes, and improve future recommendations.

The system should progressively reduce the amount of manual work required from the candidate.

---

# 3. Core Workflow

The Phase 3 workflow is:

Job discovered

↓

Job qualified

↓

AI workspace generated

↓

Application created

↓

Resume selected/generated

↓

Cover letter selected/generated

↓

Recruiter identified

↓

Application prepared

↓

User approval

↓

Application submitted

↓

Application confirmed

↓

Recruiter outreach

↓

Follow-up scheduling

↓

Response tracking

↓

Interview tracking

↓

Outcome recorded

↓

Performance analytics

↓

AI optimization

---

# 4. Central Concept

The Application entity is the central domain object of Phase 3.

The relationship becomes:

Job

↓

AI Workspace

↓

Application

↓

Application Actions

↓

Application Events

↓

Application Outcome

---

# 5. Phase 3 Modules

## Application Module

Responsible for:

- Application creation
- Application lifecycle
- Application state
- Application history
- Application metadata
- Application outcome

---

## Application Orchestrator

Responsible for:

- Preparing applications
- Coordinating application providers
- Executing approved workflows
- Handling retries
- Handling failures
- Publishing events

---

## Browser Automation Module

Responsible for:

- Browser sessions
- Navigation
- Form detection
- Field mapping
- Resume upload
- Application submission
- Submission verification

Browser automation must be provider-based.

---

## Communication Automation Module

Responsible for:

- Email sending
- Recruiter outreach
- Follow-ups
- Message scheduling
- Delivery status

---

## Approval Module

Responsible for:

- Human approval
- Rejection
- Review state
- Approval history

---

## Scheduler Module

Responsible for:

- Follow-up scheduling
- Application processing
- Retry scheduling
- Automation jobs

---

## Analytics Module

Responsible for:

- Application statistics
- Interview rate
- Response rate
- Resume performance
- Outreach performance
- Conversion rates

---

# 6. Human-in-the-Loop

Phase 3 must not blindly submit applications.

The default workflow is:

AI prepares

↓

Human reviews

↓

Human approves

↓

System executes

The system may later support configurable automation levels.

---

# 7. Automation Levels

## LEVEL_0

Manual

AI only prepares recommendations.

---

## LEVEL_1

Approval Required

AI prepares everything.

User approves execution.

---

## LEVEL_2

Trusted Automation

User explicitly enables automation for selected workflows.

---

## LEVEL_3

Advanced Automation

System executes approved workflow types automatically according to user-defined rules.

LEVEL_3 must never bypass safety rules or user restrictions.

---

# 8. Safety Principles

The system must never:

- Submit an application using incorrect candidate information.
- Invent qualifications.
- Invent work experience.
- Invent certifications.
- Invent education.
- Misrepresent authorization status.
- Submit without required approval.
- Send unlimited recruiter messages.
- Spam recruiters.
- Bypass website security mechanisms.
- Circumvent CAPTCHA.
- Circumvent authentication or access controls.
- Store passwords in plaintext.

---

# 9. Provider Architecture

External application systems must be accessed through provider interfaces.

Example:

ApplicationProvider

↓

DirectApplicationProvider

BrowserApplicationProvider

ApiApplicationProvider

The business domain must never depend directly on a specific job website.

---

# 10. Phase 3 Success Criteria

Phase 3 is successful when the system can:

1. Create an application from a matched job.
2. Assemble a complete application workspace.
3. Track application state.
4. Request human approval.
5. Execute an approved application workflow.
6. Record submission confirmation.
7. Send approved recruiter communication.
8. Schedule follow-ups.
9. Track application events.
10. Display application analytics.
11. Recover from automation failures.
12. Notify the user at important stages.

---

# 11. Non-Goals

Phase 3 does not attempt to guarantee employment or interviews.

The objective is to maximize the quality, consistency, and efficiency of the job search.

The system must measure actual outcomes rather than claiming guaranteed results.

---

# 12. Technology

Continue using the existing stack:

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- MySQL 8
- Redis
- RabbitMQ
- MinIO
- Ollama
- Flyway
- Docker Compose
- Gradle

Additional browser automation technology may be introduced only through the Browser Automation specification.

---

# 13. Implementation Strategy

Phase 3 must be implemented incrementally.

Milestone order:

1. Application Core
2. Application State Machine
3. Approval Workflow
4. Application Orchestrator
5. Automation Provider SPI
6. Browser Automation
7. Communication Automation
8. Follow-up Scheduler
9. Application Analytics
10. End-to-End Automation

Each milestone must pass its acceptance criteria before the next milestone begins.