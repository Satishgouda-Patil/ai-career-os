# Phase 1 Implementation Plan - AI Career OS

## Overview
This document defines the architectural strategy and technical plan for Phase 1 (MVP) of AI Career OS. The project is built using Java 21, Spring Boot 3.x, MySQL 8, Redis, RabbitMQ, MinIO, and Ollama for local LLM capability.

## Tech Stack & Architectural Decisions
- **Language & JDK:** Java 21 LTS
- **Build Tool:** Gradle (wrapper generated via `gradle wrapper`) / Maven option
- **Framework:** Spring Boot 3.x (Spring Web, Spring Data JPA, Spring Security, Spring AMQP, Spring Actuator)
- **Database:** MySQL 8 with Flyway schema migration
- **Caching & Event Queue:** Redis 7 & RabbitMQ 3.x
- **Storage:** MinIO (S3-compatible object store) for Resume PDFs
- **Local LLM Integration:** Ollama API (`http://localhost:11434`) for local LLM scoring/tailoring stubs
- **Architecture Pattern:** Clean Architecture (Controller -> Service -> Repository) with Lombok & DTO pattern

## Domain Data Model & Database Schema
1. **`users`**: User registration, bcrypt password hash, role (`USER`, `ADMIN`).
2. **`profiles`**: User career details (1:1 with `users`), full name, summary, resume URL.
3. **`skills`** & **`profile_skills`**: Skill entities and N:M mapping with user profiles.
4. **`jobs`**: Fetched job postings from external sources (Jooble, scrapers) with deduplication key `(source, source_job_id)`.
5. **`job_matches`**: Score calculation (0-100) between user profile and job posting.
6. **`notifications`**: Telegram alert delivery tracking (`sent_at`, `delivered`).

## Key Component Architecture
- **Auth & Security:** Stateless JWT authentication using Spring Security filters. Bearer tokens issued at `/api/v1/auth/login`.
- **Profile & Resume Service:** CRUD management for career profiles and resume PDF uploads stored in MinIO.
- **Job Discovery & Ingestion Pipeline:** Automated `@Scheduled` fetchers retrieving listings, deduplicating records, and publishing `JobsFetchedEvent` to RabbitMQ.
- **Match Scoring Engine:** Asynchronous RabbitMQ listener evaluating profile-to-job skill overlap and storing matches in MySQL.
- **Local LLM Integration (Ollama):** HTTP client integration with Ollama local endpoint for fallback/advanced matching and resume tailoring stubs.
- **Notification Service:** Telegram Bot API integration delivering high-match alerts (>80% score).

## Milestones Summary
1. **Infra Setup (Docker/Spring Boot initialization):** Repo init, Gradle wrapper, Docker Compose, Spring Security baseline.
2. **Database & Auth:** Flyway migrations, User/Profile entities, JWT authentication endpoints, repository layer.
3. **Job Fetching & RabbitMQ pipeline:** Jooble API connector, job deduplication, RabbitMQ exchange/queue setup, matching listener.
4. **Local LLM Integration (Ollama):** Ollama client service, prompt templates, match scoring fallback/enhancement, resume tailoring stubs.
5. **REST Controller endpoints:** Standardized `/api/v1` REST APIs, input validation, global exception handling, SpringDoc OpenAPI docs.
