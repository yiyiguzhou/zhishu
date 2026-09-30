#!/usr/bin/env bash
# 本地开发启动（默认）：后端连 .38 的 MySQL/Nacos/Redis/MinIO。
# 各组件地址是 application.yml 的默认值，无需显式设置；
# 仅 MinIO 凭据（代码默认 minioadmin，.38 实际为自定义值）从 .dev-secrets 注入。
# 想在本地走 OSS：用 dev-run-oss.sh。
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [ ! -f .dev-secrets ]; then
  echo "缺少 .dev-secrets（gitignore），按注释创建并填 MinIO 凭据" >&2
  exit 1
fi
set -a
# shellcheck disable=SC1091
source .dev-secrets
set +a

export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home -v 17)}"

exec mvn -f backend/pom.xml spring-boot:run
