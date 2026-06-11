-- V2: Product tables — produtos and variantes

CREATE TABLE produtos (
    id              BIGSERIAL       PRIMARY KEY,
    uuid            UUID            NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    name            VARCHAR(255)    NOT NULL,
    brand           VARCHAR(100)    NOT NULL,
    category        VARCHAR(100)    NOT NULL,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

-- Case-insensitive uniqueness for active products only (partial index)
CREATE UNIQUE INDEX uq_active_produto_name ON produtos (LOWER(name)) WHERE active = TRUE;

CREATE TABLE variantes (
    id              BIGSERIAL       PRIMARY KEY,
    uuid            UUID            NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    produto_id      BIGINT          NOT NULL REFERENCES produtos(id),
    sku             VARCHAR(50)     NOT NULL UNIQUE,
    size            VARCHAR(50)     NOT NULL,
    color           VARCHAR(50)     NOT NULL,
    barcode         VARCHAR(14)     NOT NULL UNIQUE,
    price           NUMERIC(10,2)   NOT NULL CHECK (price BETWEEN 0.01 AND 999999.99),
    cost            NUMERIC(10,2)   NOT NULL CHECK (cost BETWEEN 0.01 AND 999999.99),
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_variantes_sku         ON variantes(sku);
CREATE INDEX idx_variantes_barcode     ON variantes(barcode);
CREATE INDEX idx_variantes_produto_id  ON variantes(produto_id);
