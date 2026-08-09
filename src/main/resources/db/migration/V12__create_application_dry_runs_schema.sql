-- V12__create_application_dry_runs_schema.sql: Phase 3 Milestone 5 Application Dry Run & Validation Schema Migration

CREATE TABLE IF NOT EXISTS application_dry_runs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    run_id VARCHAR(64) NOT NULL UNIQUE,
    validation_status VARCHAR(50) NOT NULL,
    readiness_status VARCHAR(50) NOT NULL,
    dry_run_report_json LONGTEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_adr_application FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
);
