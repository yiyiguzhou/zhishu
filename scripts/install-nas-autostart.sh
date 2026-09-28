#!/usr/bin/env bash
# 安装/更新用户级 LaunchAgent：登录后自动挂载 NAS，挂载脚本非零退出（如掉线）时自动重启重挂。
# 仅当前用户、免 sudo；仓库路径变化后需重跑本脚本。
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PLIST="$HOME/Library/LaunchAgents/com.zhishu.nas-mount.plist"
LABEL="com.zhishu.nas-mount"

mkdir -p "$HOME/Library/LaunchAgents"

launchctl unload "$PLIST" 2>/dev/null || true

cat > "$PLIST" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>Label</key>
    <string>$LABEL</string>
    <key>ProgramArguments</key>
    <array>
        <string>/bin/bash</string>
        <string>$ROOT/scripts/mount-nas.sh</string>
    </array>
    <key>RunAtLoad</key>
    <true/>
    <key>KeepAlive</key>
    <true/>
    <key>StandardOutPath</key>
    <string>/tmp/zhishu-nas-mount.log</string>
    <key>StandardErrorPath</key>
    <string>/tmp/zhishu-nas-mount.log</string>
</dict>
</plist>
EOF

launchctl load "$PLIST"
echo "已安装并加载: $PLIST"
echo "日志: /tmp/zhishu-nas-mount.log"
