#!/usr/bin/env bash
# 把 scripts/git-hooks 下的钩子安装到 .git/hooks（钩子不进 git，重新 clone 后需重跑）
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cp "$ROOT/scripts/git-hooks/pre-commit" "$ROOT/.git/hooks/pre-commit"
chmod +x "$ROOT/.git/hooks/pre-commit"
echo "已安装 pre-commit 钩子 -> .git/hooks/pre-commit"
