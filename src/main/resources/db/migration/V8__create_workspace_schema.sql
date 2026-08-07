-- V8__create_workspace_schema.sql: AI Workspace Schema Migration

CREATE TABLE IF NOT EXISTS workspaces (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    status VARCHAR(50) DEFAULT 'BUILDING',
    approved BOOLEAN DEFAULT FALSE,
    rejected BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_ws_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ws_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_job_workspace UNIQUE (user_id, job_id)
);
