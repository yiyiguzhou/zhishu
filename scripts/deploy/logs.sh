#!/usr/bin/env bash
# 查看容器日志（默认跟踪最后 100 行）。可附加服务名：logs.sh backend
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
source "$ROOT/scripts/deploy/common.sh"
"${COMPOSE[@]}" logs -f --tail=100 "$@"
