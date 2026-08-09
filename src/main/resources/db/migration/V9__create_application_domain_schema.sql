-- V9__create_application_domain_schema.sql: Phase 3 Milestone 1 Application Domain Schema Migration

CREATE TABLE IF NOT EXISTS applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    workspace_id BIGINT,
    resume_version_id BIGINT,
    cover_letter_id BIGINT,
    status VARCHAR(50) NOT NULL DEFAULT 'DISCOVERED',
    application_method VARCHAR(50) DEFAULT 'MANUAL',
    match_score DECIMAL(5,2),
    ats_score DECIMAL(5,2),
    recommendation VARCHAR(50),
    application_url VARCHAR(1024),
    provider_name VARCHAR(100),
    provider_application_id VARCHAR(255),
    automation_level VARCHAR(50) DEFAULT 'LEVEL_1',
    approval_required BOOLEAN DEFAULT TRUE,
    started_at TIMESTAMP NULL,
    submitted_at TIMESTAMP NULL,
    verified_at TIMESTAMP NULL,
    closed_at TIMESTAMP NULL,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_app_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_app_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    CONSTRAINT fk_app_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE SET NULL,
    CONSTRAINT fk_app_resume FOREIGN KEY (resume_version_id) REFERENCES resume_versions(id) ON DELETE SET NULL,
    CONSTRAINT fk_app_cover FOREIGN KEY (cover_letter_id) REFERENCES cover_letters(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS application_state_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    from_status VARCHAR(50),
    to_status VARCHAR(50) NOT NULL,
    reason VARCHAR(512),
    trigger_type VARCHAR(50) DEFAULT 'SYSTEM',
    actor_type VARCHAR(50) DEFAULT 'SYSTEM',
    actor_id BIGINT,
    correlation_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ash_app FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS application_executions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    provider_name VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'READY',
    execution_logs TEXT,
    error_message TEXT,
    started_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ae_app FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS application_approvals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    reason VARCHAR(512),
    approved_by BIGINT NOT NULL,
    approved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_app_approval_app FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE,
    CONSTRAINT fk_app_approval_user FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE CASCADE
);

ALTER TABLE workspaces ADD COLUMN application_id BIGINT NULL;
ALTER TABLE workspaces ADD CONSTRAINT fk_ws_application FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE SET NULL;
