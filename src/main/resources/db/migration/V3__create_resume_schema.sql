-- V3__create_resume_schema.sql: Resume Intelligence Schema Migration

CREATE TABLE IF NOT EXISTS resume_templates (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS resume_versions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    job_id BIGINT,
    template_name VARCHAR(100) DEFAULT 'MODERN',
    version INT NOT NULL,
    status VARCHAR(50) DEFAULT 'GENERATED',
    content_json TEXT NOT NULL,
    pdf_url VARCHAR(512),
    docx_url VARCHAR(512),
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rv_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_rv_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS resume_analyses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resume_version_id BIGINT NOT NULL,
    overall_score INT NOT NULL,
    keyword_score INT NOT NULL,
    format_score INT NOT NULL,
    readability_score INT NOT NULL,
    missing_keywords TEXT,
    recommendations TEXT,
    analyzed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ra_resume FOREIGN KEY (resume_version_id) REFERENCES resume_versions(id) ON DELETE CASCADE
);

-- Seed Initial Resume Template
INSERT IGNORE INTO resume_templates (name, description) VALUES
('MODERN', 'Modern single-column ATS resume layout with clean typography'),
('EXECUTIVE', 'Professional corporate executive two-column resume template');
