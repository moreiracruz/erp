CREATE TABLE consignatarios_envio (
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

CREATE INDEX idx_consignatarios_envio_active ON consignatarios_envio(active);

CREATE TABLE contratos_consignacao_envio (
    id                  BIGSERIAL       PRIMARY KEY,
    uuid                UUID            NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    consignee_uuid      UUID            NOT NULL REFERENCES consignatarios_envio(uuid),
    code                VARCHAR(50)     NOT NULL UNIQUE,
    status              VARCHAR(30)     NOT NULL
                            CHECK (status IN ('ABERTO','PARCIALMENTE_ACERTADO','ENCERRADO','CANCELADO')),
    opened_at           TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    closed_at           TIMESTAMPTZ
);

CREATE INDEX idx_contratos_consignacao_envio_consignee ON contratos_consignacao_envio(consignee_uuid);
CREATE INDEX idx_contratos_consignacao_envio_status ON contratos_consignacao_envio(status);

CREATE TABLE itens_consignacao_envio (
    id                  BIGSERIAL       PRIMARY KEY,
    uuid                UUID            NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    contrato_uuid       UUID            NOT NULL REFERENCES contratos_consignacao_envio(uuid),
    variante_uuid       UUID            NOT NULL REFERENCES variantes(uuid),
    quantity            INT             NOT NULL CHECK (quantity > 0),
    available_quantity  INT             NOT NULL CHECK (available_quantity >= 0),
    sold_quantity       INT             NOT NULL DEFAULT 0 CHECK (sold_quantity >= 0),
    settled_quantity    INT             NOT NULL DEFAULT 0 CHECK (settled_quantity >= 0),
    returned_quantity   INT             NOT NULL DEFAULT 0 CHECK (returned_quantity >= 0),
    status              VARCHAR(20)     NOT NULL
                            CHECK (status IN ('ENVIADO','VENDIDO','DEVOLVIDO','ACERTADO')),
    sent_at             TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    returned_at         TIMESTAMPTZ,
    CONSTRAINT chk_sent_consigned_quantities
        CHECK (available_quantity + sold_quantity + returned_quantity = quantity),
    CONSTRAINT chk_sent_consigned_settled_not_above_sold
        CHECK (settled_quantity <= sold_quantity)
);

CREATE INDEX idx_itens_consignacao_envio_contrato ON itens_consignacao_envio(contrato_uuid);
CREATE INDEX idx_itens_consignacao_envio_variante ON itens_consignacao_envio(variante_uuid);
CREATE INDEX idx_itens_consignacao_envio_status ON itens_consignacao_envio(status);

CREATE TABLE acertos_consignacao_envio (
    id                  BIGSERIAL       PRIMARY KEY,
    uuid                UUID            NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    contrato_uuid       UUID            NOT NULL REFERENCES contratos_consignacao_envio(uuid),
    responsible_uuid    UUID            NOT NULL,
    total_amount        NUMERIC(15,2)   NOT NULL CHECK (total_amount BETWEEN 0.01 AND 999999999.99),
    notes               VARCHAR(500),
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_acertos_consignacao_envio_contrato ON acertos_consignacao_envio(contrato_uuid);

CREATE TABLE acerto_itens_consignacao_envio (
    id                  BIGSERIAL       PRIMARY KEY,
    acerto_uuid         UUID            NOT NULL REFERENCES acertos_consignacao_envio(uuid),
    item_uuid           UUID            NOT NULL REFERENCES itens_consignacao_envio(uuid),
    settled_quantity    INT             NOT NULL CHECK (settled_quantity > 0),
    manual_amount       NUMERIC(15,2)   NOT NULL CHECK (manual_amount BETWEEN 0.01 AND 999999999.99)
);

CREATE INDEX idx_acerto_itens_consignacao_envio_acerto ON acerto_itens_consignacao_envio(acerto_uuid);
CREATE INDEX idx_acerto_itens_consignacao_envio_item ON acerto_itens_consignacao_envio(item_uuid);
