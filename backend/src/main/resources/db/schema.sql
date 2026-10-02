-- 大模型学习平台 数据库结构（H2 兼容 MySQL 模式 / MySQL 通用）
-- 视频元数据规范化：video 经 category_id/blogger_id 外键关联类别与作者，标签走 tag + video_tag 多对多。
-- 开发用 H2 自动执行；生产建议用迁移工具（如 Flyway）管理这份脚本。

DROP TABLE IF EXISTS video_tag;
DROP TABLE IF EXISTS history;
DROP TABLE IF EXISTS favorite;
DROP TABLE IF EXISTS article;
DROP TABLE IF EXISTS tag;
DROP TABLE IF EXISTS video;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS blogger;
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    phone        VARCHAR(20)  DEFAULT NULL,
    nickname      VARCHAR(64)  NOT NULL,
    avatar        VARCHAR(512) DEFAULT NULL,
    wechat_openid VARCHAR(128) DEFAULT NULL,
    created_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_phone (phone),
    UNIQUE KEY uk_openid (wechat_openid)
);

CREATE TABLE blogger (
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    name         VARCHAR(64) NOT NULL,
    avatar       VARCHAR(512) DEFAULT NULL,
    introduction VARCHAR(512) DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE category (
    id       BIGINT      NOT NULL AUTO_INCREMENT,
    name     VARCHAR(64) NOT NULL,
    cat_key  VARCHAR(64) NOT NULL,
    cat_type VARCHAR(32) NOT NULL COMMENT 'video_tech | blogger',
    PRIMARY KEY (id),
    UNIQUE KEY uk_key_type (cat_key, cat_type)
);

CREATE TABLE video (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    title       VARCHAR(256)  NOT NULL,
    blogger_id  BIGINT        DEFAULT NULL COMMENT '作者 -> blogger.id',
    category_id BIGINT        DEFAULT NULL COMMENT '视频类别 -> category.id',
    cover       VARCHAR(512)  DEFAULT NULL,
    media_key   VARCHAR(512)  DEFAULT NULL COMMENT '存储中立定位：NAS 相对路径 / OSS Object Key',
    duration    INT           DEFAULT 0 COMMENT '秒',
    hot_score   INT           DEFAULT 0,
    source_type VARCHAR(32)   DEFAULT 'local' COMMENT 'local | minio | nas | oss',
    play_url    VARCHAR(1024) DEFAULT NULL COMMENT '可选直链覆盖；为空时由 StreamSource 按 media_key 解析',
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_video_category (category_id),
    KEY idx_video_blogger (blogger_id),
    CONSTRAINT fk_video_blogger  FOREIGN KEY (blogger_id)  REFERENCES blogger (id),
    CONSTRAINT fk_video_category FOREIGN KEY (category_id) REFERENCES category (id)
);

CREATE TABLE tag (
    id   BIGINT      NOT NULL AUTO_INCREMENT,
    name VARCHAR(32) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tag_name (name)
);

CREATE TABLE video_tag (
    video_id BIGINT NOT NULL,
    tag_id   BIGINT NOT NULL,
    PRIMARY KEY (video_id, tag_id),
    KEY idx_vt_tag (tag_id),
    CONSTRAINT fk_vt_video FOREIGN KEY (video_id) REFERENCES video (id) ON DELETE CASCADE,
    CONSTRAINT fk_vt_tag   FOREIGN KEY (tag_id)   REFERENCES tag (id)   ON DELETE CASCADE
);

CREATE TABLE article (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    title        VARCHAR(256) NOT NULL,
    blogger_id   BIGINT       DEFAULT NULL,
    cover        VARCHAR(512) DEFAULT NULL,
    content_url  VARCHAR(1024) DEFAULT NULL,
    source_url   VARCHAR(1024) DEFAULT NULL COMMENT '原文链接（人工提供，溯源）',
    source_title VARCHAR(512)  DEFAULT NULL COMMENT '来源标题',
    author_name  VARCHAR(64)  DEFAULT NULL COMMENT '外部来源作者（弱关联，不建外键）',
    summary      VARCHAR(1024) DEFAULT NULL COMMENT '列表页 AI 摘要',
    content_md   TEXT          DEFAULT NULL COMMENT '正文 Markdown',
    category_key VARCHAR(64)  DEFAULT NULL,
    hot_score    INT          DEFAULT 0,
    published_at DATETIME     DEFAULT NULL COMMENT '发布时间（衰减依据，空则用 created_at）',
    status       VARCHAR(16)  NOT NULL DEFAULT 'published' COMMENT 'published | draft',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_article_category (category_key),
    KEY idx_article_status (status, published_at)
);

CREATE TABLE favorite (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    user_id      BIGINT       NOT NULL,
    target_type  VARCHAR(16)  NOT NULL COMMENT 'video | article',
    target_id    BIGINT       NOT NULL,
    status       VARCHAR(16)  NOT NULL DEFAULT 'active' COMMENT 'active 收藏中 | canceled 已取消（软删除，行保留）',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次收藏时间',
    canceled_at  DATETIME     DEFAULT NULL COMMENT '最近一次取消时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_fav_user_target (user_id, target_type, target_id)
);
-- “未收藏”有两种状态：从未收藏（无行）、收藏后取消（行存在 status=canceled）；
-- 重新收藏时将 canceled 行更新回 active，不新增行。

CREATE TABLE history (
    id               BIGINT      NOT NULL AUTO_INCREMENT,
    user_id          BIGINT      NOT NULL,
    target_type      VARCHAR(16) NOT NULL COMMENT 'video | article',
    target_id        BIGINT      NOT NULL,
    watched_progress INT         DEFAULT 0,
    last_watched_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_history_user (user_id, last_watched_at),
    UNIQUE KEY uk_history_user_target (user_id, target_type, target_id)
);
