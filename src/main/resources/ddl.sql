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
    updated_at       TIMESTAMPTZ  NOT NULL,
    version       BIGINT       NOT NULL DEFAULT 0
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
    id            UUID PRIMARY KEY,

    -- Columns
    source        VARCHAR(30)  NOT NULL,
    original_link VARCHAR(300) NOT NULL,
    title         VARCHAR(100) NOT NULL,
    summary       TEXT         NOT NULL,
    published_at  DATE         NOT NULL,
    comment_count BIGINT       NOT NULL,
    view_count    BIGINT       NOT NULL,
    is_deleted    BOOLEAN DEFAULT FALSE,
    created_at    TIMESTAMPTZ  NOT NULL,
    version       BIGINT       NOT NULL DEFAULT 0,

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

-- 관심사 데이터 삽입
INSERT INTO interests (id, name, subscriber_count, created_at, updated_at, version) VALUES
('550e8400-e29b-41d4-a716-446655440001', '개인 지갑', 0, NOW(), NOW(), 0),
('550e8400-e29b-41d4-a716-446655440002', '게임', 0, NOW(), NOW(), 0),
('550e8400-e29b-41d4-a716-446655440003', '경제', 0, NOW(), NOW(), 0),
('550e8400-e29b-41d4-a716-446655440004', '구기종목', 0, NOW(), NOW(), 0),
('550e8400-e29b-41d4-a716-446655440005', '닌텐도', 0, NOW(), NOW(), 0),
('550e8400-e29b-41d4-a716-446655440006', '스포츠', 0, NOW(), NOW(), 0),
('550e8400-e29b-41d4-a716-446655440007', '정치', 0, NOW(), NOW(), 0);

-- 키워드 데이터 삽입
INSERT INTO keywords (id, name, interest_id, created_at) VALUES
-- 개인 지갑 키워드
('550e8400-e29b-41d4-a716-446655440101', '트러스트 플렛', '550e8400-e29b-41d4-a716-446655440001', NOW()),
('550e8400-e29b-41d4-a716-446655440102', '토큰 포켓', '550e8400-e29b-41d4-a716-446655440001', NOW()),
('550e8400-e29b-41d4-a716-446655440103', '메타마스크', '550e8400-e29b-41d4-a716-446655440001', NOW()),

-- 게임 키워드
('550e8400-e29b-41d4-a716-446655440201', '리그오브레전드', '550e8400-e29b-41d4-a716-446655440002', NOW()),
('550e8400-e29b-41d4-a716-446655440202', '오버워치2', '550e8400-e29b-41d4-a716-446655440002', NOW()),
('550e8400-e29b-41d4-a716-446655440203', '스팅', '550e8400-e29b-41d4-a716-446655440002', NOW()),
('550e8400-e29b-41d4-a716-446655440204', 'PUBG', '550e8400-e29b-41d4-a716-446655440002', NOW()),
('550e8400-e29b-41d4-a716-446655440205', '동물의숲', '550e8400-e29b-41d4-a716-446655440002', NOW()),
('550e8400-e29b-41d4-a716-446655440206', '마비노기 모바일', '550e8400-e29b-41d4-a716-446655440002', NOW()),

-- 경제 키워드
('550e8400-e29b-41d4-a716-446655440301', '코스피', '550e8400-e29b-41d4-a716-446655440003', NOW()),
('550e8400-e29b-41d4-a716-446655440302', '이더리움', '550e8400-e29b-41d4-a716-446655440003', NOW()),
('550e8400-e29b-41d4-a716-446655440303', '비트코인', '550e8400-e29b-41d4-a716-446655440003', NOW()),
('550e8400-e29b-41d4-a716-446655440304', '암호화폐', '550e8400-e29b-41d4-a716-446655440003', NOW()),
('550e8400-e29b-41d4-a716-446655440305', '나스닥', '550e8400-e29b-41d4-a716-446655440003', NOW()),
('550e8400-e29b-41d4-a716-446655440306', '코스닥', '550e8400-e29b-41d4-a716-446655440003', NOW()),

-- 구기종목 키워드
('550e8400-e29b-41d4-a716-446655440401', '풋살', '550e8400-e29b-41d4-a716-446655440004', NOW()),
('550e8400-e29b-41d4-a716-446655440402', '비치발리볼', '550e8400-e29b-41d4-a716-446655440004', NOW()),
('550e8400-e29b-41d4-a716-446655440403', '야구', '550e8400-e29b-41d4-a716-446655440004', NOW()),
('550e8400-e29b-41d4-a716-446655440404', '농구', '550e8400-e29b-41d4-a716-446655440004', NOW()),
('550e8400-e29b-41d4-a716-446655440405', '축구', '550e8400-e29b-41d4-a716-446655440004', NOW()),

-- 닌텐도 키워드
('550e8400-e29b-41d4-a716-446655440501', '동물의 숲', '550e8400-e29b-41d4-a716-446655440005', NOW()),
('550e8400-e29b-41d4-a716-446655440502', '마리오 카트', '550e8400-e29b-41d4-a716-446655440005', NOW()),
('550e8400-e29b-41d4-a716-446655440503', '스위치', '550e8400-e29b-41d4-a716-446655440005', NOW()),

-- 스포츠 키워드
('550e8400-e29b-41d4-a716-446655440601', '허들', '550e8400-e29b-41d4-a716-446655440006', NOW()),
('550e8400-e29b-41d4-a716-446655440602', '축구', '550e8400-e29b-41d4-a716-446655440006', NOW()),
('550e8400-e29b-41d4-a716-446655440603', '농구', '550e8400-e29b-41d4-a716-446655440006', NOW()),
('550e8400-e29b-41d4-a716-446655440604', '야구', '550e8400-e29b-41d4-a716-446655440006', NOW()),
('550e8400-e29b-41d4-a716-446655440605', '배구', '550e8400-e29b-41d4-a716-446655440006', NOW()),
('550e8400-e29b-41d4-a716-446655440606', '이스포츠', '550e8400-e29b-41d4-a716-446655440006', NOW()),
('550e8400-e29b-41d4-a716-446655440607', '풋볼', '550e8400-e29b-41d4-a716-446655440006', NOW()),
('550e8400-e29b-41d4-a716-446655440608', '육상', '550e8400-e29b-41d4-a716-446655440006', NOW()),
('550e8400-e29b-41d4-a716-446655440609', '수영', '550e8400-e29b-41d4-a716-446655440006', NOW()),
('550e8400-e29b-41d4-a716-446655440610', '계주', '550e8400-e29b-41d4-a716-446655440006', NOW()),

-- 정치 키워드
('550e8400-e29b-41d4-a716-446655440701', '연개소문', '550e8400-e29b-41d4-a716-446655440007', NOW()),
('550e8400-e29b-41d4-a716-446655440702', '왕자의 난', '550e8400-e29b-41d4-a716-446655440007', NOW()),
('550e8400-e29b-41d4-a716-446655440703', '이방원', '550e8400-e29b-41d4-a716-446655440007', NOW()),
('550e8400-e29b-41d4-a716-446655440704', '을지문덕', '550e8400-e29b-41d4-a716-446655440007', NOW()),
('550e8400-e29b-41d4-a716-446655440705', '이성계', '550e8400-e29b-41d4-a716-446655440007', NOW());

-- -- 관심사 추가
-- INSERT INTO interests (id, name, subscriber_count, created_at, updated_at) VALUES
-- ('11111111-1111-1111-1111-111111111111', '프로젝트', 0, now(), now()),
-- ('22222222-2222-2222-2222-222222222222', '팀 소개', 0, now(), now());
--
-- -- 기사 데이터 삽입
-- INSERT INTO articles (
--     id, source, original_link, title, summary, published_at,
--     comment_count, view_count, is_deleted, created_at, version, interest_id
-- ) VALUES
--       -- 프로젝트
--       ('a0000000-0000-0000-0000-000000000001', 'Project', 'https://news.economy/1', '[모뉴] - 모두의 뉴스', '📰 흩어진 뉴스를 한 곳에, 관심 있는 주제만 모아보세요!
-- 모뉴(MoNew)는 다양한 뉴스 출처를 통합하여 관심사 기반으로 뉴스를 저장하는 뉴스 통합 관리 플랫폼입니다.
-- 관심 있는 주제의 기사가 등록되면 실시간 알림을 받고, 댓글과 좋아요를 통해 다른 사용자와 의견을 나눌 수 있는 소셜 기능도 함께 제공됩니다. 🗞️💬', current_date, 0, 11, false, now(), 0, '11111111-1111-1111-1111-111111111111'),
--       ('a0000000-0000-0000-0000-000000000002', 'Project', 'https://news.economy/2', 'HR BANK', '🏢 기업의 핵심 자산, 인적 자원을 체계적으로 관리하세요! HR Bank는 인사 데이터를 안전하고 효율적으로 관리할 수 있도록 설계된 Open EMS(Enterprise Management System)입니다. 대량의 데이터를 안정적으로 처리할 수 있는 Batch 시스템을 기반으로 부서 및 직원 정보를 체계적으로 운영할 수 있으며, 백업 자동화, 이력 관리, 대시보드 제공을 통해 기업 인사 관리를 더욱 효과적으로 지원합니다. 📊💼', current_date - INTERVAL '1 day', 0, 7, false, now(), 0, '11111111-1111-1111-1111-111111111111'),
--       ('a0000000-0000-0000-0000-000000000003', 'Project', 'https://news.economy/3', 'FINDEX', '💰 외부 금융 API 연동을 통해 📈 사용자 맞춤형 금융 데이터 분석을 제공합니다. 🔐 데이터 보안과 📊 시각화를 고려하여 사용자 중심의 금융 서비스를 구현합니다.', current_date - INTERVAL '2 days', 0, 9, false, now(), 0, '11111111-1111-1111-1111-111111111111'),
--
--       -- 팀 소개
--       ('b0000000-0000-0000-0000-000000000011', 'Team_4', 'https://news.tech/1', '[ 이건민 ] - Project Manager', '🧭 뉴스 기사 관리 - 외부 API 연동으로 관심사 키워드에 해당하는 기사를 수집하고, 출처별 조회 및 필터링이 가능하도록 설계했습니다. 💾 매일 데이터를 백업하여 유실된 기사를 복원할 수 있습니다. 😂 여담으로 프로젝트 계속 침몰시키는 중인데 팀원들이 계속 인양해줘서 고맙다고 몰래 전했다고 합니다.', current_date, 0, 5, false, now(), 0, '22222222-2222-2222-2222-222222222222'),
--       ('b0000000-0000-0000-0000-000000000022', 'Team_4', 'https://news.tech/2', '[ 임정현 ] - Database 관리', '🗂️ 관심사 관리 - 관심사 등록, 검색, 구독, 알림까지 사용자의 관심 흐름을 설계했습니다. 🔍 키워드 기반 뉴스 수집 구조를 직접 구현하였습니다. 🎙️ 소문에 의하면 마이크와 싸운 후 화해하지 않아서 회의 도중 가끔 소리가 들리지 않는다고 합니다.', current_date - INTERVAL '1 day', 0, 3, false, now(), 0, '22222222-2222-2222-2222-222222222222'),
--       ('b0000000-0000-0000-0000-000000000033', 'Team_4', 'https://news.tech/3', '[ 백은호 ] - Git 형상 관리자', '👤 사용자 및 활동 내역 관리 - 사용자 등록, 로그인, 정보 수정 기능을 통해 사용자 정보를 관리합니다. 📝 활동 내역을 취합하여 확인할 수 있도록 구현했습니다. 🐰 제보에 따르면 코드래빗, 워크플로우 등 형상관리에 전반적인 부분을 설계했다고 합니다.', current_date - INTERVAL '2 days', 0, 8, false, now(), 0, '22222222-2222-2222-2222-222222222222'),
--       ('b0000000-0000-0000-0000-000000000044', 'Team_4', 'https://news.tech/4', '[ 김유빈 ] - Notion 회의록 관리', '🔔 알림 관리 - 알림 등록과 사용자 별 알림 정보 조회 및 수정, 삭제를 통해 알림 리소스를 관리하는 비즈니스 로직을 설계했습니다. 🐱 민원 제보에 따르면 고양이 이모지를 자주 사용해 고양이파라는 의혹이 있습니다.', current_date - INTERVAL '2 days', 0, 8, false, now(), 0, '22222222-2222-2222-2222-222222222222'),
--       ('b0000000-0000-0000-0000-000000000055', 'Team_4', 'https://news.tech/5', '[ 김민준 ] - Notion 문서 관리', '💬 댓글 관리 - 뉴스 기사별 댓글을 작성할 수 있으며, 작성자의 댓글만 수정할 수 있습니다. 🛌 익명 제보에 따르면 선호 취미가 취침이라는 소문이 있습니다.', current_date - INTERVAL '2 days', 0, 8, false, now(), 0, '22222222-2222-2222-2222-222222222222');