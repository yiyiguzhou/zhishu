#!/usr/bin/env bash
# 线上启动：构建 Web → 同步静态产物 → 构建后端镜像 → 启动全部容器
# 在部署机仓库根目录执行；依赖 docker、node、npm、rsync
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT"

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

echo ">> 构建后端镜像并启动服务"
docker compose -f deploy/docker-compose.prod.yml up -d --build

echo ">> 已启动，容器状态："
docker compose -f deploy/docker-compose.prod.yml ps
