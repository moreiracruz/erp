CREATE TABLE lancamentos_financeiros (
    id                  BIGSERIAL       PRIMARY KEY,
    uuid                UUID            NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    type                VARCHAR(10)     NOT NULL CHECK (type IN ('RECEITA','DESPESA')),
    amount              NUMERIC(15,2)   NOT NULL CHECK (amount BETWEEN 0.01 AND 999999999.99),
    payment_method      VARCHAR(10),
    description         VARCHAR(255)    NOT NULL,
    category            VARCHAR(20),
    competence_date     DATE            NOT NULL,
    responsible_uuid    UUID            NOT NULL,
    sale_uuid           UUID            UNIQUE,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_lancamentos_competence ON lancamentos_financeiros(competence_date);
CREATE INDEX idx_lancamentos_type       ON lancamentos_financeiros(type);
