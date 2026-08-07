# Phase 1 Task Checklist - AI Career OS

Based on `.spec/plan.md` and `prd-phase1.md`.

---

## 🔍 Local Setup & Software Verification Summary

Prior to task execution, a full scan of the local Windows environment was conducted.

| Software / Tool | Status | Installed Version / Path | Action / Note |
| :--- | :---: | :--- | :--- |
| **Java JDK** | ✅ Verified | Java 21.0.7 LTS | Ready for Spring Boot 3.x |
| **Gradle** | ✅ Verified | Gradle 9.5.1 (`C:\ProgramData\chocolatey\bin\gradle.exe`) | Installed & ready |
| **Git** | ✅ Verified | git version 2.45.2.windows.1 | Version control active |
| **Node.js** | ✅ Verified | v20.10.0 | Available for web/UI stubs |
| **MySQL Server** | ✅ Verified | Running on Port 3306 (`MySQL80` service active) | Service ready |
| **Maven** | ⚠️ Missing | Not found in system PATH | Use `gradle` or initialize `./gradlew` wrapper |
| **Docker / Docker Desktop** | ⚠️ Missing | `docker` CLI not in PATH | Install Docker Desktop for Windows or run local services |
| **Ollama CLI** | ⚠️ Missing | `ollama` CLI not in PATH | Download & install Ollama for Windows (http://ollama.com) |

> **Prerequisite Action Items:**
> 1. Download and install **Docker Desktop** (or use local native MySQL/Redis/RabbitMQ services).
> 2. Download and install **Ollama for Windows** (`ollama pull llama3` or `ollama pull mistral`) to enable local LLM features.
> 3. Initialize Gradle wrapper (`gradle wrapper`) in project root.

---

## 📋 Phase 1 Actionable Task Checklist

### Milestone 1: Infra Setup (Docker & Spring Boot Initialization)

- [x] **Task 1.1: Initialize Spring Boot Project Structure**
  - **Description:** Initialize Spring Boot 3.x project with Java 21, Gradle, Lombok, Spring Web, Spring Data JPA, Spring Security, Validation, and Spring AMQP dependencies.
  - **Target Files:** `build.gradle`, `settings.gradle`, `src/main/java/com/ai/career/AiCareerOsApplication.java`, `src/main/resources/application.yml`
  - **Acceptance Criteria:** Application builds successfully via `./gradlew build` and boots up cleanly on port `8080`.

- [x] **Task 1.2: Configure Environment Profiles & Logging**
  - **Description:** Setup `application-dev.yml` and `application-prod.yml` with dynamic environment variables for MySQL, Redis, RabbitMQ, MinIO, and Ollama. Configure structured logging.
  - **Target Files:** `src/main/resources/application.yml`, `src/main/resources/application-dev.yml`, `src/main/resources/logback-spring.xml`
  - **Acceptance Criteria:** App starts with `--spring.profiles.active=dev` without hardcoded secrets.

- [x] **Task 1.3: Create Docker Compose Infrastructure Setup**
  - **Description:** Build `docker-compose.yml` to orchestrate MySQL 8, Redis 7, RabbitMQ 3.8 (management UI), MinIO, Qdrant/Chroma, and Ollama services.
  - **Target Files:** `docker-compose.yml`, `.env.example`
  - **Acceptance Criteria:** `docker compose up -d` boots up containers and all health checks pass.

- [ ] **Task 1.4: Setup Spring Boot Actuator & Health Indicators**
  - **Description:** Expose `/actuator/health` and `/actuator/metrics` endpoints with custom health indicators for DB, RabbitMQ, and Ollama.
  - **Target Files:** `src/main/java/com/ai/career/config/HealthConfig.java`, `src/main/resources/application.yml`
  - **Acceptance Criteria:** `GET /actuator/health` returns `UP` status with individual subsystem statuses.

---

### Milestone 2: Database & Auth

- [x] **Task 2.1: Flyway Schema Migrations Setup**
  - **Description:** Add Flyway dependency and create baseline SQL migration scripts for `users`, `profiles`, `skills`, `profile_skills`, `jobs`, `job_matches`, and `notifications` tables.
  - **Target Files:** `src/main/resources/db/migration/V1__init_schema.sql`, `src/main/resources/db/migration/V2__seed_skills.sql`
  - **Acceptance Criteria:** DB tables created automatically on application startup with seeded initial skills data.

- [x] **Task 2.2: Implement JPA Domain Entities & Repositories**
  - **Description:** Define Java JPA Entities (`User`, `Profile`, `Skill`, `Job`, `JobMatch`, `Notification`) following Clean Architecture guidelines and create Spring Data JPA Repositories.
  - **Target Files:** 
    - `src/main/java/com/ai/career/domain/entity/*.java`
    - `src/main/java/com/ai/career/domain/repository/*.java`
  - **Acceptance Criteria:** All entities mapped correctly with relational keys, unique indexes, and repository methods tested via `@DataJpaTest`.

- [x] **Task 2.3: Implement JWT Utility & Spring Security Filter Chain**
  - **Description:** Create JWT token provider (generate/validate token, extract claims) and `JwtAuthenticationFilter` attached to Spring Security filter chain.
  - **Target Files:** 
    - `src/main/java/com/ai/career/security/JwtTokenProvider.java`
    - `src/main/java/com/ai/career/security/JwtAuthenticationFilter.java`
    - `src/main/java/com/ai/career/config/SecurityConfig.java`
  - **Acceptance Criteria:** Protected endpoints reject unauthorized requests with 401 Unauthenticated, and valid Bearer tokens pass context.

- [x] **Task 2.4: Implement Auth Service & Password Hashing**
  - **Description:** Implement `AuthService` handling user registration (`BCryptPasswordEncoder`), user login, JWT issuance, and DTO mappings (`RegisterRequest`, `LoginRequest`, `AuthResponse`).
  - **Target Files:**
    - `src/main/java/com/ai/career/auth/dto/*.java`
    - `src/main/java/com/ai/career/auth/service/AuthService.java`
    - `src/main/java/com/ai/career/auth/service/impl/AuthServiceImpl.java`
  - **Acceptance Criteria:** User registration encrypts passwords with BCrypt, login verifies hash and returns valid JWT.

- [x] **Task 2.5: Implement Profile & Skill Service**
  - **Description:** Implement `ProfileService` for retrieving and updating user profile details, experience, skills list association, and PDF resume metadata.
  - **Target Files:**
    - `src/main/java/com/ai/career/profile/dto/*.java`
    - `src/main/java/com/ai/career/profile/service/ProfileService.java`
    - `src/main/java/com/ai/career/profile/service/impl/ProfileServiceImpl.java`
  - **Acceptance Criteria:** Profile CRUD operations validate inputs, update relational skill joins, and update profile state correctly.

- [x] **Task 2.6: Implement MinIO Resume Upload Integration**
  - **Description:** Integrate MinIO Client SDK to handle multipart resume PDF uploads (`POST /api/v1/profile/resume`), store in MinIO bucket `resumes`, and save URL in profile record.
  - **Target Files:**
    - `src/main/java/com/ai/career/config/MinioConfig.java`
    - `src/main/java/com/ai/career/storage/service/FileStorageService.java`
  - **Acceptance Criteria:** Uploaded resume PDF is saved in MinIO bucket and pre-signed/direct URL is recorded in MySQL `profiles.resume_url`.

---

### Milestone 3: Job Fetching & RabbitMQ Pipeline

- [x] **Task 3.1: Configure RabbitMQ Exchanges, Queues, and Bindings**
  - **Description:** Define RabbitMQ configuration with topic exchange `job.exchange`, queues (`job.fetch.queue`, `job.match.queue`, `notification.queue`), and routing keys (`job.fetched`, `profile.updated`, `match.found`).
  - **Target Files:** `src/main/java/com/ai/career/config/RabbitMQConfig.java`
  - **Acceptance Criteria:** RabbitMQ queues and exchanges are declared automatically on startup.

- [x] **Task 3.2: Implement Jooble Job Fetcher Connector**
  - **Description:** Create `JoobleJobFetcher` using Spring `RestClient`/`RestTemplate` to fetch jobs from Jooble REST API, mapping response items to standard `JobDto`.
  - **Target Files:**
    - `src/main/java/com/ai/career/job/connector/JoobleJobFetcher.java`
    - `src/main/java/com/ai/career/job/dto/JoobleResponseDto.java`
  - **Acceptance Criteria:** Connector successfully queries external API, parses response payload, and handles API downtime gracefully with retry/fallback.

- [x] **Task 3.3: Implement Job Ingestion & Deduplication Service**
  - **Description:** Create `JobIngestionService` to process incoming fetched jobs, check unique constraint `(source, source_job_id)` or `(title, company, location)`, persist new jobs, and publish `JobsFetchedEvent` to RabbitMQ.
  - **Target Files:** `src/main/java/com/ai/career/job/service/JobIngestionService.java`
  - **Acceptance Criteria:** Duplicate postings are filtered out; only new jobs are saved to DB and triggered into message queue.

- [x] **Task 3.4: Implement Scheduled Ingestion Runner**
  - **Description:** Add `@Scheduled` cron/periodic job execution to trigger job fetchers at configurable intervals.
  - **Target Files:** `src/main/java/com/ai/career/job/scheduler/JobFetchScheduler.java`
  - **Acceptance Criteria:** Scheduler automatically triggers job ingestion pipeline without manual intervention.

- [x] **Task 3.5: Implement RabbitMQ Match Listener & Match Scoring Service**
  - **Description:** Implement `MatchScoringService` listening to `JobsFetchedEvent` and `ProfileUpdatedEvent`. Calculate keyword skill overlap score (0-100), persist to `job_matches` table, and publish `MatchFoundEvent` if score > threshold (e.g. 80).
  - **Target Files:**
    - `src/main/java/com/ai/career/match/listener/JobMatchEventListener.java`
    - `src/main/java/com/ai/career/match/service/MatchScoringService.java`
  - **Acceptance Criteria:** Processing event messages correctly computes match score, stores records in `job_matches`, and forwards high-match events.

- [x] **Task 3.6: Implement Telegram Notification Listener**
  - **Description:** Implement `NotificationService` listening to `MatchFoundEvent`, generating formatted Telegram alert markdown messages, calling Telegram Bot API via HTTP, and updating `notifications` log.
  - **Target Files:**
    - `src/main/java/com/ai/career/notify/listener/NotificationEventListener.java`
    - `src/main/java/com/ai/career/notify/service/TelegramNotificationService.java`
  - **Acceptance Criteria:** High match event triggers Telegram HTTP request and logs delivery status in DB.

---

### Milestone 4: Local LLM Integration (Ollama)

- [x] **Task 4.1: Implement Ollama HTTP Client Integration**
  - **Description:** Create `OllamaClientService` using Spring `RestClient` to connect to local Ollama server (`http://localhost:11434/api/generate` or `/api/chat`).
  - **Target Files:**
    - `src/main/java/com/ai/career/config/OllamaConfig.java`
    - `src/main/java/com/ai/career/llm/client/OllamaClientService.java`
  - **Acceptance Criteria:** Client can send prompt payload to local Ollama instance and extract completion text response with connection timeout/retry rules.

- [x] **Task 4.2: Develop Match Scoring LLM Prompt & Service Fallback**
  - **Description:** Design prompt template for local LLM skill ranking and semantic match evaluation. Update `MatchScoringService` to use local LLM scoring with fallback to term frequency scoring if Ollama service is unavailable.
  - **Target Files:**
    - `src/main/java/com/ai/career/llm/prompt/PromptTemplates.java`
    - `src/main/java/com/ai/career/llm/service/LlmMatchEvaluator.java`
  - **Acceptance Criteria:** System uses local LLM matching when available, and seamlessly degrades to exact keyword match algorithm if Ollama server is offline.

- [x] **Task 4.3: Implement Resume Tailoring Stub Endpoint & Service Interface**
  - **Description:** Create `ResumeTailoringService` interface and Phase 1 stub implementation `OllamaResumeTailorStub`. Stub accepts `jobId`, reads user base profile, and generates tailored summary preview via local LLM prompt.
  - **Target Files:**
    - `src/main/java/com/ai/career/llm/service/ResumeTailoringService.java`
    - `src/main/java/com/ai/career/llm/service/impl/OllamaResumeTailorStubImpl.java`
  - **Acceptance Criteria:** Endpoint returns AI-suggested profile adjustments or fallback original resume when called.

---

### Milestone 5: REST Controller Endpoints

- [ ] **Task 5.1: Implement Auth Controllers (`/api/v1/auth`)**
  - **Description:** Build `AuthController` with endpoints:
    - `POST /api/v1/auth/register`
    - `POST /api/v1/auth/login`
  - **Target Files:** `src/main/java/com/ai/career/auth/controller/AuthController.java`
  - **Acceptance Criteria:** Enpoints validate DTO annotations (`@NotBlank`, `@Email`), return standard HTTP status codes (201 Created, 200 OK, 400 Bad Request, 401 Unauthorized), and return JWT tokens.

- [ ] **Task 5.2: Implement Career Profile Controllers (`/api/v1/profile`)**
  - **Description:** Build `ProfileController` with endpoints:
    - `GET /api/v1/profile`
    - `PUT /api/v1/profile`
    - `GET /api/v1/profile/skills`
    - `POST /api/v1/profile/resume`
  - **Target Files:** `src/main/java/com/ai/career/profile/controller/ProfileController.java`
  - **Acceptance Criteria:** Secured with JWT. User can view, update skills/profile data, list skills lookup, and upload resume files.

- [ ] **Task 5.3: Implement Job & Match Controllers (`/api/v1/jobs`)**
  - **Description:** Build `JobController` with endpoints:
    - `GET /api/v1/jobs?minScore={score}`
    - `GET /api/v1/jobs/{id}`
    - `POST /api/v1/jobs/fetch` (admin/manual trigger)
  - **Target Files:** `src/main/java/com/ai/career/job/controller/JobController.java`
  - **Acceptance Criteria:** User can query matched jobs filtered by `minScore`, view detailed description, and view match breakdown.

- [ ] **Task 5.4: Implement Notification Test Controller (`/api/v1/notifications`)**
  - **Description:** Build `NotificationController` with endpoint `POST /api/v1/notifications/test` for sending manual test Telegram alerts.
  - **Target Files:** `src/main/java/com/ai/career/notify/controller/NotificationController.java`
  - **Acceptance Criteria:** Admin/user can trigger test Telegram message and view log status.

- [ ] **Task 5.5: Implement Global Exception Handler & Unified API Error DTO**
  - **Description:** Create `@RestControllerAdvice` handling `MethodArgumentNotValidException`, `BadCredentialsException`, `ResourceNotFoundException`, and generic exceptions, producing consistent `{ error: "...", code: "...", details: [] }` JSON responses.
  - **Target Files:**
    - `src/main/java/com/ai/career/common/exception/GlobalExceptionHandler.java`
    - `src/main/java/com/ai/career/common/dto/ApiErrorResponse.java`
  - **Acceptance Criteria:** All controllers return standardized error JSON schemas with proper HTTP status codes.

- [ ] **Task 5.6: OpenAPI / Swagger Documentation & Verification Tests**
  - **Description:** Add SpringDoc OpenAPI 3 dependency (`springdoc-openapi-starter-webmvc-ui`), annotate controllers with `@Operation`, and write end-to-end integration tests using `@SpringBootTest` and MockMvc / Testcontainers.
  - **Target Files:**
    - `src/main/java/com/ai/career/config/OpenApiConfig.java`
    - `src/test/java/com/ai/career/integration/*Test.java`
  - **Acceptance Criteria:** Swagger UI accessible at `/swagger-ui.html`, and full integration test suite passes via `./gradlew test`.
