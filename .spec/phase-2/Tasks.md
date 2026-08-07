# Phase 2 Build Tasks

## AI Career OS

Version: 2.0

---

# Overview

This document is the implementation backlog for Phase 2.

Each task is independently implementable and must satisfy the acceptance criteria before moving to the next task.

No task may introduce architectural changes that contradict Architecture.md.

---

# Milestone 1 — Resume Intelligence

## TASK-001

Title

Create Resume Domain

Objective

Create the Resume module.

Deliverables

- ResumeVersion entity
- ResumeTemplate entity
- ResumeAnalysis entity
- Repository layer
- DTOs
- MapStruct mappers
- Service interfaces
- Service implementation

Definition of Done

- Flyway migration created
- CRUD works
- Unit tests pass

---

## TASK-002

Resume Versioning

Objective

Support multiple resume versions.

Requirements

- Version increment
- Immutable history
- Restore previous version
- Soft delete only

Done

Resume history visible through API.

---

## TASK-003

Resume Generator

Objective

Generate resumes from structured profile data.

Requirements

- AI Orchestrator integration
- Prompt Manager
- JSON validation
- Save generated version

---

## TASK-004

ATS Analysis

Requirements

Generate

- Overall score
- Missing keywords
- Recommendations
- Readability
- Formatting

Persist analysis.

---

## TASK-005

Resume Export

Support

- PDF
- DOCX

Store in MinIO.

---

# Milestone 2 — Job Intelligence

## TASK-006

Create JobAnalysis entity.

---

## TASK-007

AI Job Analyzer

Extract

- Responsibilities
- Skills
- Experience
- Salary
- Work model
- Seniority

Persist results.

---

## TASK-008

Missing Skills Engine

Generate

- Missing skills
- Priority
- Learning suggestions

---

## TASK-009

Recommendation Engine

Generate

- Apply
- Wait
- Skip

Include explanation.

---

# Milestone 3 — Cover Letter Intelligence

## TASK-010

Create CoverLetter entity.

---

## TASK-011

Cover Letter Generator

Requirements

- Personalized
- Company aware
- Role aware
- Tone aware

---

## TASK-012

Version History

Store every generation.

---

# Milestone 4 — Recruiter Intelligence

## TASK-013

Create Company entity.

---

## TASK-014

Create Recruiter entity.

---

## TASK-015

Recruiter Discovery Service

Support pluggable providers.

Do not hardcode implementation.

---

# Milestone 5 — Communication Intelligence

## TASK-016

Email Draft entity.

---

## TASK-017

Generate Email Draft.

---

## TASK-018

Generate Follow-up Email.

---

## TASK-019

Generate LinkedIn Message.

Manual usage only.

---

# Milestone 6 — AI Workspace

## TASK-020

Workspace Entity

---

## TASK-021

Workspace Builder

Combine

- Resume
- ATS
- Cover Letter
- Recruiter
- Email
- Recommendation

---

## TASK-022

Workspace API

Endpoints

GET /workspace/{jobId}

POST /workspace/{jobId}/approve

POST /workspace/{jobId}/regenerate

---

# AI Infrastructure

## TASK-023

Prompt Manager

Externalize prompts.

---

## TASK-024

Context Builder

Aggregate profile, job, history, preferences.

---

## TASK-025

AI Orchestrator

Responsibilities

- Build context
- Select prompt
- Call Ollama
- Retry
- Validate
- Persist execution

---

## TASK-026

Output Validator

Validate JSON schema.

Retry invalid outputs.

---

# Events

## TASK-027

Publish JobAnalyzed event.

---

## TASK-028

Publish ResumeGenerated event.

---

## TASK-029

Publish WorkspaceCreated event.

---

# REST APIs

## TASK-030

Resume APIs

GET /resume

POST /resume/generate

GET /resume/history

GET /resume/{id}

---

## TASK-031

Job Analysis APIs

GET /jobs/{id}/analysis

---

## TASK-032

Cover Letter APIs

GET /cover-letter/{jobId}

POST /cover-letter/generate

---

## TASK-033

Recruiter APIs

GET /recruiters/{companyId}

---

## TASK-034

Email APIs

GET /emails/{jobId}

POST /emails/generate

---

# Testing

## TASK-035

Unit Tests

Minimum coverage

80%

---

## TASK-036

Integration Tests

REST

RabbitMQ

Flyway

Repositories

---

## TASK-037

AI Tests

Mock Ollama.

Validate parsing.

---

# Documentation

## TASK-038

Update OpenAPI.

---

## TASK-039

Update Architecture docs.

---

## TASK-040

Final QA

Checklist

- All tests pass
- No Flyway errors
- No duplicated code
- OpenAPI complete
- Docker compose works
- Phase 2 demo successful

---

# Phase Completion Criteria

Phase 2 is complete only if:

- All 40 tasks are implemented.
- All automated tests pass.
- Docker deployment succeeds.
- APIs are documented.
- AI outputs are versioned.
- Workspace is functional.
- Resume, Cover Letter, Job Analysis, Recruiter, and Email generation work together.
- Human approval is required before any future automation.