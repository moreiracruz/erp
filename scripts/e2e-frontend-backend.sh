#!/bin/bash
# =============================================================================
# Frontend + Backend E2E Smoke Test
# Usage: ./scripts/e2e-frontend-backend.sh
#
# Validates the deployed Docker Compose path:
#   browser/http -> frontend Nginx -> /api proxy -> backend
# =============================================================================

set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
FRONTEND_URL="${FRONTEND_URL:-http://localhost}"
BACKEND_URL="${BACKEND_URL:-http://localhost:8080}"
TIMEOUT_SECONDS="${TIMEOUT_SECONDS:-180}"

cd "$PROJECT_ROOT"

require_command() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1"
    exit 1
  fi
}

status_code() {
  local method="$1"
  local url="$2"
  local body="${3:-}"

  if [ -n "$body" ]; then
    curl -sS -o /tmp/erp-e2e-response.txt -w "%{http_code}" \
      -X "$method" \
      -H "Content-Type: application/json" \
      --data "$body" \
      "$url"
  else
    curl -sS -o /tmp/erp-e2e-response.txt -w "%{http_code}" \
      -X "$method" \
      "$url"
  fi
}

wait_for_status() {
  local name="$1"
  local method="$2"
  local url="$3"
  local expected="$4"
  local body="${5:-}"
  local deadline=$((SECONDS + TIMEOUT_SECONDS))
  local current

  echo "Waiting for $name ($method $url) -> $expected"

  while [ "$SECONDS" -lt "$deadline" ]; do
    current="$(status_code "$method" "$url" "$body" || true)"
    if [ "$current" = "$expected" ]; then
      echo "OK: $name returned $expected"
      return 0
    fi
    sleep 3
  done

  echo "FAIL: $name did not return $expected within ${TIMEOUT_SECONDS}s"
  echo "Last HTTP status: ${current:-none}"
  echo "--- Last response body ---"
  sed -n '1,120p' /tmp/erp-e2e-response.txt 2>/dev/null || true
  echo "--- Compose status ---"
  docker compose ps
  echo "--- Backend logs ---"
  docker compose logs --no-color --tail=80 app || true
  echo "--- Frontend logs ---"
  docker compose logs --no-color --tail=80 frontend || true
  exit 1
}

require_command docker
require_command curl

if [ ! -f ".env" ]; then
  echo "Missing .env. Create it from .env.example before running this E2E test:"
  echo "  cp .env.example .env"
  exit 1
fi

echo "Building and starting frontend + backend stack..."
if ! docker compose up -d --build postgres app frontend; then
  echo "FAIL: docker compose could not start the E2E stack."
  echo "--- Compose status ---"
  docker compose ps
  echo "--- Backend logs ---"
  docker compose logs --no-color --tail=120 app || true
  echo "--- Frontend logs ---"
  docker compose logs --no-color --tail=80 frontend || true
  exit 1
fi

wait_for_status "backend readiness" "GET" "$BACKEND_URL/actuator/health/readiness" "200"
wait_for_status "frontend html" "GET" "$FRONTEND_URL/" "200"

if ! grep -q "<app-root" /tmp/erp-e2e-response.txt; then
  echo "FAIL: frontend HTML did not include <app-root>."
  sed -n '1,80p' /tmp/erp-e2e-response.txt
  exit 1
fi
echo "OK: frontend HTML contains <app-root>"

wait_for_status "public products API through frontend proxy" "GET" "$FRONTEND_URL/api/v1/products" "200"
wait_for_status "public catalog API through frontend proxy" "GET" "$FRONTEND_URL/api/v1/products/catalog" "200"
wait_for_status "protected products API through frontend proxy" "POST" "$FRONTEND_URL/api/v1/products" "401" "{}"

echo ""
echo "E2E smoke test passed."
echo "Validated:"
echo "  - backend readiness: $BACKEND_URL/actuator/health/readiness"
echo "  - frontend static app: $FRONTEND_URL/"
echo "  - frontend -> backend proxy: $FRONTEND_URL/api/v1/products"
echo "  - frontend -> backend catalog proxy: $FRONTEND_URL/api/v1/products/catalog"
echo "  - backend security via proxy: protected POST returned 401"
echo ""
echo "Stack is still running for inspection. To stop it:"
echo "  docker compose down"
