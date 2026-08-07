-- V7__create_email_schema.sql: Communication Intelligence Schema

CREATE TABLE IF NOT EXISTS email_drafts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    recruiter_id BIGINT,
    version INT NOT NULL DEFAULT 1,
    subject VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    followup TEXT,
    linkedin_message TEXT,
    status VARCHAR(50) DEFAULT 'GENERATED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ed_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ed_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    CONSTRAINT fk_ed_recruiter FOREIGN KEY (recruiter_id) REFERENCES recruiters(id) ON DELETE SET NULL
);
