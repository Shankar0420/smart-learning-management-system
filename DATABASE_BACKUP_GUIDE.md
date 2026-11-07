# Galgotias LMS — Database Backup & Disaster Recovery Guide

This guide details best practices and exact commands for creating, automating, and verifying backups of the Galgotias LMS MariaDB / MySQL database, as well as safe restoration procedures.

---

## 1. Backup Strategy Overview

To safeguard institutional academic data, student submissions, and user records:
1. **Pre-Deployment Backup**: Always create a full database snapshot before any major code deployment or migration.
2. **Scheduled Daily Backups**: Automated backups during off-peak hours (e.g. 02:00 AM UTC).
3. **Point-in-Time Recovery**: Retain binary logs when running a dedicated server or manage cloud provider automated daily snapshots (e.g. Aiven, TiDB).

---

## 2. Manual Backup Procedures

### A. Full Backup via `mariadb-dump` / `mysqldump`

Run this command to create a complete SQL dump of the database with table structures, indexes, and all existing data:

```bash
# For local or standard MariaDB / MySQL:
mariadb-dump -h <DB_HOST> -P <DB_PORT> -u <DB_USERNAME> -p \
  --single-transaction \
  --quick \
  --routines \
  --triggers \
  --default-character-set=utf8mb4 \
  <DB_NAME> > backup_lms_$(date +%Y%m%d_%H%M%S).sql
```

#### Explanation of Flags:
- `--single-transaction`: Creates a consistent snapshot of InnoDB tables without locking reads or writes (zero downtime for students/teachers during backup).
- `--quick`: Streams rows directly from the server rather than buffering all rows in memory (prevents out-of-memory errors on large datasets).
- `--routines --triggers`: Preserves database stored procedures, functions, and triggers.
- `--default-character-set=utf8mb4`: Ensures full support for international characters, emojis, and academic symbols.

### B. Backup with SSL / TLS (e.g. Aiven or TiDB Cloud)

When backing up an Aiven or TiDB cloud instance with SSL certificates:

```bash
mariadb-dump -h <AIVEN_HOST> -P <AIVEN_PORT> -u <AIVEN_USER> -p \
  --ssl-ca=/path/to/ca.pem \
  --single-transaction \
  --quick \
  <AIVEN_DB_NAME> | gzip > backup_aiven_lms_$(date +%Y%m%d_%H%M%S).sql.gz
```

---

## 3. Automated Daily Backup Script (Linux / macOS Cron)

Create a backup script at `/usr/local/bin/backup_lms.sh`:

```bash
#!/bin/bash
set -e

# Configuration
BACKUP_DIR="/var/backups/galgotias_lms"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="${BACKUP_DIR}/lms_db_${DATE}.sql.gz"
RETENTION_DAYS=30

# Database credentials (use .my.cnf or environment)
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_USER="${DB_USERNAME:-lms_user}"
DB_NAME="${DB_NAME:-lms_db}"

mkdir -p "$BACKUP_DIR"

echo "[$(date)] Starting backup of ${DB_NAME}..."
mariadb-dump -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" \
  --single-transaction \
  --quick \
  --routines \
  --triggers \
  "$DB_NAME" | gzip > "$BACKUP_FILE"

echo "[$(date)] Backup completed: ${BACKUP_FILE} ($(du -h "$BACKUP_FILE" | cut -f1))"

# Prune backups older than retention period
echo "[$(date)] Cleaning up backups older than ${RETENTION_DAYS} days..."
find "$BACKUP_DIR" -type f -name "lms_db_*.sql.gz" -mtime +$RETENTION_DAYS -delete

echo "[$(date)] Backup process finished successfully."
```

Make it executable and add to crontab:
```bash
chmod +x /usr/local/bin/backup_lms.sh
crontab -e
```
Add line:
```cron
0 2 * * * /usr/local/bin/backup_lms.sh >> /var/log/lms_backup.log 2>&1
```

---

## 4. Backup Verification (Integrity Testing)

A backup that has never been restored cannot be trusted. Verify dumps periodically:

1. Check file size is non-zero:
   ```bash
   ls -lh backup_lms_*.sql.gz
   ```
2. Test that gzip archive is not corrupted:
   ```bash
   gzip -t backup_lms_*.sql.gz
   ```
3. Test restoring into an isolated temporary database:
   ```bash
   # Create temporary test schema
   mariadb -u <USER> -p -e "CREATE DATABASE IF NOT EXISTS lms_backup_test;"

   # Restore into test database
   gunzip -c backup_lms_20261107.sql.gz | mariadb -u <USER> -p lms_backup_test

   # Verify row counts
   mariadb -u <USER> -p -e "SELECT count(*) FROM lms_backup_test.users;"
   mariadb -u <USER> -p -e "SELECT count(*) FROM lms_backup_test.courses;"

   # Clean up test schema
   mariadb -u <USER> -p -e "DROP DATABASE lms_backup_test;"
   ```

---

## 5. Emergency Restoration Procedure

If disaster recovery is required:

### Step 5.1: Stop Active Backend Services
Stop the Spring Boot backend on Render / Koyeb to prevent partial writes while restoration is underway:
```bash
# In hosting dashboard, pause or suspend the web service
```

### Step 5.2: Restore Database from Valid Backup
```bash
# If plain SQL:
mariadb -h <DB_HOST> -P <DB_PORT> -u <DB_USERNAME> -p <DB_NAME> < backup_lms_YYYYMMDD_HHMMSS.sql

# If compressed gzip (.gz):
gunzip -c backup_lms_YYYYMMDD_HHMMSS.sql.gz | mariadb -h <DB_HOST> -P <DB_PORT> -u <DB_USERNAME> -p <DB_NAME>
```

### Step 5.3: Verify User Records
Verify that Admin, Teachers, and Students are present:
```sql
SELECT id, username, email, role, approval_status, is_active FROM users LIMIT 10;
```

### Step 5.4: Restart Backend Services
Restart or unpause the Spring Boot application on Render / Koyeb. Test login at `/pages/login.html`.
