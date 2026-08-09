-- V11__create_application_form_plans_schema.sql: Phase 3 Milestone 4 Form Analysis Schema Migration

CREATE TABLE IF NOT EXISTS application_form_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL UNIQUE,
    readiness_status VARCHAR(50) NOT NULL DEFAULT 'NOT_READY',
    plan_json LONGTEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_afp_application FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
);
