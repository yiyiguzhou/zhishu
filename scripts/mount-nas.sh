#!/usr/bin/env bash
# 挂载 NAS（WD My Cloud EX2 Ultra）视频共享到本机，供后端 nas 模式读取。
# 凭据文件（仓库外，不入库）：~/.config/zhishu/nas.env，内容示例：
#   NAS_IP=192.168.1.2
#   NAS_SHARE=zhishu-video
#   NAS_USER=zhishu
#   NAS_PASSWORD=xxxx
# 已挂载则前台驻留并每 30s 检测；挂载丢失则非零退出（配合 LaunchAgent 自动重挂）。
set -euo pipefail

CONFIG="$HOME/.config/zhishu/nas.env"
[ -f "$CONFIG" ] || { echo "缺少凭据文件 $CONFIG" >&2; exit 1; }
# shellcheck disable=SC1090
. "$CONFIG"

: "${NAS_IP:?}" "${NAS_SHARE:?}" "${NAS_USER:?}" "${NAS_PASSWORD:?}"
MOUNT_POINT="${NAS_MOUNT_DIR:-$HOME/mnt/zhishu-video}"

mkdir -p "$MOUNT_POINT"

is_mounted() { mount | grep -q " on $MOUNT_POINT "; }

if is_mounted; then
  echo "已挂载: $MOUNT_POINT"
else
  echo "挂载 //$NAS_IP/$NAS_SHARE -> $MOUNT_POINT"
  # 密码含特殊字符需先做 URL 编码
  mount_smbfs "//${NAS_USER}:${NAS_PASSWORD}@${NAS_IP}/${NAS_SHARE}" "$MOUNT_POINT"
fi

while true; do
  sleep 30
  is_mounted || { echo "挂载丢失，退出以触发自动重挂" >&2; exit 1; }
done
