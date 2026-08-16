CREATE TABLE IF NOT EXISTS application_workflow_runs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    workflow_type VARCHAR(50) NOT NULL DEFAULT 'STANDARD_APPLICATION_WORKFLOW',
    status VARCHAR(50) NOT NULL DEFAULT 'RUNNING',
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    correlation_id VARCHAR(100) NOT NULL,
    current_stage VARCHAR(100) NOT NULL,
    failure_code VARCHAR(100) NULL,
    retry_count INT DEFAULT 0,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_awr_application FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
);
