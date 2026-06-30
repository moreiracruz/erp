CREATE TABLE consignantes (
    id              BIGSERIAL       PRIMARY KEY,
    uuid            UUID            NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    name            VARCHAR(255)    NOT NULL,
    document        VARCHAR(30),
    email           VARCHAR(255),
    phone           VARCHAR(30),
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_consignantes_active ON consignantes(active);

CREATE TABLE contratos_consignacao (
    id                  BIGSERIAL       PRIMARY KEY,
    uuid                UUID            NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    consignante_uuid    UUID            NOT NULL REFERENCES consignantes(uuid),
    code                VARCHAR(50)     NOT NULL UNIQUE,
    status              VARCHAR(30)     NOT NULL
                            CHECK (status IN ('ABERTO','PARCIALMENTE_ACERTADO','ENCERRADO','CANCELADO')),
    opened_at           TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    closed_at           TIMESTAMPTZ
);

CREATE INDEX idx_contratos_consignante ON contratos_consignacao(consignante_uuid);
CREATE INDEX idx_contratos_status ON contratos_consignacao(status);

CREATE TABLE itens_consignados (
    id                  BIGSERIAL       PRIMARY KEY,
    uuid                UUID            NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    contrato_uuid       UUID            NOT NULL REFERENCES contratos_consignacao(uuid),
    variante_uuid       UUID            NOT NULL REFERENCES variantes(uuid),
    quantity            INT             NOT NULL CHECK (quantity > 0),
    remaining_quantity  INT             NOT NULL CHECK (remaining_quantity >= 0),
    sold_quantity       INT             NOT NULL DEFAULT 0 CHECK (sold_quantity >= 0),
    settled_quantity    INT             NOT NULL DEFAULT 0 CHECK (settled_quantity >= 0),
    returned_quantity   INT             NOT NULL DEFAULT 0 CHECK (returned_quantity >= 0),
    status              VARCHAR(20)     NOT NULL
                            CHECK (status IN ('RECEBIDO','VENDIDO','DEVOLVIDO','ACERTADO')),
    received_at         TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    sold_sale_uuid      UUID,
    returned_at         TIMESTAMPTZ,
    CONSTRAINT chk_consigned_quantities
        CHECK (remaining_quantity + sold_quantity + returned_quantity = quantity),
    CONSTRAINT chk_consigned_settled_not_above_sold
        CHECK (settled_quantity <= sold_quantity)
);

CREATE INDEX idx_itens_consignados_contrato ON itens_consignados(contrato_uuid);
CREATE INDEX idx_itens_consignados_variante ON itens_consignados(variante_uuid);
CREATE INDEX idx_itens_consignados_status ON itens_consignados(status);

CREATE TABLE acertos_consignacao (
    id                  BIGSERIAL       PRIMARY KEY,
    uuid                UUID            NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    contrato_uuid       UUID            NOT NULL REFERENCES contratos_consignacao(uuid),
    responsible_uuid    UUID            NOT NULL,
    total_amount        NUMERIC(15,2)   NOT NULL CHECK (total_amount BETWEEN 0.01 AND 999999999.99),
    notes               VARCHAR(500),
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_acertos_contrato ON acertos_consignacao(contrato_uuid);

CREATE TABLE acerto_itens (
    id                  BIGSERIAL       PRIMARY KEY,
    acerto_uuid         UUID            NOT NULL REFERENCES acertos_consignacao(uuid),
    item_uuid           UUID            NOT NULL REFERENCES itens_consignados(uuid),
    settled_quantity    INT             NOT NULL CHECK (settled_quantity > 0),
    manual_amount       NUMERIC(15,2)   NOT NULL CHECK (manual_amount BETWEEN 0.01 AND 999999999.99)
);

CREATE INDEX idx_acerto_itens_acerto ON acerto_itens(acerto_uuid);
CREATE INDEX idx_acerto_itens_item ON acerto_itens(item_uuid);
