#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

FRONTEND_PORT=1033

# Base path under which the Astro frontend is served (e.g. / or /readability).
# Match this to your nginx location block.
PUBLIC_BASE_PATH="${PUBLIC_BASE_PATH:-/readability}"

echo "==> Building backend JAR..."
(
  cd "${PROJECT_ROOT}"
  ./gradlew :readability4k:bootJar
)

echo "==> Building and starting containers..."
(
  cd "${SCRIPT_DIR}"
  PUBLIC_BASE_PATH="${PUBLIC_BASE_PATH}" docker compose up --build -d --remove-orphans
)

echo "==> Waiting for backend to be ready..."
for _ in {1..30}; do
  if docker exec readability4k-backend wget -qO- http://localhost:1031/ >/dev/null 2>&1; then
    break
  fi
  sleep 1
done

echo "==> Removing old/dangling images..."
docker image prune -f >/dev/null

echo ""
echo "==> Deployment complete"
docker compose -f "${SCRIPT_DIR}/compose.yaml" ps

echo ""
echo "  Frontend: http://localhost:${FRONTEND_PORT}"
echo ""
echo "  To override defaults, set these variables before running deploy.sh:"
echo "    PUBLIC_BASE_PATH (default: /readability)"
echo ""
echo "  Examples:"
echo "    PUBLIC_BASE_PATH=/ ${SCRIPT_DIR}/deploy.sh"
