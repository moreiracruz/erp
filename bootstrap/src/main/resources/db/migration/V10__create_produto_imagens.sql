-- V10: Product images table
CREATE TABLE produto_imagens (
    id              BIGSERIAL       PRIMARY KEY,
    produto_uuid    UUID            NOT NULL REFERENCES produtos(uuid) ON DELETE CASCADE,
    filename        VARCHAR(255)    NOT NULL,
    original_name   VARCHAR(255)    NOT NULL,
    content_type    VARCHAR(100)    NOT NULL,
    file_size       BIGINT          NOT NULL,
    sort_order      INT             NOT NULL DEFAULT 0,
    is_main         BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_produto_imagens_produto ON produto_imagens(produto_uuid);
CREATE INDEX idx_produto_imagens_main ON produto_imagens(produto_uuid, is_main) WHERE is_main = TRUE;
