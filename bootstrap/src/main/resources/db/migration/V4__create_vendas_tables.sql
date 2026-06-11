-- V4: Sales tables — vendas and itens_venda

CREATE TABLE vendas (
    id                  BIGSERIAL       PRIMARY KEY,
    uuid                UUID            NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    operator_uuid       UUID            NOT NULL,
    terminal_id         VARCHAR(50)     NOT NULL,
    cliente_uuid        UUID,
    status              VARCHAR(15)     NOT NULL DEFAULT 'EM_ANDAMENTO'
                            CHECK (status IN ('EM_ANDAMENTO','FINALIZADA','CANCELADA')),
    payment_method      VARCHAR(10)     CHECK (payment_method IN ('DINHEIRO','DEBITO','CREDITO','PIX')),
    subtotal            NUMERIC(12,2),
    discount_amount     NUMERIC(12,2)   DEFAULT 0,
    tax_amount          NUMERIC(12,2)   DEFAULT 0,
    total               NUMERIC(12,2),
    change_amount       NUMERIC(12,2),
    coupon_code         VARCHAR(50),
    cancellation_reason VARCHAR(255),
    data_venda          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    finalized_at        TIMESTAMPTZ
);

CREATE INDEX idx_vendas_data_venda  ON vendas(data_venda);
CREATE INDEX idx_vendas_cliente_id  ON vendas(cliente_uuid);
CREATE INDEX idx_vendas_operator    ON vendas(operator_uuid);

CREATE TABLE itens_venda (
    id              BIGSERIAL       PRIMARY KEY,
    venda_id        BIGINT          NOT NULL REFERENCES vendas(id),
    variante_uuid   UUID            NOT NULL REFERENCES variantes(uuid),
    sku             VARCHAR(50)     NOT NULL,
    quantity        INT             NOT NULL CHECK (quantity > 0),
    unit_price      NUMERIC(10,2)   NOT NULL,
    line_total      NUMERIC(12,2)   NOT NULL
);

CREATE INDEX idx_itens_venda_venda_id ON itens_venda(venda_id);
