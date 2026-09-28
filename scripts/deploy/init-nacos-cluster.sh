#!/usr/bin/env bash
# 初始化并启动 Nacos 3 节点集群：
# 1. 从 nacos 镜像提取官方建表 SQL 到 deploy/nacos/initdb/（供 MySQL 首启初始化）
# 2. compose 启动 MySQL + nacos1/2/3
# 依赖：docker；变量取自 deploy/.env.prod（NACOS_AUTH_TOKEN、NACOS_MYSQL_PASSWORD）
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT"

ENV_FILE="$ROOT/deploy/.env.prod"
[ -f "$ENV_FILE" ] || { echo "缺少 deploy/.env.prod（见 .env.prod.example）" >&2; exit 1; }

# 只做变量插值所需的键存在性检查，不 source（值中可能有特殊字符）
for k in NACOS_AUTH_TOKEN NACOS_MYSQL_PASSWORD; do
  grep -q "^$k=.\+" "$ENV_FILE" || { echo "$ENV_FILE 中 $k 未填写" >&2; exit 1; }
done

NACOS_IMAGE="nacos/nacos-server:v2.5.4"
INITDB="$ROOT/deploy/nacos/initdb"
mkdir -p "$INITDB"
if [ ! -s "$INITDB/nacos-schema.sql" ]; then
  echo ">> 从 $NACOS_IMAGE 提取 mysql-schema.sql"
  docker run --rm --entrypoint sh "$NACOS_IMAGE" \
    -c 'cat /home/nacos/conf/mysql-schema.sql' > "$INITDB/nacos-schema.sql"
fi

echo ">> 启动 Nacos 集群"
docker compose --env-file "$ENV_FILE" \
  -f "$ROOT/deploy/nacos/docker-compose.cluster.yml" up -d

docker compose --env-file "$ENV_FILE" \
  -f "$ROOT/deploy/nacos/docker-compose.cluster.yml" ps
