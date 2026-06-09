-- ============================================
-- 慢慢记 (ManManJi) 数据库 Schema
-- PostgreSQL 16 + PGVector
-- ============================================

-- 启用 PGVector 扩展
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- 1. 用户认证
-- ============================================

CREATE TABLE IF NOT EXISTS users (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE,
    email           VARCHAR(100) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    nickname        VARCHAR(50),
    avatar_url      VARCHAR(500),
    bio             TEXT,
    role            VARCHAR(20)  NOT NULL DEFAULT 'USER',
    status          VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

-- ============================================
-- 3. 文章
-- ============================================

CREATE TABLE IF NOT EXISTS articles (
    id                  BIGSERIAL PRIMARY KEY,                                    -- 主键
    title               VARCHAR(200) NOT NULL DEFAULT '未命名',                    -- 文章标题
    slug                VARCHAR(200) UNIQUE,                                      -- URL 标识，全局唯一，如 java-21-virtual-thread-xxx
    content             TEXT,                                                     -- Markdown 正文
    summary             VARCHAR(500),                                            -- 文章摘要，feed 列表展示用
    cover_url           VARCHAR(500),                                            -- 封面图链接

    user_id             BIGINT NOT NULL,                                         -- 用户 ID

    status              VARCHAR(15) NOT NULL DEFAULT 'DRAFT',                     -- DRAFT / PUBLISHED / PRIVATE / REVIEWING / REJECTED
    visibility          VARCHAR(10) NOT NULL DEFAULT 'PRIVATE',                   -- PUBLIC / PRIVATE / FOLLOWER
    article_type        VARCHAR(10) NOT NULL DEFAULT 'ORIGINAL',                 -- ORIGINAL / REPOST
    creation_statement  VARCHAR(30) NOT NULL DEFAULT 'NONE',                     -- NONE / AI_ASSISTED / NETWORK_SOURCED / PERSONAL_OPINION
    source_type         VARCHAR(15) NOT NULL DEFAULT 'MANUAL',                   -- MANUAL / AI_GENERATED
    source_url          VARCHAR(500),                                            -- 转载来源链接（article_type=REPOST 时必填）

    view_count          INT NOT NULL DEFAULT 0,                                  -- 阅读量（冗余计数）
    like_count          INT NOT NULL DEFAULT 0,                                  -- 点赞数（冗余计数）
    comment_count       INT NOT NULL DEFAULT 0,                                  -- 评论数（冗余计数）
    bookmark_count      INT NOT NULL DEFAULT 0,                                  -- 收藏数（冗余计数）

    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),                        -- 创建时间
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW(),                        -- 更新时间
    published_at        TIMESTAMP                                                -- 发布时间
);

-- ============================================
-- 标签
-- ============================================

CREATE TABLE IF NOT EXISTS tags (
                                    id              BIGSERIAL PRIMARY KEY,
                                    name            VARCHAR(30) NOT NULL,
    slug            VARCHAR(30) NOT NULL UNIQUE,
    color           VARCHAR(7),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
    );

-- ============================================
-- 6. 文章-标签关联
-- ============================================

CREATE TABLE IF NOT EXISTS article_tags (
    article_id      BIGINT NOT NULL,
    tag_id          BIGINT NOT NULL,
    PRIMARY KEY (article_id, tag_id)
);

CREATE INDEX IF NOT EXISTS idx_article_tags_tag ON article_tags(tag_id);

-- ============================================
-- 分组
-- ============================================

CREATE TABLE IF NOT EXISTS groups (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    name        VARCHAR(50)  NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, name)
);

CREATE TABLE IF NOT EXISTS article_groups (
    article_id  BIGINT NOT NULL,
    group_id    BIGINT NOT NULL,
    PRIMARY KEY (article_id, group_id)
);

CREATE INDEX IF NOT EXISTS idx_groups_user            ON groups(user_id);
CREATE INDEX IF NOT EXISTS idx_article_groups_article ON article_groups(article_id);
CREATE INDEX IF NOT EXISTS idx_article_groups_group   ON article_groups(group_id);

-- ============================================
-- 14. AI — LLM Provider 配置
-- ============================================

CREATE TABLE IF NOT EXISTS llm_provider_config (
    id                      VARCHAR(30)   PRIMARY KEY,
    base_url                VARCHAR(300)  NOT NULL,
    api_key_ciphertext      TEXT,
    api_key_nonce           VARCHAR(50),
    model                   VARCHAR(100)  NOT NULL,
    embedding_model         VARCHAR(100),
    embedding_dimensions    INT,
    supports_embedding      BOOLEAN       NOT NULL DEFAULT FALSE,
    temperature             DOUBLE PRECISION,
    enabled                 BOOLEAN       NOT NULL DEFAULT TRUE,
    builtin                 BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP     NOT NULL DEFAULT NOW()
);

-- ============================================
-- 15. AI — LLM 全局设置（单例表）
-- ============================================

CREATE TABLE IF NOT EXISTS llm_global_setting (
    id                              BIGINT PRIMARY KEY DEFAULT 1,
    default_chat_provider_id        VARCHAR(64) NOT NULL,
    default_embedding_provider_id   VARCHAR(64) NOT NULL,
    created_at                      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at                      TIMESTAMP NOT NULL DEFAULT NOW(),
    CHECK (id = 1)
);

-- ============================================
-- 存储配置
-- ============================================
CREATE TABLE IF NOT EXISTS storage_config (
    id                              VARCHAR(30)   PRIMARY KEY,
    user_id                         BIGINT        NOT NULL,
    provider_type                   VARCHAR(30)   NOT NULL DEFAULT 'aliyun-oss',
    endpoint                        VARCHAR(300)  NOT NULL,
    bucket_name                     VARCHAR(100)  NOT NULL,
    access_key_id_ciphertext        TEXT          NOT NULL,
    access_key_id_nonce              VARCHAR(50)   NOT NULL,
    access_key_secret_ciphertext     TEXT          NOT NULL,
    access_key_secret_nonce          VARCHAR(50)   NOT NULL,
    enabled                         BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at                      TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at                      TIMESTAMP     NOT NULL DEFAULT NOW()
);
