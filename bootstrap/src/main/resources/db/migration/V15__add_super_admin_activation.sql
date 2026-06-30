ALTER TABLE usuarios DROP CONSTRAINT IF EXISTS usuarios_role_check;

ALTER TABLE usuarios
    ADD CONSTRAINT usuarios_role_check
        CHECK (role IN ('ROLE_USER', 'ROLE_SUPER_ADMIN', 'ROLE_MANAGER', 'ROLE_CASHIER', 'ROLE_STOCK', 'ROLE_FINANCE'));

ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS status VARCHAR(40);

UPDATE usuarios
   SET status = CASE WHEN active THEN 'ACTIVE' ELSE 'INACTIVE' END
 WHERE status IS NULL;

ALTER TABLE usuarios
    ALTER COLUMN status SET NOT NULL;

ALTER TABLE usuarios DROP CONSTRAINT IF EXISTS usuarios_status_check;

ALTER TABLE usuarios
    ADD CONSTRAINT usuarios_status_check
        CHECK (status IN ('PENDING_ACTIVATION', 'ACTIVE', 'INACTIVE'));

CREATE TABLE user_activation_tokens (
    id              BIGSERIAL       PRIMARY KEY,
    uuid            UUID            NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    usuario_uuid    UUID            NOT NULL REFERENCES usuarios(uuid),
    token_hash      VARCHAR(64)     NOT NULL UNIQUE,
    purpose         VARCHAR(60)     NOT NULL,
    expires_at      TIMESTAMPTZ     NOT NULL,
    used_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_user_activation_tokens_usuario ON user_activation_tokens(usuario_uuid);
CREATE INDEX idx_user_activation_tokens_expires_at ON user_activation_tokens(expires_at);
CREATE UNIQUE INDEX ux_user_activation_tokens_active
    ON user_activation_tokens(usuario_uuid, purpose)
    WHERE used_at IS NULL;
