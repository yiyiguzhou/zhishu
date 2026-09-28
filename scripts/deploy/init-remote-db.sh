#!/usr/bin/env bash
# 初始化线上 MySQL：建库 + 建表 + 灌种子数据（schema.sql 含 DROP IF EXISTS，
# 会清空同名表，仅在首次部署或需要重置时执行）。
#
# 依赖：mysql 客户端（Ubuntu: apt install -y default-mysql-client）
# RDS 示例：
#   MYSQL_HOST=rm-xxxx.mysql.rds.aliyuncs.com MYSQL_USER=root \
#   MYSQL_PASSWORD=xxx bash scripts/deploy/init-remote-db.sh
# 同机容器库（compose 的 local-mysql profile，无宿主端口）：
#   USE_CONTAINER=1 MYSQL_PASSWORD=xxx bash scripts/deploy/init-remote-db.sh
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"

MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:?请设置 MYSQL_PASSWORD}"
MYSQL_DB="${MYSQL_DB:-zhishu}"
USE_CONTAINER="${USE_CONTAINER:-0}"

# SQL 经标准输入传入；"$@" 为额外的 mysql 参数（库名或 -e）
run_mysql() {
  if [ "$USE_CONTAINER" = "1" ]; then
    docker exec -i zhishu-mysql mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$@"
  else
    mysql -h"$MYSQL_HOST" -P"$MYSQL_PORT" -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$@"
  fi
}

echo ">> 建库 $MYSQL_DB"
run_mysql -e "CREATE DATABASE IF NOT EXISTS \`$MYSQL_DB\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

echo ">> 建表（schema.sql）"
run_mysql "$MYSQL_DB" < "$ROOT/backend/src/main/resources/db/schema.sql"

echo ">> 灌种子数据（data.sql）"
run_mysql "$MYSQL_DB" < "$ROOT/backend/src/main/resources/db/data.sql"

echo ">> 完成。"
