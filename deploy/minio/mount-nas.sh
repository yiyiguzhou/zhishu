#!/usr/bin/env bash
# .38 网关机：挂载 NAS 共享 zhishu 到 ~/mnt/zhishu，供 MinIO 容器 bind 使用。
# 凭据文件（仓库外）：~/.config/zhishu/nas.env，内容：
#   NAS_IP=192.168.1.2
#   NAS_SHARE=zhishu
#   NAS_USER=zhishu
#   NAS_PASSWORD=xxxx
set -euo pipefail

CONFIG="$HOME/.config/zhishu/nas.env"
[ -f "$CONFIG" ] || { echo "缺少凭据文件 $CONFIG" >&2; exit 1; }
# shellcheck disable=SC1090
. "$CONFIG"

: "${NAS_IP:?}" "${NAS_SHARE:?}" "${NAS_USER:?}" "${NAS_PASSWORD:?}"
MOUNT_POINT="${NAS_MOUNT_DIR:-$HOME/mnt/zhishu}"

mkdir -p "$MOUNT_POINT"

if mount | grep -q " on $MOUNT_POINT "; then
  echo "已挂载: $MOUNT_POINT"
else
  echo "挂载 //$NAS_IP/$NAS_SHARE -> $MOUNT_POINT"
  mount_smbfs "//${NAS_USER}:${NAS_PASSWORD}@${NAS_IP}/${NAS_SHARE}" "$MOUNT_POINT"
fi
