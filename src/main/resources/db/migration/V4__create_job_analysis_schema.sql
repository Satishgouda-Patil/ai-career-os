-- V4__create_job_analysis_schema.sql: Job Intelligence & Skill Gap Analysis Schema

CREATE TABLE IF NOT EXISTS job_analyses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id BIGINT NOT NULL UNIQUE,
    summary TEXT,
    responsibilities TEXT,
    required_skills TEXT,
    preferred_skills TEXT,
    salary_range VARCHAR(100),
    work_model VARCHAR(50),
    seniority_level VARCHAR(50),
    match_score INT NOT NULL,
    recommendation_status VARCHAR(50) DEFAULT 'APPLY',
    analyzed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ja_job FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS job_missing_skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_analysis_id BIGINT NOT NULL,
    skill_name VARCHAR(100) NOT NULL,
    priority VARCHAR(50) DEFAULT 'HIGH',
    learning_suggestion VARCHAR(512),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_jms_analysis FOREIGN KEY (job_analysis_id) REFERENCES job_analyses(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS job_recommendations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_analysis_id BIGINT NOT NULL UNIQUE,
    decision VARCHAR(50) NOT NULL,
    confidence INT NOT NULL,
    rationale TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_jr_analysis FOREIGN KEY (job_analysis_id) REFERENCES job_analyses(id) ON DELETE CASCADE
);
