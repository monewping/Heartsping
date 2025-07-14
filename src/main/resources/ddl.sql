-- user Table
CREATE TABLE "user" (
    -- Primary Key
    id UUID PRIMARY KEY,

    -- Column
    email VARCHAR(100) NOT NULL,
    nickname VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,

    -- Unique Key
    CONSTRAINT uk_user_email UNIQUE (email) );

    -- interest Table
CREATE TABLE interest (
    -- Primary Key
    id UUID PRIMARY KEY,

    -- Column
    name VARCHAR(100) NOT NULL,
    subscriber_count BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

-- keyword Table
CREATE TABLE keyword (
    -- Primary Key
    id UUID PRIMARY KEY,

    -- Column
    name TEXT NOT NULL, created_at TIMESTAMPTZ NOT NULL,

    -- Foreign Key
    interest_id UUID NOT NULL,
    FOREIGN KEY (interest_id) REFERENCES interest(id) ON DELETE CASCADE
);

-- interest_subscription Table
CREATE TABLE interest_subscription (
    -- Primary Key
    id UUID PRIMARY KEY,

    -- Column
    created_at TIMESTAMPTZ NOT NULL,

    -- Foreign Key
    interest_id UUID NOT NULL,
    user_id UUID NOT NULL,
    FOREIGN KEY (interest_id) REFERENCES interest (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE,

    -- Unique Constraint (중복 구독 방지)
    CONSTRAINT uk_interest_user UNIQUE (interest_id, user_id)
);

-- news_article Table
CREATE TABLE news_article (
    -- Primary Key
    id UUID PRIMARY KEY,

    -- Column
    source VARCHAR(10) NOT NULL,
    original_link VARCHAR(300) NOT NULL,
    title VARCHAR(100) NOT NULL,
    summary TEXT NOT NULL,
    published_at DATE NOT NULL,
    view_count BIGINT NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,

    -- Unique Key
    CONSTRAINT uk_news_article_link UNIQUE (original_link),

    -- Foreign Key
    interest_id UUID NOT NULL,
    FOREIGN KEY (interest_id) REFERENCES interest(id) ON DELETE CASCADE
);

-- news_view_history Table
CREATE TABLE news_view_history (
    -- Primary Key
    id UUID PRIMARY KEY,

    -- Column
    viewed_at TIMESTAMPTZ NOT NULL,

    -- Foreign key
    user_id UUID NOT NULL,
    article_id UUID NOT NULL,
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    FOREIGN KEY (article_id) REFERENCES news_article(id) ON DELETE CASCADE
);

-- comment Table
    CREATE TABLE comment (
    -- Primary Key
    id UUID PRIMARY KEY,

    -- Column
    user_nickname VARCHAR(255),
    content TEXT NOT NULL,
    like_count BIGINT,
    liked_by_me BOOLEAN,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,

    -- Foreign Key
    article_id UUID NOT NULL,
    user_id UUID NOT NULL,
    FOREIGN KEY (article_id) REFERENCES news_article(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE
);

-- comment_like Table
CREATE TABLE comment_like (
    -- Primary Key
    id UUID PRIMARY KEY,

    -- Column
    created_at TIMESTAMPTZ NOT NULL,

    -- Foreign Key
    comment_id UUID NOT NULL,
    liked_id UUID NOT NULL,
    FOREIGN KEY (comment_id)REFERENCES comment(id) ON DELETE CASCADE,
    FOREIGN KEY (liked_id) REFERENCES "user"(id) ON DELETE CASCADE
);

-- notification Table
CREATE TABLE notification (
    -- Primary Key
    id UUID PRIMARY KEY,

    -- Column
    content VARCHAR(150) NOT NULL,
    resource_id UUID,
    resource_type VARCHAR(10) NOT NULL,
    confirmed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    -- Foreign Key
    user_id UUID NOT NULL, FOREIGN KEY (user_id) REFERENCES "user" (id),

    -- Check constraint
    CONSTRAINT chk_notification_resource_type
    CHECK (resource_type IN ('Article','Comment'))
);