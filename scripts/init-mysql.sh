#!/usr/bin/env bash
# 初始化 MySQL：建库 zhishu + 建表 + 种子数据（见 scripts/InitMysql.java）
# 依赖 JDK17 与本地 ~/.m2 中的 mysql-connector-j（编译过后端就有），不需要 mysql 客户端。
# 可用环境变量覆盖：MYSQL_HOST(含端口) MYSQL_USER MYSQL_PASSWORD MYSQL_DB
# 对已存在表的库重跑（会 DROP 表）：INIT_FORCE=1 bash scripts/init-mysql.sh
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home -v 17)}"

MYSQL_HOST="${MYSQL_HOST:-192.168.1.38:3306}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-root}"
MYSQL_DB="${MYSQL_DB:-zhishu}"

JAR="$(find "$HOME/.m2/repository/com/mysql/mysql-connector-j" -name 'mysql-connector-j-*.jar' \
  ! -name '*sources*' 2>/dev/null | sort -V | tail -1)"
[ -n "$JAR" ] || { echo "未找到 mysql-connector-j，请先编译后端：mvn -f backend/pom.xml compile" >&2; exit 1; }

exec "$JAVA_HOME/bin/java" --class-path "$JAR" "$ROOT/scripts/InitMysql.java" \
  "$MYSQL_HOST" "$MYSQL_USER" "$MYSQL_PASSWORD" "$MYSQL_DB" "$ROOT/backend/src/main/resources/db"
