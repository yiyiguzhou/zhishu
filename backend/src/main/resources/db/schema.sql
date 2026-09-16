-- 大模型学习平台 数据库结构（H2 兼容 MySQL 模式 / MySQL 通用）
-- 开发用 H2 自动执行；生产建议用迁移工具（如 Flyway）管理这份脚本。

DROP TABLE IF EXISTS history;
DROP TABLE IF EXISTS favorite;
DROP TABLE IF EXISTS article;
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
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    title        VARCHAR(256) NOT NULL,
    blogger_id   BIGINT       DEFAULT NULL,
    cover        VARCHAR(512) DEFAULT NULL,
    media_key    VARCHAR(512) DEFAULT NULL COMMENT '在 NAS/MinIO 中的位置',
    duration     INT          DEFAULT 0 COMMENT '秒',
    category_key VARCHAR(64)  DEFAULT NULL COMMENT 'harness/mcp/rag...',
    hot_score    INT          DEFAULT 0,
    source_type  VARCHAR(32)  DEFAULT 'local' COMMENT 'local | minio | nas',
    play_url     VARCHAR(1024) DEFAULT NULL COMMENT 'local 模式直链地址',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_video_category (category_key),
    KEY idx_video_blogger (blogger_id)
);

CREATE TABLE article (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    title        VARCHAR(256) NOT NULL,
    blogger_id   BIGINT       DEFAULT NULL,
    cover        VARCHAR(512) DEFAULT NULL,
    content_url  VARCHAR(1024) DEFAULT NULL,
    category_key VARCHAR(64)  DEFAULT NULL,
    hot_score    INT          DEFAULT 0,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_article_category (category_key)
);

CREATE TABLE favorite (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    user_id     BIGINT      NOT NULL,
    target_type VARCHAR(16) NOT NULL COMMENT 'video | article',
    target_id   BIGINT      NOT NULL,
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_fav_user_target (user_id, target_type, target_id)
);

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