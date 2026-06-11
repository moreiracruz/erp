-- V1: Auth tables — usuarios and refresh_tokens

CREATE TABLE usuarios (
    id              BIGSERIAL       PRIMARY KEY,
    uuid            UUID            NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    username        VARCHAR(255)    NOT NULL UNIQUE,
    password_hash   VARCHAR(255)    NOT NULL,
    role            VARCHAR(20)     NOT NULL CHECK (role IN ('ROLE_MANAGER','ROLE_CASHIER','ROLE_STOCK','ROLE_FINANCE')),
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    failed_attempts INT             NOT NULL DEFAULT 0,
    locked_until    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE TABLE refresh_tokens (
    id              BIGSERIAL       PRIMARY KEY,
    token_hash      VARCHAR(64)     NOT NULL UNIQUE,   -- SHA-256 hex
    usuario_uuid    UUID            NOT NULL REFERENCES usuarios(uuid),
    expires_at      TIMESTAMPTZ     NOT NULL,
    revoked_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_usuario ON refresh_tokens(usuario_uuid);
