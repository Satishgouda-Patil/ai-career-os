CREATE TABLE IF NOT EXISTS browser_execution_runs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    application_id BIGINT NOT NULL,
    provider_name VARCHAR(50) NOT NULL,
    execution_mode VARCHAR(30) NOT NULL,
    status VARCHAR(40) NOT NULL,
    preview_id VARCHAR(100),
    form_fingerprint VARCHAR(128),
    submission_attempted BOOLEAN NOT NULL DEFAULT FALSE,
    submission_verified BOOLEAN NOT NULL DEFAULT FALSE,
    error_code VARCHAR(80),
    started_at DATETIME,
    completed_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_browser_execution_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_browser_execution_application
        FOREIGN KEY (application_id) REFERENCES applications(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_browser_execution_application
    ON browser_execution_runs(application_id);

CREATE INDEX idx_browser_execution_user
    ON browser_execution_runs(user_id);
