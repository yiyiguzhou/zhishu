#!/usr/bin/env bash
# .38 网关机：安装用户级 LaunchAgent，登录后自动挂载 NAS（掉线后 mount-nas 非零退出 → 自动重挂）。
set -euo pipefail
DIR="$(cd "$(dirname "$0")" && pwd)"
PLIST="$HOME/Library/LaunchAgents/com.zhishu.nas-share-mount.plist"
LABEL="com.zhishu.nas-share-mount"

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
        <string>$DIR/mount-nas.sh</string>
    </array>
    <key>RunAtLoad</key>
    <true/>
    <key>KeepAlive</key>
    <dict><key>SuccessfulExit</key><false/></dict>
    <key>StandardOutPath</key>
    <string>/tmp/zhishu-nas-share.log</string>
    <key>StandardErrorPath</key>
    <string>/tmp/zhishu-nas-share.log</string>
</dict>
</plist>
EOF

launchctl load "$PLIST"
echo "已安装并加载: $PLIST"
