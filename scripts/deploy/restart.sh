#!/usr/bin/env bash
# 重启容器（不重新构建镜像）。可附加服务名：restart.sh backend
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
source "$ROOT/scripts/deploy/common.sh"
"${COMPOSE[@]}" restart "$@"
