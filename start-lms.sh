#!/bin/bash
# ==============================================================================
# Galgotias LMS — Start Complete LMS Script (macOS / Local)
# ==============================================================================

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DIR"

echo "=========================================================="
echo "  Starting Complete Galgotias LMS Local Environment"
echo "=========================================================="

# 1. Ensure MySQL is running
if brew services list 2>/dev/null | grep -q "mysql.*started"; then
    echo "[1/4] Local MySQL service is running."
else
    echo "[1/4] Starting local MySQL service via Homebrew..."
    brew services start mysql 2>/dev/null || echo "[WARN] Ensure MySQL is running on port 3306."
fi

# 2. Check and start backend if not already running
if lsof -ti :8080 >/dev/null 2>&1; then
    echo "[2/4] Backend is already running on http://localhost:8080 (PID: $(lsof -ti :8080))."
else
    echo "[2/4] Starting Spring Boot Backend in background..."
    mvn -f backend/pom.xml spring-boot:run > backend.log 2>&1 &
    BACKEND_PID=$!
    echo "      Backend PID: $BACKEND_PID (logs streaming to backend.log)"

    echo "      Waiting for backend to become healthy on port 8080..."
    for i in {1..30}; do
        if curl -s http://localhost:8080/api/health 2>/dev/null | grep -q '"status":"UP"'; then
            echo "      [OK] Backend is UP and connected to local database!"
            break
        fi
        sleep 1
    done
fi

# 3. Check and start frontend if not already running
if lsof -ti :5500 >/dev/null 2>&1; then
    echo "[3/4] Frontend is already running on http://localhost:5500 (PID: $(lsof -ti :5500))."
else
    echo "[3/4] Starting Frontend server on http://localhost:5500..."
    python3 -m http.server 5500 --directory frontend > frontend.log 2>&1 &
    FRONTEND_PID=$!
    echo "      Frontend PID: $FRONTEND_PID (logs streaming to frontend.log)"
fi

# 4. Open in browser
echo "[4/4] Opening LMS in your default browser..."
sleep 1
open "http://localhost:5500" 2>/dev/null || true

echo ""
echo "=========================================================="
echo "  Galgotias LMS is now RUNNING LOCALLY!"
echo "=========================================================="
echo "  Frontend URL: http://localhost:5500"
echo "  Backend API:  http://localhost:8080"
echo "  Health Check: http://localhost:8080/api/health"
echo ""
echo "  Default Accounts:"
echo "  - Admin:   admin@lms.com    / admin123"
echo "  - Teacher: teacher@lms.com  / teacher123"
echo "  - Student: student1@lms.com / student123"
echo ""
echo "  To stop the application, run: ./stop-lms.sh"
echo "=========================================================="

