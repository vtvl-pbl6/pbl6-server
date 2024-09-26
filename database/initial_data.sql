--
-- *************************** CREATE ENUMS ***************************
--

create type visibility as enum ('PUBLIC', 'PRIVATE', 'FRIEND_ONLY');

create type account_status as enum ('ACTIVE', 'INACTIVE');

create type account_role as enum ('ADMIN', 'USER');

create type account_gender as enum ('MALE', 'FEMALE', 'OTHER');

create type thread_status as enum ('CREATING', 'CREATE_DONE', 'PENDING', 'HIDE');


--
-- *************************** CREATE TABLES ***************************
--

-- File table
create table if not exists files
(
    id          bigserial
        primary key,
    name        varchar(400)                        not null,
    url         varchar(400)                        not null,
    size        integer                             not null,
    mime_type   varchar(255)                        not null,
    nsfw_result jsonb,
    created_at  timestamp default CURRENT_TIMESTAMP not null,
    updated_at  timestamp,
    deleted_at  timestamp
);

-- Account table
create table if not exists accounts
(
    id           bigserial
        primary key,
    email        varchar(255)                                    not null
        unique,
    password     varchar(255)                                    not null,
    first_name   varchar(255)                                    not null,
    last_name    varchar(255)                                    not null,
    status       account_status default 'ACTIVE'::account_status not null,
    role         account_role                                    not null,
    display_name varchar(255)                                    not null
        unique,
    birthday     timestamp,
    gender       account_gender,
    bio          text,
    avatar       bigint
        unique
        references files,
    visibility   visibility     default 'PUBLIC'::visibility     not null,
    language     varchar(10)    default 'vi'::character varying  not null,
    created_at   timestamp      default CURRENT_TIMESTAMP        not null,
    updated_at   timestamp,
    deleted_at   timestamp
);

-- Refresh token table
create table if not exists refresh_tokens
(
    id         bigserial
        primary key,
    account_id bigint                              not null
        references accounts(id) on delete cascade ,
    token      text                        not null,
    created_at timestamp default CURRENT_TIMESTAMP not null,
    updated_at timestamp,
    deleted_at timestamp
);

-- Follower table
create table if not exists followers
(
    id          bigserial
        primary key,
    user_id     bigint                              not null
        references accounts(id) on delete cascade,
    follower_id bigint                              not null
        references accounts(id) on delete cascade,
    created_at  timestamp default CURRENT_TIMESTAMP not null,
    updated_at  timestamp,
    deleted_at  timestamp
);

-- Notification table
create table if not exists notifications
(
    id          bigserial
        primary key,
    sender_id   bigint
        references accounts(id) on delete set null,
    receiver_id bigint                              not null
        references accounts(id) on delete cascade,
    object_id   bigint,
    type        varchar(255)                        not null,
    content     varchar(1000)                       not null,
    is_read     boolean   default false             not null,
    created_at  timestamp default CURRENT_TIMESTAMP not null,
    updated_at  timestamp,
    deleted_at  timestamp
);

-- Thread table
create table if not exists threads
(
    id               bigserial
        primary key,
    author_id        bigint                                          not null
        references accounts(id) on delete cascade,
    parent_thread_id bigint
        references threads(id) on delete cascade,
    content          text,
    hos_result       jsonb,
    reaction_num     integer       default 0                         not null,
    shared_num       integer       default 0                         not null,
    is_pin           boolean       default false                     not null,
    status           thread_status default 'CREATING'::thread_status not null,
    visibility       visibility    default 'PUBLIC'::visibility      not null,
    created_at       timestamp     default CURRENT_TIMESTAMP         not null,
    updated_at       timestamp,
    deleted_at       timestamp
);

-- Thread sharer table
create table if not exists thread_sharers
(
    id         bigserial
        primary key,
    thread_id  bigint                              not null
        references threads(id) on delete cascade,
    user_id    bigint                              not null
        references accounts(id) on delete cascade,
    created_at timestamp default CURRENT_TIMESTAMP not null,
    updated_at timestamp,
    deleted_at timestamp
);

-- Thread file table
create table if not exists thread_files
(
    id         bigserial
        primary key,
    thread_id  bigint                              not null
        references threads(id) on delete cascade,
    file_id    bigint                              not null
        unique
        references files,
    created_at timestamp default CURRENT_TIMESTAMP not null,
    updated_at timestamp,
    deleted_at timestamp
);


--
-- *************************** CREATE INDEXES ***************************
--

-- Account table indexes
create index if not exists idx_accounts_email on accounts (email);

create index if not exists idx_accounts_display_name on accounts (display_name);

create index if not exists idx_accounts_status on accounts (status);

-- Refresh token table indexes
create index if not exists idx_refresh_tokens_account_id on refresh_tokens (account_id);

-- Follower table indexes
create index if not exists idx_followers_user_id on followers (user_id);

create index if not exists idx_followers_follower_id on followers (follower_id);

-- Notification table indexes
create index if not exists idx_notifications_sender_id on notifications (sender_id);

create index if not exists idx_notifications_receiver_id_and_is_read on notifications (receiver_id, is_read);

-- Thread table indexes
create index if not exists idx_threads_author_id on threads (author_id);

create index if not exists idx_threads_parent_thread_id_and_is_pin on threads (parent_thread_id, is_pin);

create index if not exists idx_threads_status on threads (status);

-- Thread sharer table indexes
create index if not exists idx_thread_sharers_thread_id on thread_sharers (thread_id);

create index if not exists idx_thread_sharers_user_id on thread_sharers (user_id);

-- Thread file table indexes
create index if not exists idx_thread_files_thread_id on thread_files (thread_id);