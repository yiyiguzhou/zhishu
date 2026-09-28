#!/usr/bin/env bash
# 线上启动：构建 Web → 同步静态产物 → 构建后端镜像 → 启动全部容器
# 在部署机仓库根目录执行；依赖 docker、node、npm、rsync
#
# 常用变量：
#   BACKEND_REPLICAS=3   后端副本数（默认 1；多副本由 Nginx 经 Docker DNS 负载）
#   COMPOSE_PROFILES=""  HA 模式：不启动本地 nacos/redis（接云 Nacos/Redis）
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT"
source "$ROOT/scripts/deploy/common.sh"

if [ ! -f deploy/.env.prod ]; then
  echo "缺少 deploy/.env.prod：cp deploy/.env.prod.example deploy/.env.prod 并填写" >&2
  exit 1
fi
for f in deploy/certs/zhishu.pem deploy/certs/zhishu.key; do
  if [ ! -f "$f" ]; then
    echo "缺少证书 $f（Nginx 443 依赖，获取方式见 doc/DEPLOY.md）" >&2
    exit 1
  fi
done

echo ">> 构建 Web 静态文件"
npm --prefix web ci
npm --prefix web run build
mkdir -p deploy/web-dist
rsync -a --delete web/dist/ deploy/web-dist/

BACKEND_REPLICAS="${BACKEND_REPLICAS:-1}"
echo ">> 构建后端镜像并启动服务（backend × $BACKEND_REPLICAS）"
"${COMPOSE[@]}" up -d --build --scale backend="$BACKEND_REPLICAS"

echo ">> 已启动，容器状态："
"${COMPOSE[@]}" ps
