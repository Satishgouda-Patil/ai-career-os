
---

# 4. `Database.md`

```md
# Phase 4 Database Specification

## Principle

Extend the existing MySQL schema using Flyway.

Never modify old migrations.

Create new migrations.

---

# Core Tables

## application_activities

Tracks the application timeline.

Fields:

- id
- application_id
- activity_type
- source
- description
- metadata_json
- confidence
- created_at

---

## application_follow_ups

Fields:

- id
- application_id
- channel
- sequence_number
- scheduled_at
- status
- message_artifact_id
- sent_at
- approved_at
- created_at
- updated_at

Statuses:

- SCHEDULED
- READY
- APPROVAL_REQUIRED
- SENT
- CANCELLED
- FAILED

---

## email_messages

Store normalized email metadata.

Fields:

- id
- provider
- external_message_id
- external_thread_id
- sender
- sender_domain
- subject
- received_at
- classification
- classification_confidence
- application_id
- processed_at
- created_at

Do not store raw email content unless required.

---

## email_classification_results

Fields:

- id
- email_message_id
- classification
- confidence
- extracted_data_json
- model
- created_at

---

## interviews

Fields:

- id
- application_id
- status
- scheduled_at
- timezone
- interview_type
- meeting_url
- interviewer_json
- source_email_id
- created_at
- updated_at

---

## interview_preparations

Fields:

- id
- interview_id
- preparation_json
- model
- version
- created_at
- updated_at

---

# Indexes

Required indexes:

- application_id
- external_message_id
- external_thread_id
- received_at
- classification
- scheduled_at
- status

External message IDs must be unique per provider.

---

# Data Security

Never store:

- email passwords
- OAuth client secrets
- access tokens in plaintext
- refresh tokens in plaintext
- unnecessary sensitive email content

Secrets must use secure configuration/secret storage.