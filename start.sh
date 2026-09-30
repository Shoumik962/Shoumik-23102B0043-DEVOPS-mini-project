#!/usr/bin/env bash

# ==============================================================================
# Food Waste Tracker - Unified Runner Script
# Builds backend, starts Java REST API on port 8081, and Vite Frontend on port 5173
# ==============================================================================

set -e

# Change directory to project root
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd "$DIR"

echo "=================================================="
echo " Starting Food Waste Tracking Dashboard Stack"
echo "=================================================="

# Kill any existing processes on ports 8081 and 5173 before startup
free_ports() {
    echo "Ensuring ports 8081 and 5173 are free..."
    PIDS_8081=$(lsof -ti :8081 2>/dev/null || true)
    if [ -n "$PIDS_8081" ]; then
        kill -9 $PIDS_8081 2>/dev/null || true
    fi
    PIDS_5173=$(lsof -ti :5173 2>/dev/null || true)
    if [ -n "$PIDS_5173" ]; then
        kill -9 $PIDS_5173 2>/dev/null || true
    fi
}

free_ports

# Function to clean up child processes on exit
cleanup() {
    echo ""
    echo "Shutting down Food Waste Tracker services..."
    if [ -n "$BACKEND_PID" ]; then
        kill "$BACKEND_PID" 2>/dev/null || true
    fi
    if [ -n "$FRONTEND_PID" ]; then
        kill "$FRONTEND_PID" 2>/dev/null || true
    fi
    pkill -f "tracker-backend.jar" 2>/dev/null || true
    echo "All services stopped."
    exit 0
}

trap cleanup SIGINT SIGTERM EXIT

# 1. Build Backend if JAR does not exist
if [ ! -f "$DIR/backend/target/tracker-backend.jar" ]; then
    echo "[1/3] Building Backend with Maven..."
    cd "$DIR/backend"
    mvn clean package -DskipTests
    cd "$DIR"
fi

# 2. Start Backend
echo "[2/3] Starting Java Backend on http://localhost:8081..."
java -jar "$DIR/backend/target/tracker-backend.jar" &
BACKEND_PID=$!

# Wait for backend to be healthy
echo "Waiting for Backend to start..."
for i in {1..30}; do
    if curl -s http://localhost:8081/api/health >/dev/null 2>&1; then
        echo "Backend is UP and HEALTHY!"
        break
    fi
    sleep 0.5
done

# 3. Start Frontend
echo "[3/3] Starting Frontend Vite Server..."
cd "$DIR/frontend"
if [ ! -d "node_modules" ]; then
    npm install
fi
npm run dev &
FRONTEND_PID=$!
cd "$DIR"

echo "=================================================="
echo " Stack is RUNNING!"
echo " Frontend: http://localhost:5173"
echo " Backend:  http://localhost:8081"
echo " Health:   http://localhost:8081/api/health"
echo " Press Ctrl+C to stop all services."
echo "=================================================="

# Wait for any process to terminate
wait
