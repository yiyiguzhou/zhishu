#!/usr/bin/env bash
# 每日数据库备份：mysqldump（容器内）业务库 zhishu 与 nacos_config，
# gzip 后上传 OSS（db-backups/ 前缀）。保留策略由 OSS 生命周期管理
# （控制台对 db-backups/ 配 14/30 天自动过期）。
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
ENV="$ROOT/deploy/.env.prod"
[ -f "$ENV" ] || { echo "缺少 $ENV" >&2; exit 1; }

PW=$(grep "^MYSQL_PASSWORD=" "$ENV" | cut -d= -f2-)
DATE=$(date +%Y%m%d-%H%M)
TMP=$(mktemp -d)
trap 'rm -rf "$TMP"' EXIT

for DB in zhishu nacos_config; do
  OUT="$TMP/$DB-$DATE.sql.gz"
  docker exec zhishu-mysql mysqldump \
    --single-transaction --routines --triggers \
    --default-character-set=utf8mb4 \
    -uroot -p"$PW" "$DB" 2>/dev/null | gzip > "$OUT"
  /usr/local/bin/ossutil cp -f "$OUT" \
    "oss://zhishu-video-ai/db-backups/$DB-$DATE.sql.gz" >/dev/null
done

echo "[$(date '+%F %T')] 备份完成并已上传 OSS db-backups/"
