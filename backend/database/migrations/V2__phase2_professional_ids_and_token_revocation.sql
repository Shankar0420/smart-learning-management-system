-- =========================================================================
-- Phase 2 Additive Database Migration
-- Professional IDs, Sequence Counter, and Server-Side JWT Token Revocation
-- Database: lms_db
-- =========================================================================

USE lms_db;

-- 1. Create atomic sequence counter table
CREATE TABLE IF NOT EXISTS id_sequences (
    id VARCHAR(50) PRIMARY KEY,
    current_val BIGINT NOT NULL DEFAULT 0,
    updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Add additive columns to users table (if not already existing)
SET @dbname = DATABASE();
SET @tablename = "users";

-- Add professional_id
SET @columnname = "professional_id";
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  "SELECT 1",
  "ALTER TABLE users ADD COLUMN professional_id VARCHAR(50) UNIQUE AFTER employee_id;"
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Add enrollment_number
SET @columnname = "enrollment_number";
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  "SELECT 1",
  "ALTER TABLE users ADD COLUMN enrollment_number VARCHAR(50) AFTER professional_id;"
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Add department
SET @columnname = "department";
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  "SELECT 1",
  "ALTER TABLE users ADD COLUMN department VARCHAR(100) AFTER enrollment_number;"
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Add token_version
SET @columnname = "token_version";
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname)
  ) > 0,
  "SELECT 1",
  "ALTER TABLE users ADD COLUMN token_version INT NOT NULL DEFAULT 1 AFTER active;"
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 3. Initialize sequence seeds for year 2026
INSERT INTO id_sequences (id, current_val, updated_at)
VALUES ('TEA_2026', 1, NOW())
ON DUPLICATE KEY UPDATE id=id;

INSERT INTO id_sequences (id, current_val, updated_at)
VALUES ('STU_2026', 2, NOW())
ON DUPLICATE KEY UPDATE id=id;

-- 4. Backfill existing seed accounts with professional IDs if null
UPDATE users SET professional_id = 'GUL-TEA-2026-001', department = 'School of Computing Science & Engineering' WHERE id = 2 AND (professional_id IS NULL OR professional_id = '');
UPDATE users SET professional_id = 'GUL-STU-2026-001', enrollment_number = 'ENR2026001', department = 'Computer Science' WHERE id = 3 AND (professional_id IS NULL OR professional_id = '');
UPDATE users SET professional_id = 'GUL-STU-2026-002', enrollment_number = 'ENR2026002', department = 'Information Technology' WHERE id = 4 AND (professional_id IS NULL OR professional_id = '');

