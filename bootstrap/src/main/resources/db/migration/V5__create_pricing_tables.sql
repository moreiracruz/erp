-- V5: Pricing tables — campanhas and cupons

CREATE TABLE campanhas (
    id              BIGSERIAL       PRIMARY KEY,
    uuid            UUID            NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    name            VARCHAR(255)    NOT NULL,
    type            VARCHAR(15)     NOT NULL CHECK (type IN ('PERCENTAGE','FIXED','PROGRESSIVE')),
    target_type     VARCHAR(10)     NOT NULL CHECK (target_type IN ('PRODUTO','CATEGORY','ALL')),
    target_uuid     UUID,
    target_category VARCHAR(100),
    discount_value  NUMERIC(10,2)   NOT NULL,
    min_quantity    INT,
    cashback_pct    NUMERIC(5,2),
    starts_at       TIMESTAMPTZ     NOT NULL,
    ends_at         TIMESTAMPTZ     NOT NULL,
    active          BOOLEAN         NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_campanhas_active_dates ON campanhas(starts_at, ends_at) WHERE active = TRUE;

CREATE TABLE cupons (
    id              BIGSERIAL       PRIMARY KEY,
    uuid            UUID            NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    code            VARCHAR(50)     NOT NULL,
    type            VARCHAR(15)     NOT NULL CHECK (type IN ('PERCENTAGE','FIXED','PROGRESSIVE')),
    discount_value  NUMERIC(10,2)   NOT NULL,
    starts_at       TIMESTAMPTZ     NOT NULL,
    ends_at         TIMESTAMPTZ     NOT NULL,
    max_usages      INT             NOT NULL,
    usage_count     INT             NOT NULL DEFAULT 0,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    version         BIGINT          NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uq_cupom_code ON cupons(LOWER(code));
