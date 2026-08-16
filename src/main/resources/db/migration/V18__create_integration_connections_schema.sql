-- Phase 5 M2: Create Integration Connections Schema
CREATE TABLE IF NOT EXISTS integration_connections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    provider VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DISCONNECTED',
    external_account_id VARCHAR(100),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_integration_connections_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_integration_user_provider ON integration_connections(user_id, provider);
