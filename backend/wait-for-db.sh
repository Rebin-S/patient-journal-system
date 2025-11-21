#!/usr/bin/env bash
HOST="$1"; PORT="$2"; shift 2
echo "Waiting for $HOST:$PORT ..."
until (echo > /dev/tcp/$HOST/$PORT) >/dev/null 2>&1; do
  sleep 1
done
echo "DB is up. Starting app..."
exec "$@"