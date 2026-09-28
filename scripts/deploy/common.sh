# 部署脚本公共配置：被 scripts/deploy/*.sh source，不直接执行。
# 单机默认本地 nacos/redis 容器；HA 时 COMPOSE_PROFILES="" 只起 backend+nginx。
# 需要同机 MySQL：COMPOSE_PROFILES="local-nacos,local-redis,local-mysql"
export COMPOSE_PROFILES="${COMPOSE_PROFILES:-local-nacos,local-redis}"

DEPLOY_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
# --env-file 用于 compose 文件的 ${...} 插值（env_file 只注入容器，不做插值）；
# backend 容器另外通过 env_file 拿到同一份变量。
COMPOSE=(docker compose
  --env-file "$DEPLOY_ROOT/deploy/.env.prod"
  -f "$DEPLOY_ROOT/deploy/docker-compose.prod.yml")
