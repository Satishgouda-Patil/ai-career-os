-- Phase 5 M4-A: Create Production Integration Vault & Audit Logs Schema
CREATE TABLE IF NOT EXISTS integration_credentials (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider_name VARCHAR(50) NOT NULL,
    encrypted_payload TEXT NOT NULL,
    payload_iv VARCHAR(64) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_credentials_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS integration_audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    application_id BIGINT,
    provider_name VARCHAR(50) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL,
    request_summary TEXT,
    response_summary TEXT,
    execution_time_ms BIGINT,
    error_code VARCHAR(50),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_cred_user_provider ON integration_credentials(user_id, provider_name);
CREATE INDEX idx_audit_user_provider ON integration_audit_logs(user_id, provider_name);
