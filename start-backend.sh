#!/bin/bash
# ==============================================================================
# Galgotias LMS — Start Backend Script (macOS / Local)
# ==============================================================================

set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DIR"

echo "========================================="
echo "  Starting Galgotias LMS Backend"
echo "========================================="

# Check MySQL status
if brew services list 2>/dev/null | grep -q "mysql.*started"; then
    echo "[OK] Local MySQL service is running."
else
    echo "[INFO] Starting local MySQL service via Homebrew..."
    brew services start mysql 2>/dev/null || echo "[WARN] Could not start MySQL via brew. Ensure MySQL is listening on port 3306."
fi

# Check if port 8080 is already in use and free it
if lsof -ti :8080 >/dev/null 2>&1; then
    OLD_PID=$(lsof -ti :8080 | tr '\n' ' ')
    echo "[INFO] Port 8080 is currently occupied by PID(s): $OLD_PID."
    echo "[INFO] Stopping existing instance to prevent port conflict..."
    ./stop-lms.sh >/dev/null 2>&1 || true
    sleep 1
fi

echo "[INFO] Launching Spring Boot Backend on http://localhost:8080..."
cd backend
mvn spring-boot:run

