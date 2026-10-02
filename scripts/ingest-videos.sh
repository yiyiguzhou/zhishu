#!/usr/bin/env bash
# 视频导入脚本：把一个「作者目录」下的 .mp4 批量导入到 dev MinIO，并生成增量 SQL。
#
# 运行位置：部署机 .38 宿主（依赖本地 ffmpeg/ffprobe，及 docker 容器 zhishu-minio 内置的 mc）。
# 注意：非交互 SSH 不会加载 ~/.zshrc，需 `zsh -lc` 或 login shell 才能找到 ffmpeg/docker。
#
# 做的事（对映射里的每个视频）：
#   1. 封面：blackdetect 跳过片头黑场，取第一张非黑帧（探测失败回退第 3 秒），缩到 640 宽
#   2. ffprobe 读时长
#   3. mc 上传 mp4 → bucket/<作者slug>/<视频slug>.mp4
#              封面 → bucket/covers/<作者slug>/<视频slug>.jpg
#   4. 生成 SQL：INSERT blogger + blogger 分类 + N 条 video（+ 可选回填 UPDATE）
#
# --covers-only：只重抽/重传封面（mp4 与 SQL 不动），用于修黑帧封面等场景。
#
# 用法：
#   bash scripts/ingest-videos.sh \
#     --author '马克的技术工作坊' --author-slug mark-tech-workshop \
#     --dir "$HOME/Documents/YouTube/马克的技术工作坊" \
#     --map scripts/ingest/mark-tech-workshop.map.tsv \
#     --out-sql /tmp/ingest-sql.sql
# 仅重生成封面：
#   bash scripts/ingest-videos.sh --covers-only \
#     --author-slug mark-tech-workshop \
#     --dir "$HOME/Documents/YouTube/马克的技术工作坊" \
#     --map scripts/ingest/mark-tech-workshop.map.tsv
#
# 环境变量覆盖：见下方 DEFAULT 区。BACKFILL_IDS="2,3,4,5,6" 时额外输出回填 UPDATE。
set -uo pipefail

# ---------- 参数解析 ----------
AUTHOR="" AUTHOR_SLUG="" DIR="" MAP="" OUT_SQL="" COVERS_ONLY=0
while [ $# -gt 0 ]; do
  case "$1" in
    --author) AUTHOR="$2"; shift 2;;
    --author-slug) AUTHOR_SLUG="$2"; shift 2;;
    --dir) DIR="$2"; shift 2;;
    --map) MAP="$2"; shift 2;;
    --out-sql) OUT_SQL="$2"; shift 2;;
    --covers-only) COVERS_ONLY=1; shift;;
    *) echo "未知参数: $1" >&2; exit 2;;
  esac
done
if [ "$COVERS_ONLY" = 1 ]; then
  [ -n "$AUTHOR_SLUG" ] && [ -n "$DIR" ] && [ -n "$MAP" ] || {
    echo "covers-only 缺少参数（--author-slug --dir --map）" >&2; exit 2; }
else
  [ -n "$AUTHOR" ] && [ -n "$AUTHOR_SLUG" ] && [ -n "$DIR" ] && [ -n "$MAP" ] && [ -n "$OUT_SQL" ] || {
    echo "缺少必需参数（--author --author-slug --dir --map --out-sql）" >&2; exit 2; }
fi
[ -f "$MAP" ] || { echo "映射文件不存在: $MAP" >&2; exit 2; }
[ -d "$DIR" ] || { echo "作者目录不存在: $DIR" >&2; exit 2; }

# ---------- 环境默认值（与 data.sql / .38 环境一致） ----------
MINIO_DATA_HOST="${MINIO_DATA_HOST:-$HOME/Documents/YouTube}"
MINIO_CTR_DATA="${MINIO_CTR_DATA:-/data}"
MINIO_CONTAINER="${MINIO_CONTAINER:-zhishu-minio}"
MINIO_ALIAS="${MINIO_ALIAS:-local}"
BUCKET="${BUCKET:-zhishu-video}"
MINIO_PUBLIC_ENDPOINT="${MINIO_PUBLIC_ENDPOINT:-http://192.168.1.38:9000}"
BLOGGER_ID="${BLOGGER_ID:-5}"
CATEGORY_ID="${CATEGORY_ID:-12}"
VIDEO_START_ID="${VIDEO_START_ID:-7}"
AVATAR="${AVATAR:-https://placehold.co/200?text=${AUTHOR_SLUG}}"
INTRODUCTION="${INTRODUCTION:-专注 AI 工程化与智能体实践，覆盖 MCP、Agent、RAG、AI 编程等前沿技术}"
SOURCE_TYPE="${SOURCE_TYPE:-minio}"
BACKFILL_IDS="${BACKFILL_IDS:-}"

# 分类 cat_key -> category.id（与 data.sql 保持一致；bash 3 无关联数组，用 case）
cat_id_of() {
  case "$1" in
    harness) echo 1;; mcp) echo 2;; rag) echo 3;; prompt) echo 4;;
    ai-coding) echo 9;; ai-trends) echo 10;; fundamentals) echo 11;;
    *) echo "";;
  esac
}

sqlq() { printf '%s' "$1" | sed "s/'/''/g"; }

AUTHOR_DIR_BASENAME="$(basename "$DIR")"
COVER_TMP_HOST="$MINIO_DATA_HOST/.ingest-covers"
COVER_TMP_CTR="$MINIO_CTR_DATA/.ingest-covers"
mkdir -p "$COVER_TMP_HOST"

ffmpeg="$(command -v ffmpeg || echo /opt/homebrew/bin/ffmpeg)"
ffprobe="$(command -v ffprobe || echo /opt/homebrew/bin/ffprobe)"

# ---------- 生成 SQL 头部（covers-only 跳过） ----------
if [ "$COVERS_ONLY" != 1 ]; then
  {
    echo "-- 生成于 $(date '+%F %T') · 作者：$AUTHOR（slug=$AUTHOR_SLUG）"
    echo "-- 媒体已上传到 MinIO bucket=$BUCKET（source_type=$SOURCE_TYPE）"
    echo
    echo "INSERT INTO blogger (id, name, avatar, introduction) VALUES"
    echo "  ($BLOGGER_ID, '$(sqlq "$AUTHOR")', '$AVATAR', '$(sqlq "$INTRODUCTION")');"
    echo
    echo "INSERT INTO category (id, name, cat_key, cat_type) VALUES"
    echo "  ($CATEGORY_ID, '$(sqlq "$AUTHOR")', 'blogger_$BLOGGER_ID', 'blogger');"
    echo
  } > "$OUT_SQL"

  vid=$VIDEO_START_ID
  echo "INSERT INTO video (id, title, blogger_id, category_id, cover, media_key, duration, hot_score, source_type, play_url) VALUES" >> "$OUT_SQL"
  first=1
fi

count=0
fail=0

while IFS=$'\t' read -r filename slug cat_key <&3; do
  [ -n "$filename" ] || continue
  case "$filename" in \#*) continue;; esac
  file="$DIR/$filename"
  if [ ! -f "$file" ]; then
    echo "[跳过] 未找到文件: $filename" >&2; fail=$((fail+1)); continue
  fi

  # 封面：跳过片头黑场，取第一张非黑帧（blackdetect 扫前 8 秒；无黑场则回退第 3 秒）
  cover_host="$COVER_TMP_HOST/$slug.jpg"
  ts="$(ffmpeg -hide_banner -t 8 -i "$file" -vf blackdetect=d=0.5:pix_th=0.10 -f null - < /dev/null 2>&1 \
        | grep -oE 'black_end:[0-9.]+' | head -1 | cut -d: -f2)"
  case "$ts" in ''|*[!0-9.]*) ts=3;; esac
  ts="$(awk -v t="$ts" 'BEGIN{printf "%.2f", t+0.2}')"
  if ! "$ffmpeg" -y -v error -ss "$ts" -i "$file" -frames:v 1 -q:v 3 -vf "scale=640:-2" "$cover_host" 2>/dev/null; then
    echo "[警告] 封面抽帧失败: $slug" >&2; fail=$((fail+1)); continue
  fi

  # 上传封面
  cover_key="covers/$AUTHOR_SLUG/$slug.jpg"
  if ! docker exec "$MINIO_CONTAINER" mc cp "$COVER_TMP_CTR/$slug.jpg" "$MINIO_ALIAS/$BUCKET/$cover_key" >/dev/null 2>&1; then
    echo "[失败] 封面上传: $cover_key" >&2; fail=$((fail+1)); continue
  fi
  echo "[封面] $slug @ ${ts}s"

  if [ "$COVERS_ONLY" != 1 ]; then
    # 时长
    dur="$("$ffprobe" -v error -show_entries format=duration -of csv=p=0 "$file" 2>/dev/null)"
    dur_int="${dur%%.*}"; [ -z "$dur_int" ] && dur_int=0

    # 上传 mp4
    ctr_mp4="$MINIO_CTR_DATA/$AUTHOR_DIR_BASENAME/$filename"
    mp4_key="$AUTHOR_SLUG/$slug.mp4"
    if ! docker exec "$MINIO_CONTAINER" mc cp "$ctr_mp4" "$MINIO_ALIAS/$BUCKET/$mp4_key" >/dev/null 2>&1; then
      echo "[失败] mp4 上传: $mp4_key" >&2; fail=$((fail+1)); continue
    fi

    # 写 SQL 行
    cat_id="$(cat_id_of "$cat_key")"
    [ -n "$cat_id" ] || { echo "[错误] 未知分类 cat_key=$cat_key ($filename)" >&2; fail=$((fail+1)); continue; }
    cover_url="$MINIO_PUBLIC_ENDPOINT/$BUCKET/$cover_key"
    title="$(printf '%s' "${filename%.mp4}" | sed -E 's/[[:space:].]+$//')"
    sep=""; [ "$first" = 1 ] || sep=","
    printf "%s  (%d, '%s', %s, %s, '%s', '%s', %d, 0, '%s', NULL)\n" \
      "$sep" "$vid" "$(sqlq "$title")" "$BLOGGER_ID" "$cat_id" "$(sqlq "$cover_url")" "$(sqlq "$AUTHOR_SLUG/$slug.mp4")" "$dur_int" "$SOURCE_TYPE" >> "$OUT_SQL"
    vid=$((vid+1)); first=0
  fi

  count=$((count+1))
done 3< "$MAP"

if [ "$COVERS_ONLY" != 1 ]; then
  echo ";" >> "$OUT_SQL"

  # 回填（已存在视频的 blogger_id）
  if [ -n "$BACKFILL_IDS" ]; then
    {
      echo
      echo "-- 回填：已在库中但 blogger_id 为空的本作者视频"
      echo "UPDATE video SET blogger_id = $BLOGGER_ID WHERE id IN ($BACKFILL_IDS);"
    } >> "$OUT_SQL"
  fi
fi

# 清理临时封面
rm -rf "$COVER_TMP_HOST"

echo
if [ "$COVERS_ONLY" = 1 ]; then
  echo "完成：重生成封面 $count 个，失败 $fail 个。"
else
  echo "完成：成功 $count 个，失败/跳过 $fail 个。SQL -> $OUT_SQL"
fi