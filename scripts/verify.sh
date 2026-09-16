#!/usr/bin/env bash
# 提交前/交付前的完整验证：后端编译 + Web 类型检查
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
export JAVA_HOME="$(/usr/libexec/java_home -v 17)"

echo "== [1/2] 后端编译 (JDK 17, mvn compile) =="
mvn -q -f "$ROOT/backend/pom.xml" -DskipTests compile

echo "== [2/2] Web 类型检查 (tsc -b) =="
(cd "$ROOT/web" && npx tsc -b)

echo "== 验证通过 =="
