CREATE TABLE clientes (
    id              BIGSERIAL       PRIMARY KEY,
    uuid            UUID            NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    full_name       VARCHAR(255)    NOT NULL,
    cpf             CHAR(11)        NOT NULL UNIQUE,
    email           VARCHAR(255),
    phone           VARCHAR(15),
    birth_date      DATE,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_clientes_cpf  ON clientes(cpf);
CREATE INDEX idx_clientes_name ON clientes(LOWER(full_name));
