CREATE TABLE IF NOT EXISTS browser_execution_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    execution_run_id BIGINT,
    application_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    tenant_id BIGINT,
    event_type VARCHAR(60) NOT NULL,
    event_status VARCHAR(40) NOT NULL,
    sanitized_reason TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_execution_event_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_execution_event_application
        FOREIGN KEY (application_id) REFERENCES applications(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_execution_event_run ON browser_execution_events(execution_run_id);
CREATE INDEX idx_execution_event_app ON browser_execution_events(application_id);
CREATE INDEX idx_execution_event_user ON browser_execution_events(user_id);
CREATE INDEX idx_execution_event_type ON browser_execution_events(event_type);
