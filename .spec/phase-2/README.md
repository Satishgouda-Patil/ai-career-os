# AI Career OS
## Phase 2 — AI Career Intelligence

Version: 2.0

Status: Design

Author: Chief Architect

---

# Purpose

Phase 2 transforms AI Career OS from a Job Discovery Platform into an AI Career Intelligence Platform.

Phase 1 successfully introduced:

- Authentication
- User Profiles
- Resume Storage
- Job Discovery
- Job Matching
- Ollama Integration
- Notifications
- RabbitMQ
- REST APIs
- Docker Infrastructure

Phase 2 introduces intelligence.

Instead of simply finding jobs, the platform will understand jobs, understand candidates, generate personalized application assets, and present everything inside a unified AI Workspace.

No automatic application submission will occur during this phase.

Automation belongs to Phase 3.

---

# Product Vision

Build the world's best AI Career Operating System.

The system should eventually become an AI employee working on behalf of the candidate.

The user should never need to manually:

- Tailor resumes
- Write cover letters
- Search recruiters
- Write cold emails

Instead, AI prepares everything.

The user simply reviews and approves.

---

# Phase 2 Goals

Primary Goals

✓ Understand every discovered job

✓ Generate ATS optimized resumes

✓ Generate personalized cover letters

✓ Build recruiter intelligence

✓ Generate cold emails

✓ Build AI Workspace

✓ Improve match quality

✓ Improve interview probability

---

# Non Goals

Phase 2 WILL NOT

❌ Auto apply

❌ Browser automation

❌ Interview preparation

❌ Follow-up automation

❌ Email synchronization

❌ Gmail integration

❌ LinkedIn automation

Those belong to Phase 3.

---

# Technology Stack

Backend

Java 21

Spring Boot 3

MySQL 8

Redis

RabbitMQ

Flyway

MinIO

Spring Security

JWT

OpenAPI

Docker

Ollama

Frontend

(To be implemented later)

React

TypeScript

Material UI

---

# Architecture Principles

The application follows

Clean Architecture

DDD

Event Driven Architecture

Specification Driven Development

Every AI feature must pass through:

Context Builder

↓

Prompt Manager

↓

LLM

↓

Validator

↓

Persistence

Never directly call the LLM.

---

# Folder Structure

.spec/

phase-2/

README.md

PRD.md

Architecture.md

Database.md

API.md

AI.md

PromptLibrary.md

UX.md

Events.md

Tasks.md

Acceptance.md

CodingStandards.md

Testing.md

Roadmap.md

---

# Development Principles

Never hardcode prompts

Never expose raw LLM responses

Everything version controlled

Everything testable

Everything observable

Everything documented

---

# Definition of Done

Every feature must include

Domain

Repository

Service

REST API

DTO

Mapper

Tests

Documentation

Events

Logging

Validation

OpenAPI

---

# Milestones

Milestone 1

Resume Intelligence

Milestone 2

Job Intelligence

Milestone 3

Cover Letter Intelligence

Milestone 4

Recruiter Intelligence

Milestone 5

Communication Intelligence

Milestone 6

AI Workspace

---

# Coding Rules

No business logic inside controllers

No SQL inside services

Repositories only access database

AI only through AI Orchestrator

Prompt files live under resources/prompts

All prompts versioned

Every AI output validated

Every exception logged

Everything covered by integration tests

---

# Expected Outcome

At the end of Phase 2

The user opens any job.

The system automatically prepares

✓ Resume

✓ ATS Analysis

✓ Cover Letter

✓ Recruiter

✓ Cold Email

✓ Recommendation

inside one workspace.

The user reviews everything before Phase 3 introduces automation.
