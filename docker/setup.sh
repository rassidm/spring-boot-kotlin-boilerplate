#!/bin/bash

set -e

COMPOSE_ARGS=""

for file in base/docker-compose*.yml monitoring/docker-compose*.yml; do
  if [ -f "$file" ]; then
    COMPOSE_ARGS="$COMPOSE_ARGS -f $file"
  fi
done

echo "🛠️ Running docker compose with files:"
echo "$COMPOSE_ARGS"

if docker compose $COMPOSE_ARGS up -d --build; then
  echo "✅ Docker Compose completed successfully!"
else
  echo "❌ Docker Compose failed. Please check the logs."
  exit 1
fi
