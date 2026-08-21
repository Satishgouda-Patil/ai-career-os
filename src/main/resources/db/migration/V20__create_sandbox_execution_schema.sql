CREATE TABLE IF NOT EXISTS sandbox_execution_runs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    execution_mode VARCHAR(30) NOT NULL,
    status VARCHAR(40) NOT NULL,
    fields_detected INT NOT NULL DEFAULT 0,
    fields_mapped INT NOT NULL DEFAULT 0,
    fields_verified INT NOT NULL DEFAULT 0,
    fields_require_review INT NOT NULL DEFAULT 0,
    submission_simulated BOOLEAN NOT NULL DEFAULT FALSE,
    submission_verified BOOLEAN NOT NULL DEFAULT FALSE,
    real_submission_attempted BOOLEAN NOT NULL DEFAULT FALSE,
    started_at DATETIME NOT NULL,
    completed_at DATETIME NULL,
    error_code VARCHAR(80) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_sandbox_application
        FOREIGN KEY (application_id)
        REFERENCES applications(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_sandbox_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);
