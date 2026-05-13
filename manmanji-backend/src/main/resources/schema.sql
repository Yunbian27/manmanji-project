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
    points_balance  INT          NOT NULL DEFAULT 0,
    ai_quota        INT          NOT NULL DEFAULT 10,
    api_key_encrypted TEXT,
    api_key_provider  VARCHAR(20),
    status          VARCHAR(10)  NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

-- ============================================
-- 2. 关注关系
-- ============================================

CREATE TABLE IF NOT EXISTS user_follows (
    id              BIGSERIAL PRIMARY KEY,
    follower_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    following_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (follower_id, following_id),
    CHECK (follower_id <> following_id)
);

CREATE INDEX IF NOT EXISTS idx_follows_follower ON user_follows(follower_id);
CREATE INDEX IF NOT EXISTS idx_follows_following ON user_follows(following_id);

-- ============================================
-- 3. 文章分类
-- ============================================

CREATE TABLE IF NOT EXISTS categories (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(50)  NOT NULL,
    slug            VARCHAR(50)  NOT NULL UNIQUE,
    description     VARCHAR(200),
    parent_id       BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    sort_order      INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_categories_slug ON categories(slug);

-- ============================================
-- 4. 标签
-- ============================================

CREATE TABLE IF NOT EXISTS tags (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(30) NOT NULL,
    slug            VARCHAR(30) NOT NULL UNIQUE,
    color           VARCHAR(7),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================
-- 5. 文章
-- ============================================

CREATE TABLE IF NOT EXISTS articles (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(200) NOT NULL,
    slug            VARCHAR(200) NOT NULL UNIQUE,
    content         TEXT,
    summary         VARCHAR(500),
    cover_url       VARCHAR(500),
    author_id       BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id     BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    status          VARCHAR(15) NOT NULL DEFAULT 'DRAFT',
    source_type     VARCHAR(10) NOT NULL DEFAULT 'MANUAL',
    source_prompt   TEXT,
    view_count      INT NOT NULL DEFAULT 0,
    like_count      INT NOT NULL DEFAULT 0,
    comment_count   INT NOT NULL DEFAULT 0,
    bookmark_count  INT NOT NULL DEFAULT 0,
    is_original     BOOLEAN NOT NULL DEFAULT TRUE,
    source_url      VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    published_at    TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_articles_slug ON articles(slug);
CREATE INDEX IF NOT EXISTS idx_articles_author ON articles(author_id);
CREATE INDEX IF NOT EXISTS idx_articles_category ON articles(category_id);
CREATE INDEX IF NOT EXISTS idx_articles_status ON articles(status);
CREATE INDEX IF NOT EXISTS idx_articles_source_type ON articles(source_type);
CREATE INDEX IF NOT EXISTS idx_articles_published_at ON articles(published_at DESC);
CREATE INDEX IF NOT EXISTS idx_articles_created_at ON articles(created_at DESC);

-- ============================================
-- 6. 文章-标签关联
-- ============================================

CREATE TABLE IF NOT EXISTS article_tags (
    article_id      BIGINT NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    tag_id          BIGINT NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (article_id, tag_id)
);

CREATE INDEX IF NOT EXISTS idx_article_tags_tag ON article_tags(tag_id);

-- ============================================
-- 7. 文章版本历史
-- ============================================

CREATE TABLE IF NOT EXISTS article_versions (
    id              BIGSERIAL PRIMARY KEY,
    article_id      BIGINT NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    title           VARCHAR(200) NOT NULL,
    content         TEXT,
    version_number  INT NOT NULL,
    change_summary  VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_versions_article ON article_versions(article_id, version_number);

-- ============================================
-- 8. 评论（两层嵌套）
-- ============================================

CREATE TABLE IF NOT EXISTS comments (
    id              BIGSERIAL PRIMARY KEY,
    article_id      BIGINT NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    parent_id       BIGINT REFERENCES comments(id) ON DELETE CASCADE,
    content         TEXT NOT NULL,
    is_deleted      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_comments_article ON comments(article_id, created_at);
CREATE INDEX IF NOT EXISTS idx_comments_parent ON comments(parent_id);

-- ============================================
-- 9. 点赞
-- ============================================

CREATE TABLE IF NOT EXISTS article_likes (
    id              BIGSERIAL PRIMARY KEY,
    article_id      BIGINT NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (article_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_likes_article ON article_likes(article_id);
CREATE INDEX IF NOT EXISTS idx_likes_user ON article_likes(user_id);

-- ============================================
-- 10. 收藏
-- ============================================

CREATE TABLE IF NOT EXISTS bookmarks (
    id              BIGSERIAL PRIMARY KEY,
    article_id      BIGINT NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (article_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_bookmarks_user ON bookmarks(user_id, created_at DESC);

-- ============================================
-- 11. AI — 文章向量嵌入
-- ============================================

CREATE TABLE IF NOT EXISTS article_embeddings (
    id              BIGSERIAL PRIMARY KEY,
    article_id      BIGINT NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    chunk_index     INT NOT NULL,
    chunk_text      TEXT NOT NULL,
    chunk_hash      VARCHAR(64) NOT NULL UNIQUE,
    embedding       vector(1536),
    token_count     INT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_embeddings_article ON article_embeddings(article_id);
CREATE INDEX IF NOT EXISTS idx_embeddings_hash ON article_embeddings(chunk_hash);

-- ============================================
-- 12. AI — 对话
-- ============================================

CREATE TABLE IF NOT EXISTS ai_conversations (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title           VARCHAR(200),
    model_name      VARCHAR(50),
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_conversations_user ON ai_conversations(user_id, updated_at DESC);

-- ============================================
-- 13. AI — 消息
-- ============================================

CREATE TABLE IF NOT EXISTS ai_messages (
    id                BIGSERIAL PRIMARY KEY,
    conversation_id   BIGINT NOT NULL REFERENCES ai_conversations(id) ON DELETE CASCADE,
    role              VARCHAR(20) NOT NULL,
    content           TEXT NOT NULL,
    references_json   JSONB,
    token_count       INT,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_messages_conversation ON ai_messages(conversation_id, created_at);

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
-- 16. 积分流水
-- ============================================

CREATE TABLE IF NOT EXISTS points_transactions (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount          INT NOT NULL,
    type            VARCHAR(30) NOT NULL,
    description     VARCHAR(200),
    reference_id    BIGINT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_points_user ON points_transactions(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_points_type ON points_transactions(type);

-- ============================================
-- 17. 秒杀活动
-- ============================================

CREATE TABLE IF NOT EXISTS flash_sales (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(100) NOT NULL,
    description     VARCHAR(500),
    start_time      TIMESTAMP NOT NULL,
    end_time        TIMESTAMP NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_flash_sales_status ON flash_sales(status, start_time, end_time);

-- ============================================
-- 18. 秒杀商品
-- ============================================

CREATE TABLE IF NOT EXISTS flash_sale_items (
    id              BIGSERIAL PRIMARY KEY,
    flash_sale_id   BIGINT NOT NULL REFERENCES flash_sales(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    description     VARCHAR(300),
    total_stock     INT NOT NULL,
    sold_count      INT NOT NULL DEFAULT 0,
    per_user_limit  INT NOT NULL DEFAULT 1,
    points_cost     INT NOT NULL DEFAULT 0,
    ai_quota_reward INT NOT NULL DEFAULT 0,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_flash_items_sale ON flash_sale_items(flash_sale_id);

-- ============================================
-- 19. 秒杀订单
-- ============================================

CREATE TABLE IF NOT EXISTS flash_sale_orders (
    id              BIGSERIAL PRIMARY KEY,
    flash_sale_id   BIGINT NOT NULL REFERENCES flash_sales(id) ON DELETE CASCADE,
    item_id         BIGINT NOT NULL REFERENCES flash_sale_items(id) ON DELETE CASCADE,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status          VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (item_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_flash_orders_user ON flash_sale_orders(user_id);

-- ============================================
-- 20. 优惠券模板
-- ============================================

CREATE TABLE IF NOT EXISTS coupons (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    description     VARCHAR(300),
    type            VARCHAR(10) NOT NULL,
    value           INT NOT NULL,
    min_points      INT NOT NULL DEFAULT 0,
    total_count     INT NOT NULL,
    received_count  INT NOT NULL DEFAULT 0,
    valid_days      INT NOT NULL DEFAULT 30,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ============================================
-- 21. 用户优惠券
-- ============================================

CREATE TABLE IF NOT EXISTS user_coupons (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    coupon_id       BIGINT NOT NULL REFERENCES coupons(id) ON DELETE CASCADE,
    status          VARCHAR(10) NOT NULL DEFAULT 'UNUSED',
    received_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    used_at         TIMESTAMP,
    expired_at      TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_user_coupons_user ON user_coupons(user_id, status);
