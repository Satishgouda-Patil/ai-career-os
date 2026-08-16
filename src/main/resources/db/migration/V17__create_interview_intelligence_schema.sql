CREATE TABLE IF NOT EXISTS interviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    interview_type VARCHAR(100) NOT NULL DEFAULT 'GENERAL',
    scheduled_at TIMESTAMP NULL,
    timezone VARCHAR(50),
    meeting_url VARCHAR(512),
    interviewer_name VARCHAR(255),
    interviewer_title VARCHAR(255),
    status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_int_application FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE
);

CREATE INDEX idx_int_application_id ON interviews(application_id);
CREATE INDEX idx_int_status ON interviews(status);
CREATE INDEX idx_int_scheduled_at ON interviews(scheduled_at);

CREATE TABLE IF NOT EXISTS interview_preparations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    interview_id BIGINT NOT NULL,
    company_overview_json TEXT,
    role_focus_json TEXT,
    candidate_talking_points_json TEXT,
    sample_questions_json TEXT,
    questions_to_ask_json TEXT,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ip_interview FOREIGN KEY (interview_id) REFERENCES interviews(id) ON DELETE CASCADE
);

CREATE INDEX idx_ip_interview_id ON interview_preparations(interview_id);

CREATE TABLE IF NOT EXISTS mock_interview_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    interview_id BIGINT NOT NULL,
    question TEXT NOT NULL,
    question_category VARCHAR(100) DEFAULT 'TECHNICAL',
    candidate_answer TEXT,
    score INT NULL,
    feedback TEXT,
    improved_answer TEXT,
    evaluated_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mis_interview FOREIGN KEY (interview_id) REFERENCES interviews(id) ON DELETE CASCADE
);

CREATE INDEX idx_mis_interview_id ON mock_interview_sessions(interview_id);
