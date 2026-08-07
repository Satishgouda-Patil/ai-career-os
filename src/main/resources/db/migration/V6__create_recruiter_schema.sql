-- V6__create_recruiter_schema.sql: Recruiter Intelligence Schema

CREATE TABLE IF NOT EXISTS companies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    industry VARCHAR(100),
    website VARCHAR(512),
    location VARCHAR(255),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS recruiters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    title VARCHAR(255),
    email VARCHAR(255),
    linkedin_url VARCHAR(512),
    location VARCHAR(255),
    confidence_score INT DEFAULT 85,
    source_provider VARCHAR(50) DEFAULT 'HUNTER_IO',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rec_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    CONSTRAINT uk_company_recruiter_email UNIQUE (company_id, email)
);
