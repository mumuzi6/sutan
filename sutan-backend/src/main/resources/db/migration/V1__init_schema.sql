-- ==========================================================
-- 诉探 V1 初始化：pgvector 扩展 + 核心表
-- ==========================================================

create extension if not exists vector;

-- 用户
create table if not exists users (
    id          bigserial primary key,
    open_id     varchar(128) unique,
    phone       varchar(32),
    role        varchar(32) not null default 'user',
    created_at  timestamptz not null default now()
);

-- 会话
create table if not exists sessions (
    id          bigserial primary key,
    user_id     bigint references users(id),
    title       varchar(256),
    model       varchar(64) default 'deepseek-chat',
    created_at  timestamptz not null default now()
);

-- 消息（含溯源引用，citations 存 JSON）
create table if not exists messages (
    id              bigserial primary key,
    session_id      bigint not null references sessions(id),
    role            varchar(16) not null,
    content         text not null,
    citations_json  jsonb,
    token_cost      int,
    created_at      timestamptz not null default now()
);
create index if not exists idx_messages_session on messages(session_id);

-- 法条（embedding 维度按 BGE-M3=1024，切换 DashScope(1536) 时改此处）
create table if not exists legal_articles (
    id          bigserial primary key,
    regulation  varchar(128),
    chapter     varchar(128),
    article_no  varchar(64),
    content     text not null,
    embedding   vector(1024)
);

-- 案例
create table if not exists cases (
    id              bigserial primary key,
    cause           varchar(128),
    case_no         varchar(128),
    key_points      text,
    source          varchar(256),
    embedding       vector(1024)
);

-- 真题
create table if not exists exam_questions (
    id              bigserial primary key,
    subject         varchar(64),
    year            int,
    stem            text not null,
    ref_answer      text,
    scoring_points  text,
    embedding       vector(1024)
);

-- 主观题作答与 AI 批改报告
create table if not exists submissions (
    id              bigserial primary key,
    user_id         bigint references users(id),
    question_id     bigint references exam_questions(id),
    answer          text not null,
    grade_report    jsonb,
    score           numeric(5,2),
    created_at      timestamptz not null default now()
);

-- 用量统计（定时聚合，admin 看板数据源）
create table if not exists usage_stats (
    id              bigserial primary key,
    stat_date       date not null,
    uv              int default 0,
    dau             int default 0,
    msg_count       int default 0,
    register_count  int default 0,
    top_questions   jsonb,
    unique (stat_date)
);
