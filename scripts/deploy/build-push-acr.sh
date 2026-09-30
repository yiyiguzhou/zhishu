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

# 镜像版本：git 短提交号（可追溯/回退）+ latest（指向最近一次构建）
GIT_TAG="$(git rev-parse --short HEAD)"

echo ">> login ${REGISTRY}"
docker login "${REGISTRY}"

for svc in backend web; do
  REPO="${REGISTRY}/${NAMESPACE}/${svc}"
  echo ">> build+push ${svc} [${PLATFORMS}] tags: ${GIT_TAG}, latest"
  docker buildx build \
    --platform "${PLATFORMS}" \
    -f "deploy/${svc}/Dockerfile" \
    -t "${REPO}:${GIT_TAG}" \
    -t "${REPO}:latest" \
    --push \
    .
done

echo "done: backend web pushed (${PLATFORMS}, tags ${GIT_TAG} + latest) to ${REGISTRY}/${NAMESPACE}"
