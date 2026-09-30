#!/usr/bin/env bash
# Build backend/web images for the server architecture and push to Aliyun ACR.
# ECS is x86 (linux/amd64); local Mac is Apple Silicon (arm64), so we build
# explicitly for linux/amd64 via buildx and push directly.
# Usage: bash scripts/deploy/build-push-acr.sh
# Multi-arch: PLATFORMS=linux/amd64,linux/arm64 bash ...
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT"

# Personal edition dedicated registry domain (per account instance).
REGISTRY="crpi-rt3s48pkmbucmrvt.cn-beijing.personal.cr.aliyuncs.com"
NAMESPACE="zhishu-ai"
PLATFORMS="${PLATFORMS:-linux/amd64}"

echo ">> login ${REGISTRY}"
docker login "${REGISTRY}"

for svc in backend web; do
  IMAGE="${REGISTRY}/${NAMESPACE}/${svc}:latest"
  echo ">> build+push ${svc} [${PLATFORMS}] -> ${IMAGE}"
  docker buildx build \
    --platform "${PLATFORMS}" \
    -f "deploy/${svc}/Dockerfile" \
    -t "${IMAGE}" \
    --push \
    .
done

echo "done: backend web pushed (${PLATFORMS}) to ${REGISTRY}/${NAMESPACE}"
