#!/usr/bin/env bash
# .38 网关机：以 soft 模式挂载 NAS zhishu 共享，并驻留监控。
# soft 挂载在连接故障时自动卸载（而非永久挂死）；看门狗检测到丢失后带重试自动重挂，
# 并重启 MinIO 容器以恢复 /data bind。由用户级 LaunchAgent 常驻运行。
# 凭据文件：~/.config/zhishu/nas.env（NAS_IP/NAS_SHARE/NAS_USER/NAS_PASSWORD）。
set -euo pipefail

CONFIG="$HOME/.config/zhishu/nas.env"
[ -f "$CONFIG" ] || { echo "缺少凭据文件 $CONFIG" >&2; exit 1; }
# shellcheck disable=SC1090
. "$CONFIG"
: "${NAS_IP:?}" "${NAS_SHARE:?}" "${NAS_USER:?}" "${NAS_PASSWORD:?}"
MOUNT_POINT="${NAS_MOUNT_DIR:-$HOME/mnt/zhishu}"
MINIO_CONTAINER="${MINIO_CONTAINER:-zhishu-minio}"
DOCKER="/usr/local/bin/docker"

mkdir -p "$MOUNT_POINT"

is_mounted() { mount | grep -q " on $MOUNT_POINT "; }

do_mount() {
  for i in $(seq 1 10); do
    is_mounted && return 0
    # NAS 刚从休眠/断连恢复时挂载可能间歇失败，重试
    mount_smbfs -o soft "//${NAS_USER}:${NAS_PASSWORD}@${NAS_IP}/${NAS_SHARE}" "$MOUNT_POINT" 2>/dev/null \
      && { echo "NAS 已挂载: $MOUNT_POINT"; return 0; }
    echo "第 $i 次挂载失败，10 秒后重试" >&2
    sleep 10
  done
  echo "持续挂载失败，退出以触发 LaunchAgent 重启" >&2
  exit 1
}

do_mount
while true; do
  sleep 30
  is_mounted && continue
  echo "检测到挂载丢失，重新挂载 ..." >&2
  do_mount
  "$DOCKER" restart "$MINIO_CONTAINER" >/dev/null && echo "已重启 $MINIO_CONTAINER 以恢复数据绑定"
done
