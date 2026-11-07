# Galgotias LMS — Database Migration & Schema Guide

This guide describes the database schema, version history, execution steps, and non-destructive verification procedures for the Galgotias LMS database (MariaDB / MySQL).

---

## 1. Safety Principles & Non-Destructive Guarantee

> [!CAUTION]
> **Production Safety Rule**:
> - NEVER run `DROP DATABASE`, `DROP TABLE`, or `TRUNCATE TABLE` in production.
> - NEVER delete user accounts, administrator records, courses, or student enrollment histories.
> - All migrations must be idempotent (safe to run multiple times without causing errors or duplicate columns).

All migration scripts in `backend/database/migrations/` adhere to these rules:
1. They use dynamic SQL with `INFORMATION_SCHEMA.COLUMNS` queries to verify column absence before altering tables.
2. They use `CREATE TABLE IF NOT EXISTS` for all newly introduced entities.
3. Existing primary keys, foreign key constraints, and user records remain completely untouched.

---

## 2. Migration Version History & Architecture

```
+-----------------------------------------------------------------------------------+
| BASELINE: schema.sql                                                              |
| Tables: users, roles, user_roles, courses, enrollments, quizzes, questions,       |
|         quiz_attempts, student_answers, assignments, assignment_submissions       |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
| PHASE 2: V2__phase2_professional_ids_and_token_revocation.sql                    |
| New Table: id_sequences (atomic sequence allocation for concurrency safety)       |
| Added to users: professional_id (UNIQUE), enrollment_number, department,          |
|                 phone_number, approval_status, rejection_reason, is_active,       |
|                 token_version (server-side JWT revocation counter)               |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
| PHASE 3: V3__phase3_study_materials_and_features.sql                              |
| New Table: study_materials (PPT/PPTX/PDF upload metadata and tracking)             |
| New Table: password_reset_tokens (cryptographic tokens for secure password reset) |
| Added to quizzes: is_published (draft vs live quiz control)                       |
| Added to assignments: attachment_filename, attachment_path, max_score              |
| Added to assignment_submissions: original_filename, file_size, score, feedback    |
+-----------------------------------------------------------------------------------+
```

---

## 3. Step-by-Step Production Migration Instructions

### Option A: Using Command-Line Client (MariaDB / MySQL CLI)

#### Step 3.1: Connect to the Target Database
```bash
mariadb -h <DB_HOST> -P <DB_PORT> -u <DB_USER> -p <DB_NAME>
# or for MySQL
mysql -h <DB_HOST> -P <DB_PORT> -u <DB_USER> -p <DB_NAME>
```

#### Step 3.2: For New / Fresh Installations (First-time deploy only)
If the database is empty:
```bash
mariadb -h <DB_HOST> -P <DB_PORT> -u <DB_USER> -p <DB_NAME> < backend/database/schema.sql
```

#### Step 3.3: Apply Phase 2 Migration (Professional IDs & Security)
```bash
mariadb -h <DB_HOST> -P <DB_PORT> -u <DB_USER> -p <DB_NAME> < backend/database/migrations/V2__phase2_professional_ids_and_token_revocation.sql
```

#### Step 3.4: Apply Phase 3 Migration (Study Materials & Password Reset)
```bash
mariadb -h <DB_HOST> -P <DB_PORT> -u <DB_USER> -p <DB_NAME> < backend/database/migrations/V3__phase3_study_materials_and_features.sql
```

---

### Option B: Using GUI Client (DBeaver, phpMyAdmin, HeidiSQL, or Cloud Console)

1. Connect to your database instance (e.g. Aiven Console, TiDB Cloud SQL Editor, or DBeaver).
2. Open SQL Editor.
3. If existing database:
   - Copy contents of `backend/database/migrations/V2__phase2_professional_ids_and_token_revocation.sql` -> Execute.
   - Copy contents of `backend/database/migrations/V3__phase3_study_materials_and_features.sql` -> Execute.
4. Verify execution output (all statements should return Success or Warnings if columns already exist).

---

## 4. Post-Migration Verification Queries

Execute these verification queries to confirm that all tables and columns were created properly without data loss:

```sql
-- 1. Verify all expected tables exist
SELECT TABLE_NAME, TABLE_ROWS
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = DATABASE()
ORDER BY TABLE_NAME;

-- Expected tables:
-- assignments
-- assignment_submissions
-- courses
-- enrollments
-- id_sequences
-- password_reset_tokens
-- questions
-- quiz_attempts
-- quizzes
-- roles
-- student_answers
-- study_materials
-- user_roles
-- users

-- 2. Verify new columns in users table
SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'users'
  AND COLUMN_NAME IN ('professional_id', 'approval_status', 'is_active', 'token_version', 'rejection_reason');

-- 3. Verify sequence table is initialized
SELECT * FROM id_sequences;

-- 4. Check existing admin record is preserved
SELECT id, username, email, professional_id, approval_status, is_active
FROM users
WHERE username = 'admin' OR id = 1;
```

---

## 5. Rollback & Emergency Recovery

Because migrations are strictly additive:
- If a deployment needs to be rolled back to an older backend version, the newly added columns will simply be ignored by older entity mappings.
- No dropping of columns or tables is necessary or recommended.
- Always retain a pre-migration database snapshot following the procedures in `DATABASE_BACKUP_GUIDE.md`.
