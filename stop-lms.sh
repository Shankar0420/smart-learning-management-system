#!/bin/bash
# ==============================================================================
# Galgotias LMS — Stop LMS Script (macOS / Local)
# ==============================================================================

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DIR"

echo "========================================="
echo "  Stopping Galgotias LMS Local Services"
echo "========================================="

# 1. Stop backend on port 8080
BACKEND_PIDS=$(lsof -ti :8080 2>/dev/null || true)
if [ -n "$BACKEND_PIDS" ]; then
    echo "[INFO] Stopping backend process (PID: $BACKEND_PIDS)..."
    kill $BACKEND_PIDS 2>/dev/null || true
    sleep 1
    if lsof -ti :8080 >/dev/null 2>&1; then
        kill -9 $(lsof -ti :8080) 2>/dev/null || true
    fi
    pkill -f "mvn.*spring-boot:run" 2>/dev/null || true
    echo "[OK] Backend stopped."
else
    pkill -f "mvn.*spring-boot:run" 2>/dev/null || true
    echo "[INFO] No backend process running on port 8080."
fi

# 2. Stop frontend on port 5500
FRONTEND_PIDS=$(lsof -ti :5500 2>/dev/null || true)
if [ -n "$FRONTEND_PIDS" ]; then
    echo "[INFO] Stopping frontend process (PID: $FRONTEND_PIDS)..."
    kill $FRONTEND_PIDS 2>/dev/null || true
    sleep 1
    if lsof -ti :5500 >/dev/null 2>&1; then
        kill -9 $(lsof -ti :5500) 2>/dev/null || true
    fi
    pkill -f "python3 -m http.server 5500" 2>/dev/null || true
    echo "[OK] Frontend stopped."
else
    pkill -f "python3 -m http.server 5500" 2>/dev/null || true
    echo "[INFO] No frontend process running on port 5500."
fi

echo "========================================="
echo "  All LMS services stopped successfully."
echo "========================================="

