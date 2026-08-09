-- V10__extend_application_executions_schema.sql: Phase 3 Milestone 2 Execution Engine Schema Extensions

ALTER TABLE application_executions ADD COLUMN outcome_status VARCHAR(50) NULL;
ALTER TABLE application_executions ADD COLUMN external_application_id VARCHAR(255) NULL;
ALTER TABLE application_executions ADD COLUMN external_url VARCHAR(1024) NULL;
ALTER TABLE application_executions ADD COLUMN error_code VARCHAR(100) NULL;
ALTER TABLE application_executions ADD COLUMN retryable BOOLEAN DEFAULT FALSE;
