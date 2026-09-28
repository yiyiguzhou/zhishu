#!/usr/bin/env bash
# 停止线上全部容器（数据卷保留，不丢数据）。可附加服务名只停部分：stop.sh backend
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
docker compose -f "$ROOT/deploy/docker-compose.prod.yml" down "$@"
