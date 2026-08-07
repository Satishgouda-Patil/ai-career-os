-- V5__create_cover_letter_schema.sql: Cover Letter Intelligence Schema

CREATE TABLE IF NOT EXISTS cover_letters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    version INT NOT NULL DEFAULT 1,
    tone VARCHAR(50) DEFAULT 'Professional',
    status VARCHAR(50) DEFAULT 'GENERATED',
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cl_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_cl_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE
);
