-- monewping 스키마 생성 및 경로 설정
CREATE SCHEMA IF NOT EXISTS monewping;

SET search_path TO monewping;

-- 테이블 초기화 (참조 무결성 순서 고려)
DROP TABLE IF EXISTS comment_likes CASCADE;
DROP TABLE IF EXISTS comments CASCADE;
DROP TABLE IF EXISTS article_views CASCADE;
DROP TABLE IF EXISTS articles CASCADE;
DROP TABLE IF EXISTS keywords CASCADE;
DROP TABLE IF EXISTS interest_subscriptions CASCADE;
DROP TABLE IF EXISTS interests CASCADE;
DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS users CASCADE;


-- users Table
CREATE TABLE users
(
    -- Primary Key
    id UUID PRIMARY KEY,

    -- Columns
    email VARCHAR(100) NOT NULL,
    nickname VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,

    -- Unique Key
    CONSTRAINT uk_user_email UNIQUE (email) );


-- interests Table
CREATE TABLE interests
(
    -- Primary Key
    id               UUID PRIMARY KEY,

    -- Columns
    name             VARCHAR(100) NOT NULL,
    subscriber_count BIGINT       NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL,
    updated_at       TIMESTAMPTZ  NOT NULL

);
-- keywords Table
    CREATE TABLE keywords
        (
        -- Primary Key
        id UUID PRIMARY KEY,

        -- Columns
        name TEXT NOT NULL, created_at TIMESTAMPTZ NOT NULL,

        -- Foreign Key
        interest_id UUID NOT NULL,
        FOREIGN KEY (interest_id) REFERENCES interests (id) ON DELETE CASCADE
        );


-- interest_subscriptions Table
CREATE TABLE interest_subscriptions
(
    -- Primary Key
    id UUID PRIMARY KEY,

    -- Columns
    created_at TIMESTAMPTZ NOT NULL,

    -- Foreign Keys
    interest_id UUID NOT NULL,
    user_id UUID NOT NULL,
    FOREIGN KEY (interest_id) REFERENCES interests (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,

-- Unique Constraint (중복 구독 방지)
    CONSTRAINT uk_interest_user UNIQUE (interest_id, user_id) );


-- articles Table
CREATE TABLE articles
(
    -- Primary Key
    id UUID PRIMARY KEY,

    -- Columns
    source VARCHAR(10) NOT NULL,
    original_link VARCHAR(300) NOT NULL,
    title VARCHAR(100) NOT NULL,
    summary TEXT NOT NULL,
    published_at DATE NOT NULL,
    comment_count BIGINT NOT NULL,
    view_count BIGINT NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,

    -- Unique Key
    CONSTRAINT uk_article_link UNIQUE (original_link),

    -- Foreign Key
    interest_id   UUID         NOT NULL,
    FOREIGN KEY (interest_id) REFERENCES interests (id) ON DELETE CASCADE
);

-- article_views Table
CREATE TABLE article_views
(
    -- Primary Key
    id UUID PRIMARY KEY,

    -- Columns
    viewed_at TIMESTAMPTZ NOT NULL,

    -- Foreign keys
    user_id    UUID        NOT NULL,
    article_id UUID        NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (article_id) REFERENCES articles (id) ON DELETE CASCADE
);


-- comments Table
CREATE TABLE comments
(
    -- Primary Key
    id UUID PRIMARY KEY,

    -- Columns
    user_nickname VARCHAR(255),
    content TEXT NOT NULL,
    like_count BIGINT,
    liked_by_me BOOLEAN,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,

    -- Foreign Keys
    article_id    UUID         NOT NULL,
    user_id       UUID         NOT NULL,
    FOREIGN KEY (article_id) REFERENCES articles (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);


-- comment_likes Table
CREATE TABLE comment_likes
(
    -- Primary Key
    id         UUID PRIMARY KEY,

    -- Columns
    created_at TIMESTAMPTZ NOT NULL,

    -- Foreign Keys
    comment_id UUID NOT NULL,
    liked_id UUID NOT NULL,
    FOREIGN KEY (comment_id) REFERENCES comments (id) ON DELETE CASCADE,
    FOREIGN KEY (liked_id) REFERENCES users (id) ON DELETE CASCADE
);


-- notifications Table
CREATE TABLE notifications
(
    -- Primary Key
    id UUID PRIMARY KEY,

    -- Columns
    content VARCHAR(150) NOT NULL,
    resource_id UUID,
    resource_type VARCHAR(10) NOT NULL,
    confirmed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    -- Foreign Key
    user_id UUID,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL,

    -- Check constraint
    CONSTRAINT chk_notification_resource_type
        CHECK (resource_type IN ('Article', 'Comment'))
);