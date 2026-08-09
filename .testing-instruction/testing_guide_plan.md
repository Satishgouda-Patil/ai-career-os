# 🧪 Complete Testing & Feature Verification Guide — AI Career OS

This comprehensive guide provides step-by-step instructions for testing **AI Career OS** from scratch. It outlines all built features, environment setup commands, automated test execution, Swagger/cURL testing flows, and verification checklists.

---

## 🎯 1. Built Features Overview

| Domain Module | Primary Functionality | Key REST Endpoints |
| :--- | :--- | :--- |
| **Authentication & Security** | User Registration, Bcrypt Hashing, JWT Issuance & Validation | `POST /api/v1/auth/register`<br>`POST /api/v1/auth/login` |
| **Candidate Profile & Skills** | Career Summary, Experience, Skills Mapping (N:M) | `POST /api/v1/profiles`<br>`GET /api/v1/profiles/me` |
| **Job Fetching & Ingestion** | Jooble Ingestion Pipeline, Deduplication, RabbitMQ Event Queue | `POST /api/v1/jobs/fetch`<br>`GET /api/v1/jobs` |
| **Match Engine & Alerts** | Skill Overlap Scoring (0-100%), Telegram Bot Notifications (>80%) | `GET /api/v1/jobs/matches` |
| **AI Orchestrator Pipeline** | Resilient Prompt Execution, JSON Schema Parsing, Ollama Fallback Engine | Internal Core SPI Engine |
| **Deep Job Analysis** | Responsibilities Extraction, Missing Skills Identification, Recommendation | `POST /api/v1/jobs/{jobId}/analyze`<br>`GET /api/v1/jobs/{jobId}/analysis` |
| **ATS Resume Builder & Export** | Tailored ATS Content Generator, Apache PDFBox/POI PDF & DOCX Export, MinIO Upload | `POST /api/v1/resume/generate`<br>`GET /api/v1/resume/history` |
| **Cover Letter Intelligence** | Dynamic Cover Letter Generator, Tone Customizer (Professional/Direct/Executive), Version History | `POST /api/v1/cover-letter/generate`<br>`GET /api/v1/cover-letter/history` |
| **Recruiter Intelligence** | Pluggable `RecruiterDiscoveryProvider` SPI, Hunter.io Mock Discovery, Company/Recruiter Registry | `POST /api/v1/recruiters/discover`<br>`GET /api/v1/recruiters/company/{companyId}` |
| **Communication Intelligence** | Personalized Cold Outreach Emails, Follow-up Notes, LinkedIn Connection Requests | `POST /api/v1/emails/generate`<br>`GET /api/v1/emails/history` |
| **AI Workspace & Aggregator** | Unified Payload Aggregation (Job, Analysis, Resume, Cover Letter, Recruiter, Email), Approval Engine (`APPROVED`/`REJECTED`/`READY`) | `GET /api/v1/workspace/{jobId}`<br>`POST /api/v1/workspace/{jobId}/approve`<br>`POST /api/v1/workspace/{jobId}/reject` |

---

## ⚡ 2. Step 0: Automated Test Suite Verification

Before running manual API requests, execute the automated integration test suite. This verifies database migrations, JPA mapping, JWT filters, and full workspace assembly in an isolated H2 in-memory test environment.

### Command (PowerShell):
```powershell
.\gradlew.bat test
```

> **Expected Outcome:** `BUILD SUCCESSFUL` with all unit and integration tests passing (`Phase2WorkflowIntegrationTest`).

---

## 🐳 3. Step 1: Launch Infrastructure Services (Docker)

AI Career OS relies on 5 backend services: **MySQL 8**, **Redis 7**, **RabbitMQ 3.8**, **MinIO**, and **Ollama**.

### Start Docker Containers:
```powershell
docker-compose up -d mysql redis rabbitmq minio ollama
```

### Verify Container Health & Ports:

| Container | Host Port | Web Console / URL | Default Credentials |
| :--- | :--- | :--- | :--- |
| **MySQL 8** | `3306` | `jdbc:mysql://localhost:3306/career_os` | User: `root`, Pass: `root` |
| **Redis 7** | `6379` | `localhost:6379` | None |
| **RabbitMQ** | `5672` / `15672` | Management Console: [http://localhost:15672](http://localhost:15672) | User: `guest`, Pass: `guest` |
| **MinIO Object Store** | `9000` / `9001` | MinIO Console: [http://localhost:9001](http://localhost:9001) | User: `minioadmin`, Pass: `minioadmin` |
| **Ollama Local LLM** | `11434` | API Endpoint: [http://localhost:11434](http://localhost:11434) | None |

---

## 🚀 4. Step 2: Start Spring Boot Application Server

Launch the Spring Boot backend server using Gradle:

```powershell
.\gradlew.bat bootRun
```

Once started, open your web browser to verify Swagger OpenAPI Documentation:
- 📖 **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- 📄 **OpenAPI v3 JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 🛠️ 5. Step 3: End-to-End Manual Testing Flow (Postman / cURL)

Follow this chronological sequence to test all features end-to-end.

---

### 🔹 Phase 1 Testing Flow

#### **Flow 3.1: Candidate Registration & Login**
1. **Register Candidate:**
   - `POST /api/v1/auth/register`
   ```json
   {
     "email": "candidate@example.com",
     "password": "Password123!",
     "fullName": "Test Candidate"
   }
   ```
2. **Login & Save JWT Token:**
   - `POST /api/v1/auth/login`
   ```json
   {
     "email": "candidate@example.com",
     "password": "Password123!"
   }
   ```
   > 💡 **Copy the `token` from the response.** Include header: `Authorization: Bearer <TOKEN>` for all subsequent calls.

#### **Flow 3.2: Create Candidate Profile & Skills**
- `POST /api/v1/profiles`
```json
{
  "fullName": "Test Candidate",
  "summary": "Senior Java Developer with 6 years experience in Spring Boot, MySQL, and Kafka.",
  "skills": ["Java", "Spring Boot", "MySQL", "Docker", "RabbitMQ"]
}
```

#### **Flow 3.3: Ingest Job Postings & Verify RabbitMQ Queue**
1. **Trigger Jooble Ingestion:**
   - `POST /api/v1/jobs/fetch?keywords=Java%20Developer&location=Remote`
2. **Fetch List of Ingested Jobs:**
   - `GET /api/v1/jobs`
   > 💡 **Copy a `jobId`** from the returned list (e.g., `jobId: 1`).
3. **Verify Match Score:**
   - `GET /api/v1/jobs/matches`

---

### 🔹 Phase 2 Testing Flow

#### **Flow 3.4: Deep Job Analysis**
- `POST /api/v1/jobs/1/analyze`
```json
// Headers: Authorization: Bearer <TOKEN>
```
> **Expected Response (202 Accepted):** Returns structured analysis including key responsibilities, missing skills, and apply recommendation.

#### **Flow 3.5: ATS Resume Generation & Storage**
- `POST /api/v1/resume/generate`
```json
{
  "jobId": 1,
  "template": "MODERN"
}
```
> **Expected Response (202 Accepted):** Returns resume content, downloadable PDF URL, and DOCX URL.
> 💡 Log into MinIO Console ([http://localhost:9001](http://localhost:9001)) -> Bucket `resumes` to verify uploaded files!

#### **Flow 3.6: Cover Letter Generation & Tone Customization**
- `POST /api/v1/cover-letter/generate`
```json
{
  "jobId": 1,
  "tone": "Executive"
}
```
> **Expected Response (202 Accepted):** Returns cover letter content tailored with specified tone.

#### **Flow 3.7: Recruiter Intelligence Discovery**
- `POST /api/v1/recruiters/discover`
```json
{
  "companyId": 1
}
```
> **Expected Response (202 Accepted):** Returns technical recruiters/talent acquisition contacts for company.

#### **Flow 3.8: Cold Email & Connection Note Generation**
- `POST /api/v1/emails/generate`
```json
{
  "jobId": 1,
  "recruiterId": 1
}
```
> **Expected Response (202 Accepted):** Returns personalized cold outreach email, follow-up note, and LinkedIn message.

#### **Flow 3.9: AI Workspace Aggregator & Approval State Machine**
1. **Get Unified Workspace:**
   - `GET /api/v1/workspace/1`
   > **Expected Response:** Single aggregated JSON payload containing Job details, Job Analysis, Resume, ATS Score, Cover Letter, Recruiter List, and Cold Email.
2. **Approve Candidate Application Assets:**
   - `POST /api/v1/workspace/1/approve`
   > **Expected Response:** `{"status": "APPROVED"}`
3. **Reject Assets (Optional):**
   - `POST /api/v1/workspace/1/reject`
   > **Expected Response:** `{"status": "REJECTED"}`

---

## ✅ 6. Step 4: Verification Checklist Matrix

Use this matrix to confirm all capabilities function as expected:

| # | Feature to Verify | Verification Criteria | Status |
| :-: | :--- | :--- | :-: |
| 1 | **Database Migrations** | Flyway runs migrations V1 through V8 without error | [ ] |
| 2 | **JWT Authentication** | Invalid token returns `401 Unauthorized`; valid token grants access | [ ] |
| 3 | **Job Ingestion & Dedupe** | Ingesting duplicate jobs does not create duplicate rows in MySQL | [ ] |
| 4 | **RabbitMQ Event Bus** | Job fetch triggers `JobsFetchedEvent` -> `job.matching.queue` | [ ] |
| 5 | **AI Resiliency & Offline Fallback** | If Ollama container is offline, AI Orchestrator gracefully returns structured fallback JSON | [ ] |
| 6 | **MinIO S3 Document Storage** | Generated ATS Resumes upload to MinIO bucket `resumes` | [ ] |
| 7 | **Cover Letter Tones** | Changing tone parameter (`Professional` vs `Direct`) alters content tone | [ ] |
| 8 | **Recruiter Discovery SPI** | `RecruiterDiscoveryService` executes provider SPI and persists recruiters | [ ] |
| 9 | **Workspace State Machine** | Workspace status transitions accurately (`BUILDING` -> `READY` -> `APPROVED`/`REJECTED`) | [ ] |
