# API Specification

# AI Career OS

## Phase 2

Version: 2.0

Status: Approved

Owner: Chief Architect

---

# Purpose

This document defines every REST API introduced during Phase 2.

All APIs are RESTful.

JSON only.

JWT Authentication required unless stated otherwise.

Base URL

/api/v1

---

# Standards

Content-Type

application/json

Authentication

Bearer JWT

Date Format

ISO-8601 UTC

Response Wrapper

{
  "success": true,
  "data": {},
  "message": "",
  "timestamp": "",
  "requestId": ""
}

Error Wrapper

{
  "success": false,
  "errorCode": "",
  "message": "",
  "timestamp": "",
  "requestId": ""
}

---

# Authentication

Every endpoint requires JWT except

POST /auth/login

POST /auth/register

GET /actuator/health

GET /swagger-ui

---

# Resume APIs

## Generate Resume

POST /resume/generate

Description

Generate ATS optimized resume.

Request

{
    "jobId":1,
    "template":"MODERN"
}

Response

{
    "resumeId":101,
    "version":1,
    "status":"GENERATED",
    "atsScore":91
}

Status

202 Accepted

---

## Resume History

GET /resume/history

Response

[
   {
      "id":10,
      "version":1,
      "createdAt":"",
      "atsScore":88
   }
]

---

## Resume Details

GET /resume/{resumeId}

Response

{
   "id":1,
   "version":2,
   "content":{},
   "analysis":{}
}

---

## Resume Export PDF

GET /resume/{resumeId}/pdf

Response

Binary PDF

---

## Resume Export DOCX

GET /resume/{resumeId}/docx

Response

Binary DOCX

---

## Delete Resume

DELETE /resume/{resumeId}

Soft delete only.

---

# ATS APIs

## Analyze Resume

POST /resume/{resumeId}/analyze

Response

{
   "overallScore":91,
   "keywordScore":95,
   "formatScore":90,
   "missingKeywords":[
      "Kafka",
      "Terraform"
   ]
}

---

## Resume Recommendations

GET /resume/{resumeId}/recommendations

Response

{
   "recommendations":[]
}

---

# Job Analysis APIs

## Analyze Job

POST /jobs/{jobId}/analyze

Response

{
   "analysisId":21,
   "status":"COMPLETED"
}

202 Accepted

---

## Get Job Analysis

GET /jobs/{jobId}/analysis

Response

{
  "summary":"",
  "responsibilities":[],
  "requiredSkills":[],
  "preferredSkills":[],
  "salary":{},
  "matchScore":93,
  "recommendation":"APPLY"
}

---

## Missing Skills

GET /jobs/{jobId}/missing-skills

Response

[
 {
   "skill":"Kafka",
   "priority":"HIGH"
 }
]

---

## Match Explanation

GET /jobs/{jobId}/recommendation

Response

{
   "recommendation":"APPLY",
   "confidence":94,
   "reason":[]
}

---

# Cover Letter APIs

## Generate Cover Letter

POST /cover-letter/generate

Request

{
    "jobId":1,
    "tone":"Professional"
}

Response

{
    "coverLetterId":11,
    "status":"GENERATED"
}

---

## Get Cover Letter

GET /cover-letter/{jobId}

Response

{
   "version":1,
   "content":"..."
}

---

## Regenerate

POST /cover-letter/{jobId}/regenerate

Creates new version.

---

## History

GET /cover-letter/history

Returns all versions.

---

# Workspace APIs

## Build Workspace

POST /workspace/{jobId}

Response

{
    "workspaceId":7,
    "status":"BUILDING"
}

202 Accepted

---

## Get Workspace

GET /workspace/{jobId}

Response

{
   "job":{},
   "analysis":{},
   "resume":{},
   "ats":{},
   "coverLetter":{},
   "email":{},
   "recommendation":{}
}

---

## Approve Workspace

POST /workspace/{jobId}/approve

Response

{
   "status":"APPROVED"
}

---

## Reject Workspace

POST /workspace/{jobId}/reject

Response

{
   "status":"REJECTED"
}

---

## Regenerate Workspace

POST /workspace/{jobId}/regenerate

Creates fresh AI artifacts.

---

# Recommendation APIs

## Recommendation

GET /recommendations/{jobId}

Response

{
   "decision":"APPLY",
   "confidence":92,
   "pros":[],
   "cons":[]
}

---

## Interview Probability

GET /recommendations/{jobId}/probability

Response

{
    "probability":87
}

---

# Common Status Codes

200 OK

201 Created

202 Accepted

204 No Content

400 Validation Error

401 Unauthorized

403 Forbidden

404 Not Found

409 Conflict

422 AI Validation Failed

429 Too Many Requests

500 Internal Error

503 AI Service Unavailable

---

# Pagination

GET endpoints returning collections support

?page=0

&size=20

&sort=createdAt,desc

---

# Filtering

Supported

status

company

skill

date

matchScore

atsScore

recommendation

Example

GET /resume/history?page=0&size=20

---

# Validation Rules

All DTOs use Bean Validation.

Examples

@NotNull

@NotBlank

@Email

@Size

@Min

@Max

---

# Idempotency

The following endpoints are idempotent

GET

DELETE

PUT

Generation endpoints

POST

are NOT idempotent.

Every generation creates a new version.

---

# Part 1 Complete

Part 2 includes

- Recruiter APIs
- Email APIs
- AI APIs
- Prompt APIs
- Admin APIs
- Event APIs
- Notification APIs
- Error Codes
- OpenAPI Standards
- Versioning Strategy
- Security Rules

---

# Recruiter APIs

## Discover Recruiters

POST /recruiters/discover

Description

Discover recruiters for a company using configured provider(s).

Request

{
  "companyId": 10
}

Response

{
  "status": "PROCESSING",
  "jobId": "DISCOVERY-001"
}

Status

202 Accepted

---

## Get Company Recruiters

GET /recruiters/company/{companyId}

Response

[
  {
    "id":1,
    "name":"John Doe",
    "title":"Senior Technical Recruiter",
    "linkedinUrl":"",
    "email":"",
    "confidenceScore":91
  }
]

---

## Recruiter Details

GET /recruitters/{recruiterId}

Response

{
  "id":1,
  "company":{},
  "name":"",
  "title":"",
  "email":"",
  "linkedin":"",
  "location":"",
  "confidenceScore":90
}

---

# Email APIs

## Generate Cold Email

POST /emails/generate

Request

{
  "jobId":10,
  "recruiterId":5
}

Response

{
  "emailId":100,
  "status":"GENERATED"
}

---

## Get Email

GET /emails/{emailId}

Response

{
  "subject":"",
  "body":"",
  "followup":"",
  "linkedinMessage":""
}

---

## Regenerate Email

POST /emails/{emailId}/regenerate

Creates a new version.

---

## Email History

GET /emails/history

Response

[
  {
    "id":1,
    "version":1,
    "createdAt":""
  }
]

---

# AI APIs

## Generate AI Workspace

POST /ai/workspace/{jobId}

Description

Generate every AI artifact.

Response

{
  "status":"PROCESSING"
}

---

## AI Execution History

GET /ai/executions

Response

[
   {
      "executionId":"",
      "prompt":"resume_v1",
      "model":"llama3.1",
      "latency":4200,
      "status":"SUCCESS"
   }
]

---

## AI Execution Details

GET /ai/executions/{executionId}

Response

{
   "execution":{},
   "context":{},
   "prompt":{},
   "response":{}
}

---

# Prompt APIs

## Prompt List

GET /prompts

Response

[
  {
    "name":"resume",
    "version":"v1",
    "active":true
  }
]

---

## Prompt Details

GET /prompts/{name}

---

## Activate Prompt Version

PUT /prompts/{name}/activate/{version}

Response

{
   "status":"ACTIVE"
}

---

# Company APIs

## Company Details

GET /companies/{companyId}

---

## Company Search

GET /companies

Supports

name

industry

location

---

# Notification APIs

## Notifications

GET /notifications

---

## Mark Read

PUT /notifications/{id}/read

---

## Test Notification

POST /notifications/test

---

# Event APIs

## Event History

GET /events

---

## Event Details

GET /events/{eventId}

Response

{
   "eventType":"",
   "status":"",
   "createdAt":"",
   "payload":{}
}

---

# Health APIs

GET /actuator/health

GET /actuator/metrics

GET /actuator/prometheus

(Spring Boot Actuator)

---

# OpenAPI

/swagger-ui

/api-docs

Every endpoint must include

Summary

Description

Request Example

Response Example

Possible Errors

Authentication

---

# Error Codes

AUTH-001

Invalid Credentials

AUTH-002

Expired Token

AUTH-003

Unauthorized

PROFILE-001

Profile Not Found

PROFILE-002

Resume Missing

JOB-001

Job Not Found

JOB-002

Job Analysis Failed

RESUME-001

Resume Generation Failed

RESUME-002

ATS Analysis Failed

LETTER-001

Cover Letter Failed

EMAIL-001

Email Generation Failed

AI-001

LLM Offline

AI-002

Prompt Missing

AI-003

Invalid AI Response

AI-004

JSON Validation Failed

WORKSPACE-001

Workspace Not Found

WORKSPACE-002

Workspace Build Failed

SYSTEM-001

Unexpected Error

---

# Rate Limiting

Resume Generation

10/hour

Cover Letter

20/hour

Email Generation

20/hour

Workspace Generation

20/hour

AI APIs

50/hour

---

# Security

JWT Required

HTTPS Only

CORS Configured

Input Validation

Output Sanitization

Prompt Injection Protection

Environment Variables for Secrets

No sensitive data in logs

---

# API Versioning

Current Version

v1

Future

/api/v2

Backward compatibility must be maintained for one major version.

---

# Naming Standards

Resources use plural nouns.

Examples

/users

/jobs

/resumes

/workspaces

Use kebab-case for URLs where needed.

---

# Async Processing

Long-running operations return

202 Accepted

with a tracking identifier.

Clients should poll the resource or use future notification mechanisms.

---

# API Acceptance Criteria

The API layer is complete only if:

✓ RESTful conventions followed

✓ JWT authentication enforced

✓ OpenAPI documentation generated

✓ Bean Validation applied

✓ Standard response wrapper used

✓ Standard error wrapper used

✓ Pagination supported

✓ Filtering supported

✓ Versioning supported

✓ Async endpoints return 202

✓ Integration tests cover every public endpoint

✓ All APIs conform to Architecture.md