-- Phase 3 Migration: Study Materials, Assignments File Attachments, and Password Reset Tokens
-- Safe, incremental, non-destructive migration for Galgotias LMS

-- 1. Study Materials Table for PPT, PPTX, and PDF uploads
CREATE TABLE IF NOT EXISTS study_materials (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(1000) NULL,
    course_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NULL,
    file_size BIGINT NOT NULL DEFAULT 0,
    file_type VARCHAR(20) NOT NULL, -- 'PDF', 'PPT', 'PPTX'
    file_path VARCHAR(500) NOT NULL,
    uploaded_by BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_materials_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT fk_materials_teacher FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Add Supporting Document columns to Assignments table
SET @dbname = DATABASE();

SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'assignments' AND COLUMN_NAME = 'attachment_file_name'
);
SET @sql = IF(@col_exists = 0, 'ALTER TABLE assignments ADD COLUMN attachment_file_name VARCHAR(255) NULL AFTER max_marks', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'assignments' AND COLUMN_NAME = 'attachment_original_name'
);
SET @sql = IF(@col_exists = 0, 'ALTER TABLE assignments ADD COLUMN attachment_original_name VARCHAR(255) NULL AFTER attachment_file_name', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'assignments' AND COLUMN_NAME = 'attachment_type'
);
SET @sql = IF(@col_exists = 0, 'ALTER TABLE assignments ADD COLUMN attachment_type VARCHAR(50) NULL AFTER attachment_original_name', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'assignments' AND COLUMN_NAME = 'attachment_size'
);
SET @sql = IF(@col_exists = 0, 'ALTER TABLE assignments ADD COLUMN attachment_size BIGINT NULL AFTER attachment_type', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'assignments' AND COLUMN_NAME = 'attachment_path'
);
SET @sql = IF(@col_exists = 0, 'ALTER TABLE assignments ADD COLUMN attachment_path VARCHAR(500) NULL AFTER attachment_size', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 3. Add Submission File columns to Assignment Submissions table
SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'assignment_submissions' AND COLUMN_NAME = 'submission_file_name'
);
SET @sql = IF(@col_exists = 0, 'ALTER TABLE assignment_submissions ADD COLUMN submission_file_name VARCHAR(255) NULL AFTER answer_text', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'assignment_submissions' AND COLUMN_NAME = 'submission_original_name'
);
SET @sql = IF(@col_exists = 0, 'ALTER TABLE assignment_submissions ADD COLUMN submission_original_name VARCHAR(255) NULL AFTER submission_file_name', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'assignment_submissions' AND COLUMN_NAME = 'submission_file_type'
);
SET @sql = IF(@col_exists = 0, 'ALTER TABLE assignment_submissions ADD COLUMN submission_file_type VARCHAR(50) NULL AFTER submission_original_name', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'assignment_submissions' AND COLUMN_NAME = 'submission_file_size'
);
SET @sql = IF(@col_exists = 0, 'ALTER TABLE assignment_submissions ADD COLUMN submission_file_size BIGINT NULL AFTER submission_file_type', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists = (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'assignment_submissions' AND COLUMN_NAME = 'submission_file_path'
);
SET @sql = IF(@col_exists = 0, 'ALTER TABLE assignment_submissions ADD COLUMN submission_file_path VARCHAR(500) NULL AFTER submission_file_size', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Allow answer_text to be nullable when student uploads a file
ALTER TABLE assignment_submissions MODIFY COLUMN answer_text TEXT NULL;

-- 4. Password Reset Tokens Table
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expiry_date DATETIME NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reset_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_reset_token (token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

