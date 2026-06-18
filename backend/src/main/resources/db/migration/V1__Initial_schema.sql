-- V1: Initial schema
-- Matches Hibernate-generated schema for all entities.

CREATE TABLE users (
    id              UUID        NOT NULL PRIMARY KEY,
    created_at      TIMESTAMP   NOT NULL,
    updated_at      TIMESTAMP   NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    display_name    VARCHAR(100) NOT NULL,
    avatar_url      VARCHAR(500),
    role            VARCHAR(255) NOT NULL DEFAULT 'USER',
    settings        JSONB,
    last_login_at   TIMESTAMP
);

CREATE TABLE refresh_tokens (
    id          UUID        NOT NULL PRIMARY KEY,
    user_id     UUID        NOT NULL,
    token       VARCHAR(500) NOT NULL UNIQUE,
    expires_at  TIMESTAMP   NOT NULL,
    revoked     BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP   NOT NULL
);

CREATE TABLE password_reset_tokens (
    id          UUID        NOT NULL PRIMARY KEY,
    token       VARCHAR(255) NOT NULL UNIQUE,
    email       VARCHAR(255) NOT NULL,
    expiry_date TIMESTAMP   NOT NULL,
    used        BOOLEAN     NOT NULL DEFAULT FALSE
);

CREATE TABLE containers (
    id              UUID        NOT NULL PRIMARY KEY,
    created_at      TIMESTAMP   NOT NULL,
    updated_at      TIMESTAMP   NOT NULL,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    type            VARCHAR(255) NOT NULL,
    status          VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    metadata        JSONB,
    env_vars        TEXT,
    resource_limits TEXT,
    labels          TEXT,
    error_message   VARCHAR(1000),
    started_at      TIMESTAMP,
    stopped_at      TIMESTAMP,
    pinned          BOOLEAN     NOT NULL DEFAULT FALSE,
    pinned_at       TIMESTAMP
);

CREATE TABLE tags (
    id        UUID        NOT NULL PRIMARY KEY,
    name      VARCHAR(100) NOT NULL,
    color     VARCHAR(7),
    owner_id  UUID        NOT NULL
);

CREATE TABLE container_tags (
    container_id UUID NOT NULL REFERENCES containers(id),
    tag_id       UUID NOT NULL REFERENCES tags(id),
    PRIMARY KEY (container_id, tag_id)
);

CREATE TABLE timeline_events (
    id              UUID        NOT NULL PRIMARY KEY,
    created_at      TIMESTAMP   NOT NULL,
    updated_at      TIMESTAMP   NOT NULL,
    container_id    UUID        NOT NULL,
    event_type      VARCHAR(255) NOT NULL,
    previous_status VARCHAR(255),
    new_status      VARCHAR(255),
    description     VARCHAR(500),
    metadata        JSONB
);

CREATE TABLE snapshots (
    id                      UUID        NOT NULL PRIMARY KEY,
    created_at              TIMESTAMP   NOT NULL,
    updated_at              TIMESTAMP   NOT NULL,
    container_id            UUID        NOT NULL,
    name                    VARCHAR(255),
    description             VARCHAR(500),
    captured_name           VARCHAR(255) NOT NULL,
    captured_description    TEXT,
    captured_type           VARCHAR(255) NOT NULL,
    captured_status         VARCHAR(255) NOT NULL,
    captured_metadata       JSONB,
    captured_env_vars       TEXT,
    captured_resource_limits TEXT,
    captured_labels         TEXT
);

CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_password_reset_tokens_token ON password_reset_tokens(token);
CREATE INDEX idx_containers_status ON containers(status);
CREATE INDEX idx_containers_type ON containers(type);
CREATE INDEX idx_tags_owner_id ON tags(owner_id);
CREATE INDEX idx_container_tags_container_id ON container_tags(container_id);
CREATE INDEX idx_container_tags_tag_id ON container_tags(tag_id);
CREATE INDEX idx_timeline_events_container_id ON timeline_events(container_id);
CREATE INDEX idx_timeline_events_event_type ON timeline_events(event_type);
CREATE INDEX idx_snapshots_container_id ON snapshots(container_id);
