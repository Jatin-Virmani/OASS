-- ============================================================================
-- OASS - Online Assignment Submission System
-- MySQL 8 schema. Run once:   mysql -u root -p < schema.sql
-- Start fresh (deletes ALL data):   DROP DATABASE oass;   then run this file again.
-- Conventions: all dates are DATETIME in one timezone (Asia/Kolkata),
-- passwords are stored as SHA-256 hex of (salt + password), marks are DECIMAL(6,2).
-- ============================================================================

CREATE DATABASE IF NOT EXISTS oass CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE oass;

-- ---------------------------------------------------------------------------
-- 1. Academic structure
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sections (
    id          INT          NOT NULL AUTO_INCREMENT,
    course      VARCHAR(100) NOT NULL,              -- e.g. B.Tech CSE
    name        VARCHAR(20)  NOT NULL,              -- e.g. CSE-5D
    batch_year  SMALLINT     NOT NULL,              -- admission year, e.g. 2024
    semester    TINYINT      NOT NULL,              -- current semester
    PRIMARY KEY (id),
    UNIQUE KEY uq_section (course, name, batch_year),
    CONSTRAINT ck_section_sem CHECK (semester BETWEEN 1 AND 8)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS subjects (
    id    INT          NOT NULL AUTO_INCREMENT,
    code  VARCHAR(20)  NOT NULL,                    -- e.g. CS505
    name  VARCHAR(150) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_subject_code (code)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- 2. Users (one row per person) + role details
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id             INT          NOT NULL AUTO_INCREMENT,
    name           VARCHAR(100) NOT NULL,
    email          VARCHAR(120) NOT NULL,
    password_hash  CHAR(64)     NOT NULL,           -- SHA-256 hex of (salt + password)
    salt           VARCHAR(64)  NOT NULL,
    role           ENUM('STUDENT','TEACHER')              NOT NULL,
    status         ENUM('PENDING','ACTIVE','DISABLED')    NOT NULL DEFAULT 'ACTIVE',
    phone          VARCHAR(20)  NULL,
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_user_email (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS students (
    user_id     INT         NOT NULL,
    roll_no     VARCHAR(30) NOT NULL,               -- e.g. 2K24CSUN01099
    section_id  INT         NOT NULL,
    PRIMARY KEY (user_id),
    UNIQUE KEY uq_student_roll (roll_no),
    KEY idx_student_section (section_id),
    CONSTRAINT fk_student_user    FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE,
    CONSTRAINT fk_student_section FOREIGN KEY (section_id) REFERENCES sections(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS teachers (
    user_id      INT          NOT NULL,
    employee_id  VARCHAR(30)  NOT NULL,
    department   VARCHAR(100) NOT NULL,
    PRIMARY KEY (user_id),
    UNIQUE KEY uq_teacher_emp (employee_id),
    CONSTRAINT fk_teacher_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Which teacher teaches which subject to which section (teacher-subject mapping)
CREATE TABLE IF NOT EXISTS teaching (
    id          INT NOT NULL AUTO_INCREMENT,
    teacher_id  INT NOT NULL,
    subject_id  INT NOT NULL,
    section_id  INT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_teaching (teacher_id, subject_id, section_id),
    CONSTRAINT fk_teaching_teacher FOREIGN KEY (teacher_id) REFERENCES teachers(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_teaching_subject FOREIGN KEY (subject_id) REFERENCES subjects(id),
    CONSTRAINT fk_teaching_section FOREIGN KEY (section_id) REFERENCES sections(id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- 3. Assignments
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS assignments (
    id                        INT           NOT NULL AUTO_INCREMENT,
    teacher_id                INT           NOT NULL,
    subject_id                INT           NOT NULL,
    section_id                INT           NOT NULL,           -- one section per assignment
    title                     VARCHAR(200)  NOT NULL,
    description               TEXT          NULL,
    max_marks                 DECIMAL(6,2)  NOT NULL,           -- must equal SUM of rubric criteria
    status                    ENUM('DRAFT','PUBLISHED','CLOSED') NOT NULL DEFAULT 'DRAFT',
    deadline                  DATETIME      NOT NULL,
    allow_late                TINYINT(1)    NOT NULL DEFAULT 0,
    late_penalty_pct_per_day  DECIMAL(5,2)  NOT NULL DEFAULT 0,
    max_late_penalty_pct      DECIMAL(5,2)  NOT NULL DEFAULT 0,
    hard_cutoff               DATETIME      NULL,               -- no uploads after this
    allowed_types             VARCHAR(100)  NOT NULL DEFAULT 'pdf,docx,txt',
    max_file_mb               INT           NOT NULL DEFAULT 10,
    plagiarism_enabled        TINYINT(1)    NOT NULL DEFAULT 1,
    plagiarism_threshold      DECIMAL(5,2)  NOT NULL DEFAULT 70,
    blind_grading             TINYINT(1)    NOT NULL DEFAULT 0,
    created_at                DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_assign_section (section_id, status),
    KEY idx_assign_teacher (teacher_id),
    KEY idx_assign_deadline (deadline),
    CONSTRAINT fk_assign_teacher FOREIGN KEY (teacher_id) REFERENCES teachers(user_id),
    CONSTRAINT fk_assign_subject FOREIGN KEY (subject_id) REFERENCES subjects(id),
    CONSTRAINT fk_assign_section FOREIGN KEY (section_id) REFERENCES sections(id),
    CONSTRAINT ck_assign_marks     CHECK (max_marks > 0),
    CONSTRAINT ck_assign_threshold CHECK (plagiarism_threshold BETWEEN 0 AND 100),
    CONSTRAINT ck_assign_cutoff    CHECK (hard_cutoff IS NULL OR hard_cutoff >= deadline)
) ENGINE=InnoDB;

-- Reference files the teacher attaches ("Download Instructions" button)
CREATE TABLE IF NOT EXISTS assignment_files (
    id             INT          NOT NULL AUTO_INCREMENT,
    assignment_id  INT          NOT NULL,
    original_name  VARCHAR(255) NOT NULL,
    stored_name    VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    KEY idx_afile_assignment (assignment_id),
    CONSTRAINT fk_afile_assignment FOREIGN KEY (assignment_id) REFERENCES assignments(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Rubric: marks are given per criterion; criteria must add up to max_marks (checked in Java)
CREATE TABLE IF NOT EXISTS rubric_criteria (
    id             INT          NOT NULL AUTO_INCREMENT,
    assignment_id  INT          NOT NULL,
    name           VARCHAR(100) NOT NULL,
    max_marks      DECIMAL(6,2) NOT NULL,
    sort_order     TINYINT      NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    KEY idx_rubric_assignment (assignment_id),
    CONSTRAINT fk_rubric_assignment FOREIGN KEY (assignment_id) REFERENCES assignments(id) ON DELETE CASCADE,
    CONSTRAINT ck_rubric_marks CHECK (max_marks > 0)
) ENGINE=InnoDB;

-- Question bank (descriptive types only) and the questions chosen for an assignment
CREATE TABLE IF NOT EXISTS questions (
    id             INT          NOT NULL AUTO_INCREMENT,
    teacher_id     INT          NOT NULL,
    subject_id     INT          NOT NULL,
    question_type  ENUM('SHORT','DESCRIPTIVE','ESSAY') NOT NULL DEFAULT 'DESCRIPTIVE',
    question_text  TEXT         NOT NULL,
    marks          DECIMAL(6,2) NOT NULL,
    difficulty     ENUM('EASY','MEDIUM','HARD') NOT NULL DEFAULT 'MEDIUM',
    topic          VARCHAR(100) NULL,
    PRIMARY KEY (id),
    KEY idx_question_teacher (teacher_id, subject_id),
    CONSTRAINT fk_question_teacher FOREIGN KEY (teacher_id) REFERENCES teachers(user_id),
    CONSTRAINT fk_question_subject FOREIGN KEY (subject_id) REFERENCES subjects(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS assignment_questions (
    assignment_id  INT NOT NULL,
    question_id    INT NOT NULL,
    PRIMARY KEY (assignment_id, question_id),
    CONSTRAINT fk_aq_assignment FOREIGN KEY (assignment_id) REFERENCES assignments(id) ON DELETE CASCADE,
    CONSTRAINT fk_aq_question   FOREIGN KEY (question_id)   REFERENCES questions(id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- 4. Submissions: ONE ROW PER UPLOAD ATTEMPT (version). Highest accepted version is current.
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS submissions (
    id               INT          NOT NULL AUTO_INCREMENT,
    assignment_id    INT          NOT NULL,
    student_id       INT          NOT NULL,
    version_no       INT          NOT NULL,
    upload_status    ENUM('ACCEPTED','REJECTED') NOT NULL DEFAULT 'ACCEPTED',
    original_name    VARCHAR(255) NULL,
    stored_name      VARCHAR(100) NULL,            -- UUID name on disk (NULL for rejected uploads)
    size_bytes       BIGINT       NOT NULL DEFAULT 0,
    sha256           CHAR(64)     NULL,            -- file hash, computed on the server
    receipt_no       VARCHAR(80)  NULL,
    comment_text     VARCHAR(500) NULL,            -- optional student comment
    submitted_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_late          TINYINT(1)   NOT NULL DEFAULT 0,
    rejected_reason  VARCHAR(255) NULL,
    text_content     LONGTEXT     NULL,            -- extracted text for plagiarism (NULL = no text extracted)
    deleted_at       DATETIME     NULL,            -- soft delete
    deleted_reason   VARCHAR(255) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_submission_version (assignment_id, student_id, version_no),
    UNIQUE KEY uq_submission_receipt (receipt_no),
    KEY idx_submission_student (student_id),
    KEY idx_submission_hash (sha256),
    CONSTRAINT fk_sub_assignment FOREIGN KEY (assignment_id) REFERENCES assignments(id),
    CONSTRAINT fk_sub_student    FOREIGN KEY (student_id)    REFERENCES students(user_id)
) ENGINE=InnoDB;

-- Convenience view: the CURRENT (latest accepted, not deleted) submission per student per assignment
CREATE OR REPLACE VIEW current_submissions AS
SELECT s.*
FROM submissions s
JOIN (SELECT assignment_id, student_id, MAX(version_no) AS latest_version
        FROM submissions
       WHERE upload_status = 'ACCEPTED' AND deleted_at IS NULL
       GROUP BY assignment_id, student_id) m
  ON s.assignment_id = m.assignment_id
 AND s.student_id    = m.student_id
 AND s.version_no    = m.latest_version;

-- ---------------------------------------------------------------------------
-- 5. Evaluation (rubric marks) and regrade requests
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS evaluations (
    id             INT          NOT NULL AUTO_INCREMENT,
    submission_id  INT          NOT NULL,
    total          DECIMAL(6,2) NOT NULL DEFAULT 0,   -- sum of rubric scores
    late_penalty   DECIMAL(6,2) NOT NULL DEFAULT 0,
    final_marks    DECIMAL(6,2) NOT NULL DEFAULT 0,   -- total - late_penalty
    feedback       TEXT         NULL,
    status         ENUM('DRAFT','FINALIZED','PUBLISHED') NOT NULL DEFAULT 'DRAFT',
    evaluated_by   INT          NULL,
    evaluated_at   DATETIME     NULL,
    published_at   DATETIME     NULL,                 -- regrade window (7 days) starts here
    PRIMARY KEY (id),
    UNIQUE KEY uq_eval_submission (submission_id),
    CONSTRAINT fk_eval_submission FOREIGN KEY (submission_id) REFERENCES submissions(id),
    CONSTRAINT fk_eval_user       FOREIGN KEY (evaluated_by)  REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS evaluation_scores (
    evaluation_id  INT          NOT NULL,
    criterion_id   INT          NOT NULL,
    marks          DECIMAL(6,2) NOT NULL,
    PRIMARY KEY (evaluation_id, criterion_id),
    CONSTRAINT fk_score_eval      FOREIGN KEY (evaluation_id) REFERENCES evaluations(id) ON DELETE CASCADE,
    CONSTRAINT fk_score_criterion FOREIGN KEY (criterion_id)  REFERENCES rubric_criteria(id),
    CONSTRAINT ck_score_marks CHECK (marks >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS regrade_requests (
    id             INT  NOT NULL AUTO_INCREMENT,
    evaluation_id  INT  NOT NULL,
    student_id     INT  NOT NULL,
    reason         TEXT NOT NULL,
    status         ENUM('REQUESTED','ACCEPTED','REJECTED') NOT NULL DEFAULT 'REQUESTED',
    response       TEXT NULL,
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_regrade_once (evaluation_id, student_id),   -- one request per evaluation
    CONSTRAINT fk_regrade_eval    FOREIGN KEY (evaluation_id) REFERENCES evaluations(id),
    CONSTRAINT fk_regrade_student FOREIGN KEY (student_id)    REFERENCES students(user_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- 6. Deadline extensions
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS extensions (
    id             INT          NOT NULL AUTO_INCREMENT,
    assignment_id  INT          NOT NULL,
    student_id     INT          NULL,                  -- NULL = applies to the whole section
    new_deadline   DATETIME     NOT NULL,
    reason         VARCHAR(255) NULL,
    status         ENUM('REQUESTED','APPROVED','REJECTED') NOT NULL DEFAULT 'APPROVED',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    decided_by     INT          NULL,
    decided_at     DATETIME     NULL,
    PRIMARY KEY (id),
    KEY idx_ext_assignment (assignment_id, student_id),
    CONSTRAINT fk_ext_assignment FOREIGN KEY (assignment_id) REFERENCES assignments(id),
    CONSTRAINT fk_ext_student    FOREIGN KEY (student_id)    REFERENCES students(user_id),
    CONSTRAINT fk_ext_decider    FOREIGN KEY (decided_by)    REFERENCES users(id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- 7. Notifications and reminders
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notifications (
    id          INT          NOT NULL AUTO_INCREMENT,
    user_id     INT          NOT NULL,
    title       VARCHAR(150) NOT NULL,
    message     VARCHAR(500) NULL,
    link        VARCHAR(255) NULL,
    is_read     TINYINT(1)   NOT NULL DEFAULT 0,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_notif_user (user_id, is_read, created_at),
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- One row per reminder already sent. The primary key stops duplicates and, because it
-- contains the deadline, an extended deadline allows a fresh reminder.
CREATE TABLE IF NOT EXISTS reminder_log (
    assignment_id  INT         NOT NULL,
    student_id     INT         NOT NULL,
    stage          VARCHAR(10) NOT NULL,               -- '24H' or '2H'
    deadline       DATETIME    NOT NULL,
    sent_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (assignment_id, student_id, stage, deadline),
    CONSTRAINT fk_rem_assignment FOREIGN KEY (assignment_id) REFERENCES assignments(id) ON DELETE CASCADE,
    CONSTRAINT fk_rem_student    FOREIGN KEY (student_id)    REFERENCES students(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- 8. Plagiarism results and review cases
-- ---------------------------------------------------------------------------
-- One row per compared pair. Always store the smaller submission id in submission_a.
-- Re-running a check should use INSERT ... ON DUPLICATE KEY UPDATE so existing cases stay attached.
CREATE TABLE IF NOT EXISTS similarity_results (
    id             INT          NOT NULL AUTO_INCREMENT,
    assignment_id  INT          NOT NULL,
    submission_a   INT          NOT NULL,
    submission_b   INT          NOT NULL,
    jaccard        DECIMAL(5,2) NOT NULL DEFAULT 0,
    cosine         DECIMAL(5,2) NOT NULL DEFAULT 0,
    score          DECIMAL(5,2) NOT NULL DEFAULT 0,
    risk           ENUM('LOW','MEDIUM','HIGH') NOT NULL DEFAULT 'LOW',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_sim_pair (submission_a, submission_b),
    KEY idx_sim_assignment (assignment_id, score),
    CONSTRAINT fk_sim_assignment FOREIGN KEY (assignment_id) REFERENCES assignments(id),
    CONSTRAINT fk_sim_a FOREIGN KEY (submission_a) REFERENCES submissions(id),
    CONSTRAINT fk_sim_b FOREIGN KEY (submission_b) REFERENCES submissions(id),
    CONSTRAINT ck_sim_order CHECK (submission_a < submission_b)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS plagiarism_cases (
    id             INT  NOT NULL AUTO_INCREMENT,
    similarity_id  INT  NOT NULL,
    status         ENUM('FLAGGED','UNDER_REVIEW','FAIR','CONFIRMED') NOT NULL DEFAULT 'FLAGGED',
    action_taken   ENUM('NONE','WARNING','MARK_DEDUCTION','ZERO')    NOT NULL DEFAULT 'NONE',
    remarks        TEXT NULL,
    reviewed_by    INT  NULL,
    reviewed_at    DATETIME NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_case_similarity (similarity_id),
    CONSTRAINT fk_case_sim      FOREIGN KEY (similarity_id) REFERENCES similarity_results(id) ON DELETE CASCADE,
    CONSTRAINT fk_case_reviewer FOREIGN KEY (reviewed_by)   REFERENCES users(id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- 9. Announcements, password reset, settings, audit log
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS announcements (
    id          INT          NOT NULL AUTO_INCREMENT,
    teacher_id  INT          NOT NULL,
    section_id  INT          NOT NULL,
    title       VARCHAR(150) NOT NULL,
    body        TEXT         NOT NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_announce_section (section_id, created_at),
    CONSTRAINT fk_announce_teacher FOREIGN KEY (teacher_id) REFERENCES teachers(user_id),
    CONSTRAINT fk_announce_section FOREIGN KEY (section_id) REFERENCES sections(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS password_resets (
    id          INT         NOT NULL AUTO_INCREMENT,
    user_id     INT         NOT NULL,
    otp         VARCHAR(10) NOT NULL,                -- 6-digit code
    expires_at  DATETIME    NOT NULL,                -- now + 10 minutes
    used        TINYINT(1)  NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_reset_user (user_id),
    CONSTRAINT fk_reset_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS system_settings (
    setting_key    VARCHAR(50)  NOT NULL,
    setting_value  VARCHAR(255) NOT NULL,
    PRIMARY KEY (setting_key)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS audit_log (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    ts          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id     INT          NULL,                  -- the real person who did it
    role        VARCHAR(10)  NULL,
    action      VARCHAR(60)  NOT NULL,              -- e.g. LOGIN, SUBMIT, MARKS_CHANGED
    entity      VARCHAR(60)  NULL,                  -- e.g. submission:42
    old_value   TEXT         NULL,
    new_value   TEXT         NULL,
    reason      VARCHAR(255) NULL,
    ip          VARCHAR(45)  NULL,
    PRIMARY KEY (id),
    KEY idx_audit_ts (ts),
    KEY idx_audit_user (user_id, ts),
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- 10. Default settings the application reads
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO system_settings (setting_key, setting_value) VALUES
    ('weight_jaccard',    '0.5'),
    ('weight_cosine',     '0.5'),
    ('risk_medium_from',  '40'),
    ('risk_high_from',    '70'),
    ('default_threshold', '70'),
    ('reminder_hours',    '24,2'),
    ('roll_regex',        '^2K\\d{2}[A-Z]{2,6}\\d{5}$');
