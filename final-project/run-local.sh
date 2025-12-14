#!/bin/bash
# ===========================================
# Campus Events Platform - Local Development
# ===========================================
# This script runs both the backend and frontend locally.
# Make sure you have a .env file with all required variables.

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=========================================${NC}"
echo -e "${BLUE}  Campus Events Platform - Local Dev    ${NC}"
echo -e "${BLUE}=========================================${NC}"

# Check if .env file exists
if [ ! -f ".env" ]; then
    echo -e "${RED}Error: .env file not found!${NC}"
    echo -e "${YELLOW}Please copy .env.example to .env and fill in your values:${NC}"
    echo "  cp .env.example .env"
    exit 1
fi

# Load environment variables from .env
echo -e "${GREEN}Loading environment variables from .env...${NC}"
set -a
source .env
set +a

# Check required variables
REQUIRED_VARS=("DB_URL" "DB_USERNAME" "DB_PASSWORD" "JWT_SECRET")
MISSING_VARS=()

for var in "${REQUIRED_VARS[@]}"; do
    if [ -z "${!var}" ]; then
        MISSING_VARS+=("$var")
    fi
done

if [ ${#MISSING_VARS[@]} -ne 0 ]; then
    echo -e "${RED}Error: Missing required environment variables:${NC}"
    for var in "${MISSING_VARS[@]}"; do
        echo "  - $var"
    done
    exit 1
fi

echo -e "${GREEN}✓ All required environment variables set${NC}"

# Function to cleanup on exit
cleanup() {
    echo -e "\n${YELLOW}Shutting down services...${NC}"
    if [ ! -z "$BACKEND_PID" ]; then
        kill $BACKEND_PID 2>/dev/null || true
    fi
    if [ ! -z "$FRONTEND_PID" ]; then
        kill $FRONTEND_PID 2>/dev/null || true
    fi
    echo -e "${GREEN}Done.${NC}"
    exit 0
}

trap cleanup SIGINT SIGTERM

# Check for required tools
echo -e "\n${BLUE}Checking dependencies...${NC}"

if ! command -v java &> /dev/null; then
    echo -e "${RED}Error: Java is not installed.${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Java: $(java -version 2>&1 | head -n 1)${NC}"

if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Error: Maven is not installed.${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Maven: $(mvn -version 2>&1 | head -n 1)${NC}"

if ! command -v node &> /dev/null; then
    echo -e "${RED}Error: Node.js is not installed.${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Node.js: $(node -v)${NC}"

if ! command -v npm &> /dev/null; then
    echo -e "${RED}Error: npm is not installed.${NC}"
    exit 1
fi
echo -e "${GREEN}✓ npm: $(npm -v)${NC}"

# Helper to compute checksum (prefers shasum for macOS)
checksum_file() {
    if command -v shasum &> /dev/null; then
        shasum "$1" | awk '{print $1}'
    elif command -v sha1sum &> /dev/null; then
        sha1sum "$1" | awk '{print $1}'
    else
        echo ""
    fi
}

# Install frontend dependencies if needed
FRONTEND_DIR="frontend"
LOCK_FILE="$FRONTEND_DIR/package-lock.json"
HASH_FILE="$FRONTEND_DIR/node_modules/.install-hash"

NEED_INSTALL=false

if [ ! -d "$FRONTEND_DIR/node_modules" ]; then
    NEED_INSTALL=true
elif [ -f "$LOCK_FILE" ]; then
    CURRENT_HASH=$(checksum_file "$LOCK_FILE")
    RECORDED_HASH=""
    if [ -f "$HASH_FILE" ]; then
        RECORDED_HASH=$(cat "$HASH_FILE")
    fi
    if [ "$CURRENT_HASH" != "$RECORDED_HASH" ]; then
        NEED_INSTALL=true
    fi
else
    NEED_INSTALL=true
fi

if [ "$NEED_INSTALL" = true ]; then
    echo -e "\n${BLUE}Installing frontend dependencies...${NC}"
    (cd "$FRONTEND_DIR" && npm install)
    if [ -f "$LOCK_FILE" ]; then
        mkdir -p "$FRONTEND_DIR/node_modules"
        checksum_file "$LOCK_FILE" > "$HASH_FILE"
    fi
else
    echo -e "\n${GREEN}✓ Frontend dependencies already up to date${NC}"
fi

# Build backend if needed
if [ ! -f "backend/target/campus-events-backend-0.0.1-SNAPSHOT.jar" ]; then
    echo -e "\n${BLUE}Building backend...${NC}"
    (cd backend && mvn package -DskipTests -q)
fi

echo -e "\n${BLUE}=========================================${NC}"
echo -e "${GREEN}Starting services...${NC}"
echo -e "${BLUE}=========================================${NC}"

# Start backend
echo -e "\n${BLUE}Starting backend on http://localhost:8080${NC}"
(cd backend && mvn spring-boot:run -q) &
BACKEND_PID=$!

# Wait for backend to start
echo -e "${YELLOW}Waiting for backend to start...${NC}"
for i in {1..30}; do
    if curl -s http://localhost:8080/api/health > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Backend is running${NC}"
        break
    fi
    if [ $i -eq 30 ]; then
        echo -e "${RED}Backend failed to start within 30 seconds${NC}"
        cleanup
        exit 1
    fi
    sleep 1
done

# Start frontend
echo -e "\n${BLUE}Starting frontend on http://localhost:5173${NC}"
(cd frontend && VITE_API_URL="${VITE_API_URL:-http://localhost:8080/api}" npm run dev) &
FRONTEND_PID=$!

echo -e "\n${GREEN}=========================================${NC}"
echo -e "${GREEN}  Services are running!                 ${NC}"
echo -e "${GREEN}=========================================${NC}"
echo -e "${BLUE}  Backend:  ${NC}http://localhost:8080"
echo -e "${BLUE}  Frontend: ${NC}http://localhost:5173"
echo -e "${BLUE}  API Docs: ${NC}http://localhost:8080/api/health"
echo -e "${GREEN}=========================================${NC}"
echo -e "${YELLOW}Press Ctrl+C to stop all services${NC}"

# Wait for both processes
wait
