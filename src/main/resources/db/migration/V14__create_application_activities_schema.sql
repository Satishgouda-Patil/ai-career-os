CREATE TABLE IF NOT EXISTS application_activities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    activity_type VARCHAR(100) NOT NULL,
    source VARCHAR(50) NOT NULL DEFAULT 'SYSTEM',
    description VARCHAR(512),
    metadata_json TEXT,
    confidence DOUBLE DEFAULT 1.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_aa_application FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
);

CREATE INDEX idx_aa_app_id ON application_activities(application_id);
CREATE INDEX idx_aa_activity_type ON application_activities(activity_type);
