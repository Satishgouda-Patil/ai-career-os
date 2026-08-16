CREATE TABLE IF NOT EXISTS email_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider VARCHAR(50) NOT NULL DEFAULT 'SIMULATED',
    external_message_id VARCHAR(255) NOT NULL,
    external_thread_id VARCHAR(255),
    sender VARCHAR(255) NOT NULL,
    sender_domain VARCHAR(255),
    subject VARCHAR(512),
    body_snippet TEXT,
    received_at TIMESTAMP NULL,
    classification VARCHAR(100),
    classification_confidence DOUBLE,
    application_id BIGINT NULL,
    processed_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_em_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_em_application FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE SET NULL,
    CONSTRAINT uk_provider_external_msg UNIQUE (provider, external_message_id)
);

CREATE INDEX idx_em_user_id ON email_messages(user_id);
CREATE INDEX idx_em_app_id ON email_messages(application_id);
CREATE INDEX idx_em_classification ON email_messages(classification);
CREATE INDEX idx_em_external_thread ON email_messages(external_thread_id);

CREATE TABLE IF NOT EXISTS email_classification_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email_message_id BIGINT NOT NULL,
    classification VARCHAR(100) NOT NULL,
    confidence DOUBLE NOT NULL,
    extracted_data_json TEXT,
    model VARCHAR(100) DEFAULT 'HEURISTIC_RULE_ENGINE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ecr_email_msg FOREIGN KEY (email_message_id) REFERENCES email_messages(id) ON DELETE CASCADE
);

CREATE INDEX idx_ecr_email_msg_id ON email_classification_results(email_message_id);
