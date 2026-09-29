#!/usr/bin/env bash
# 本地以 OSS 模式启动后端（连北京 zhishu-video-ai）。
# AK/SK 从 .dev-secrets 读取（gitignore，不入库）；
# set -a 让其中的变量自动导出给 mvn（直接 source 不会进子进程环境）。
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [ ! -f .dev-secrets ]; then
  echo "缺少 .dev-secrets：按模板创建并填入 RAM AK/SK" >&2
  exit 1
fi
set -a
# shellcheck disable=SC1091
source .dev-secrets
set +a

if [ -z "${OSS_ACCESS_KEY:-}" ] || [ -z "${OSS_SECRET_KEY:-}" ]; then
  echo ".dev-secrets 中 OSS_ACCESS_KEY / OSS_SECRET_KEY 未填写" >&2
  exit 1
fi

export MEDIA_MODE=oss
export OSS_ENDPOINT="${OSS_ENDPOINT:-https://oss-cn-beijing.aliyuncs.com}"
export OSS_BUCKET="${OSS_BUCKET:-zhishu-video-ai}"
export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home -v 17)}"

exec mvn -f backend/pom.xml spring-boot:run
