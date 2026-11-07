#!/bin/bash
# ==============================================================================
# Galgotias LMS — Start Frontend Script (macOS / Local)
# ==============================================================================

set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$DIR"

echo "========================================="
echo "  Starting Galgotias LMS Frontend"
echo "========================================="

# Check if port 5500 is already in use and free it
if lsof -ti :5500 >/dev/null 2>&1; then
    OLD_PID=$(lsof -ti :5500 | tr '\n' ' ')
    echo "[INFO] Port 5500 is currently occupied by PID(s): $OLD_PID."
    echo "[INFO] Stopping existing frontend process to prevent port conflict..."
    kill $(lsof -ti :5500) 2>/dev/null || true
    sleep 1
fi

echo "[INFO] Serving frontend files on http://localhost:5500..."
echo "[INFO] Opening browser to http://localhost:5500..."

(sleep 1 && open "http://localhost:5500" 2>/dev/null || true) &

python3 -m http.server 5500 --directory frontend

