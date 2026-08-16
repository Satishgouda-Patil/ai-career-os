CREATE TABLE IF NOT EXISTS application_follow_ups (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    channel VARCHAR(50) NOT NULL DEFAULT 'EMAIL',
    sequence_number INT NOT NULL DEFAULT 1,
    scheduled_at TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
    message_artifact_id BIGINT NULL,
    follow_up_subject VARCHAR(512),
    follow_up_body TEXT,
    sent_at TIMESTAMP NULL,
    approved_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_afu_application FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
);

CREATE INDEX idx_afu_application_id ON application_follow_ups(application_id);
CREATE INDEX idx_afu_status ON application_follow_ups(status);
CREATE INDEX idx_afu_scheduled_at ON application_follow_ups(scheduled_at);
