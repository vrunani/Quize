-- ============================================================
-- QUIZ APPLICATION — COMPLETE SCHEMA
-- Supports all Teacher + Student functionalities
-- ============================================================

-- ==============================
-- 1 USERS TABLE
-- Change: id is now VARCHAR (custom like vrunani@123)
-- ==============================
CREATE TABLE IF NOT EXISTS users ( 
    id            VARCHAR(50)  PRIMARY KEY,           -- e.g. vrunani@123, john@456
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          ENUM('ADMIN','TEACHER','STUDENT') NOT NULL,
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ==============================
-- 2 TESTS TABLE
-- ==============================
CREATE TABLE IF NOT EXISTS tests (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    title             VARCHAR(200) NOT NULL,
    description       TEXT,
    exam_code         VARCHAR(50)  NOT NULL UNIQUE,
    created_by        VARCHAR(50)  NOT NULL,           -- FK -> users.id (VARCHAR now)
    total_marks       INT NOT NULL,
    duration_minutes  INT NOT NULL,
    start_time        TIMESTAMP NULL,
    end_time          TIMESTAMP NULL,
    negative_marking  DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    is_published      BOOLEAN NOT NULL DEFAULT FALSE,
    show_results      BOOLEAN NOT NULL DEFAULT FALSE,
    result_publish_at TIMESTAMP NULL,                  -- NEW: scheduled result publish time
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_tests_user
        FOREIGN KEY (created_by) REFERENCES users(id)
        ON DELETE CASCADE
);

-- ==============================
-- 3 QUESTIONS TABLE
-- ==============================
CREATE TABLE IF NOT EXISTS questions (
    test_id         BIGINT NOT NULL,
    question_number INT    NOT NULL,
    question_text   TEXT   NOT NULL,
    question_type   ENUM('OBJECTIVE','SUBJECTIVE') NOT NULL,
    option1         TEXT,
    option2         TEXT,
    option3         TEXT,
    option4         TEXT,
    correct_option  INT,
    marks           INT NOT NULL,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (test_id, question_number),

    CONSTRAINT fk_questions_test
        FOREIGN KEY (test_id) REFERENCES tests(id)
        ON DELETE CASCADE
);

-- ==============================
-- 4 SUBMISSIONS TABLE
-- ==============================
CREATE TABLE IF NOT EXISTS submissions (
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    test_id      BIGINT      NOT NULL,
    student_id   VARCHAR(50) NOT NULL,               -- FK -> users.id (VARCHAR now)
    submitted_at TIMESTAMP NULL,
    status       ENUM('IN_PROGRESS','SUBMITTED','EVALUATED') NOT NULL DEFAULT 'IN_PROGRESS',
    total_score  DECIMAL(6,2) NOT NULL DEFAULT 0.00,

    CONSTRAINT fk_submissions_test
        FOREIGN KEY (test_id)    REFERENCES tests(id)  ON DELETE CASCADE,
    CONSTRAINT fk_submissions_user
        FOREIGN KEY (student_id) REFERENCES users(id)  ON DELETE CASCADE
);

-- ==============================
-- 5 ANSWERS TABLE
-- ==============================
CREATE TABLE IF NOT EXISTS answers (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    submission_id   BIGINT      NOT NULL,
    test_id         BIGINT      NOT NULL,
    question_number INT         NOT NULL,
    selected_option INT,
    text_answer     TEXT,
    marks_awarded   DECIMAL(6,2) DEFAULT 0.00,
    status          ENUM('AUTO','PENDING','CHECKED') DEFAULT 'PENDING',
    checked_by      VARCHAR(50),                      -- FK -> users.id (VARCHAR now)
    checked_at      TIMESTAMP NULL,

    CONSTRAINT fk_answers_submission
        FOREIGN KEY (submission_id) REFERENCES submissions(id) ON DELETE CASCADE,
    CONSTRAINT fk_answers_question
        FOREIGN KEY (test_id, question_number) REFERENCES questions(test_id, question_number) ON DELETE CASCADE,
    CONSTRAINT fk_answers_checker
        FOREIGN KEY (checked_by) REFERENCES users(id) ON DELETE SET NULL
);

-- ==============================
-- 6 NOTIFICATIONS TABLE  (NEW -- for announcements & alerts)
-- ==============================
CREATE TABLE IF NOT EXISTS notifications (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id    VARCHAR(50)  NOT NULL,
    title      VARCHAR(200) NOT NULL,
    message    TEXT NOT NULL,
    is_read    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);