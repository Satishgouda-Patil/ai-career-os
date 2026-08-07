# Phase 2 Actionable Task Checklist - AI Career OS

Based on `.spec/phase-2/` documentation.

---

## 📋 Phase 2 Milestones Breakdown

### Milestone 1: AI Pipeline Core & Resume Intelligence

- [x] **Task 1.1: Implement AI Infrastructure Pipeline Core**
  - **Description:** Implement `ContextBuilder`, `PromptManager`, `JsonOutputValidator`, and `AIOrchestrator` to standardize AI LLM calls. Externalize prompt templates in `resources/prompts/`.
  - **Target Files:**
    - `src/main/java/com/ai/career/ai/orchestrator/AIOrchestrator.java`
    - `src/main/java/com/ai/career/ai/context/ContextBuilder.java`
    - `src/main/java/com/ai/career/ai/prompt/PromptManager.java`
    - `src/main/java/com/ai/career/ai/validator/JsonOutputValidator.java`

- [x] **Task 1.2: Create Resume Schema Migration**
  - **Description:** Create Flyway script `V3__create_resume_schema.sql` for `resume_versions`, `resume_templates`, and `resume_analyses` tables.
  - **Target Files:** `src/main/resources/db/migration/V3__create_resume_schema.sql`

- [x] **Task 1.3: Implement Resume Entities & Repositories**
  - **Description:** Implement `ResumeVersion`, `ResumeTemplate`, and `ResumeAnalysis` JPA entities and Spring Data repositories.
  - **Target Files:** `src/main/java/com/ai/career/resume/domain/entity/*.java`, `src/main/java/com/ai/career/resume/domain/repository/*.java`

- [x] **Task 1.4: Implement Resume Versioning & Service Layer**
  - **Description:** Create `ResumeService` for managing resume creation, version increments, retrieval history, and soft deletion.
  - **Target Files:** `src/main/java/com/ai/career/resume/service/*.java`

- [x] **Task 1.5: Implement ATS Resume Generator & Scoring Engine**
  - **Description:** Implement AI-powered ATS resume generator and analyzer computing overall score, keyword density, readability, format score, and missing keywords.
  - **Target Files:** `src/main/java/com/ai/career/resume/service/AtsAnalysisService.java`

- [x] **Task 1.6: Implement Resume Exporter (PDF/DOCX)**
  - **Description:** Implement PDF and DOCX resume generation and save binary exports to MinIO storage.
  - **Target Files:** `src/main/java/com/ai/career/resume/export/*.java`

---

### Milestone 2: Deep Job Intelligence & Skill Gap Analysis

- [x] **Task 2.1: Create Job Analysis Schema Migration**
  - **Description:** Create Flyway script `V4__create_job_analysis_schema.sql` for `job_analyses`, `job_missing_skills`, and `job_recommendations` tables.
  - **Target Files:** `src/main/resources/db/migration/V4__create_job_analysis_schema.sql`

- [x] **Task 2.2: Implement Job Analysis Entities & Repositories**
  - **Description:** Build `JobAnalysis`, `JobMissingSkill`, and `JobRecommendation` JPA entities and repositories.
  - **Target Files:** `src/main/java/com/ai/career/jobanalysis/domain/entity/*.java`, `src/main/java/com/ai/career/jobanalysis/domain/repository/*.java`

- [x] **Task 2.3: Implement AI Job Analyzer**
  - **Description:** Extract structured job details (responsibilities, required skills, preferred skills, salary range, work model, seniority level) using AI Orchestrator.
  - **Target Files:** `src/main/java/com/ai/career/jobanalysis/service/JobAnalyzerService.java`

- [x] **Task 2.4: Implement Missing Skills & Priority Engine**
  - **Description:** Compare user skills vs job requirements, generating prioritized missing skill gaps and learning suggestions.
  - **Target Files:** `src/main/java/com/ai/career/jobanalysis/service/MissingSkillsService.java`

- [x] **Task 2.5: Implement Application Recommendation Engine**
  - **Description:** Compute candidate recommendation (`APPLY`, `WAIT`, `SKIP`) with confidence score and structured rationale.
  - **Target Files:** `src/main/java/com/ai/career/jobanalysis/service/RecommendationEngine.java`

---

### Milestone 3: Cover Letter Intelligence

- [x] **Task 3.1: Create Cover Letter Schema Migration**
  - **Description:** Create Flyway script `V5__create_cover_letter_schema.sql` for `cover_letters` table.
  - **Target Files:** `src/main/resources/db/migration/V5__create_cover_letter_schema.sql`

- [x] **Task 3.2: Implement Cover Letter Entities & Repositories**
  - **Description:** Build `CoverLetter` JPA entity and repository.
  - **Target Files:** `src/main/java/com/ai/career/coverletter/domain/entity/CoverLetter.java`, `src/main/java/com/ai/career/coverletter/domain/repository/CoverLetterRepository.java`

- [x] **Task 3.3: Implement Personalized Cover Letter Generator**
  - **Description:** Generate personalized, company-aware, role-aware, and tone-aware cover letters with version history.
  - **Target Files:** `src/main/java/com/ai/career/coverletter/service/CoverLetterService.java`

---

### Milestone 4: Recruiter Intelligence

- [x] **Task 4.1: Create Company & Recruiter Schema Migration**
  - **Description:** Create Flyway script `V6__create_recruiter_schema.sql` for `companies` and `recruiters` tables.
  - **Target Files:** `src/main/resources/db/migration/V6__create_recruiter_schema.sql`

- [x] **Task 4.2: Implement Recruiter Intelligence & Discovery Service**
  - **Description:** Implement `Company` and `Recruiter` domain entities, repositories, and pluggable `RecruiterDiscoveryService` provider SPI.
  - **Target Files:** `src/main/java/com/ai/career/recruiter/service/RecruiterDiscoveryService.java`

---

### Milestone 5: Communication Intelligence (Cold Email & Messaging)

- [ ] **Task 5.1: Create Email Draft Schema Migration**
  - **Description:** Create Flyway script `V7__create_email_schema.sql` for `email_drafts` table.
  - **Target Files:** `src/main/resources/db/migration/V7__create_email_schema.sql`

- [ ] **Task 5.2: Implement Cold Email & Messaging Generator**
  - **Description:** Generate personalized cold emails, follow-up emails, and LinkedIn connection messages.
  - **Target Files:** `src/main/java/com/ai/career/communication/service/EmailGeneratorService.java`

---

### Milestone 6: AI Workspace & Unified REST APIs

- [ ] **Task 6.1: Create AI Workspace Schema Migration**
  - **Description:** Create Flyway script `V8__create_workspace_schema.sql` for `workspaces` table.
  - **Target Files:** `src/main/resources/db/migration/V8__create_workspace_schema.sql`

- [ ] **Task 6.2: Implement Workspace Aggregator Service**
  - **Description:** Aggregate job analysis, ATS resume, cover letter, recruiter info, cold email, and recommendation into a single workspace with approval/rejection state.
  - **Target Files:** `src/main/java/com/ai/career/workspace/service/WorkspaceService.java`

- [ ] **Task 6.3: Implement REST Controllers Suite**
  - **Description:** Expose standard Phase 2 REST APIs (`/api/v1/resume`, `/api/v1/jobs/analysis`, `/api/v1/cover-letter`, `/api/v1/recruiters`, `/api/v1/emails`, `/api/v1/workspace`) with response wrappers (`{ success, data, message, timestamp, requestId }`).
  - **Target Files:** `src/main/java/com/ai/career/web/controller/*.java`

- [ ] **Task 6.4: OpenAPI 3.0 Documentation & Comprehensive Integration Tests**
  - **Description:** Update OpenAPI Swagger spec and write integration tests for Phase 2 workflows.
  - **Target Files:** `src/test/java/com/ai/career/integration/phase2/*Test.java`
