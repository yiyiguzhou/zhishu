-- 纸书运营后台 数据库增量（主库 zhishu）
-- article 加 valid（0 下线 | 1 上线）；新建 admin_user 表。

ALTER TABLE article ADD COLUMN `valid` TINYINT NOT NULL DEFAULT 1 COMMENT '0 下线 | 1 上线';

CREATE TABLE IF NOT EXISTS admin_user (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    username   VARCHAR(64)  NOT NULL,
    password   VARCHAR(128) NOT NULL COMMENT 'BCrypt',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_username (username)
);