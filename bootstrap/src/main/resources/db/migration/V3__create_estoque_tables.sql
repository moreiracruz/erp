-- V3: Inventory tables — estoque_items, movimentos_estoque, reservas_estoque

CREATE TABLE estoque_items (
    id              BIGSERIAL       PRIMARY KEY,
    variante_uuid   UUID            NOT NULL UNIQUE REFERENCES variantes(uuid),
    physical_stock  INT             NOT NULL DEFAULT 0 CHECK (physical_stock >= 0),
    reserved_stock  INT             NOT NULL DEFAULT 0 CHECK (reserved_stock >= 0),
    version         BIGINT          NOT NULL DEFAULT 0,
    -- Enforce available = physical - reserved >= 0
    CONSTRAINT chk_available_non_negative CHECK (physical_stock - reserved_stock >= 0)
);

CREATE TABLE movimentos_estoque (
    id              BIGSERIAL       PRIMARY KEY,
    uuid            UUID            NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    variante_uuid   UUID            NOT NULL REFERENCES variantes(uuid),
    operation_type  VARCHAR(25)     NOT NULL CHECK (operation_type IN ('ENTRADA','SAÍDA','RESERVA','LIBERAÇÃO_RESERVA')),
    quantity        INT             NOT NULL CHECK (quantity > 0),
    occurred_at     TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    actor_uuid      UUID,           -- NULL means SYSTEM
    reference_uuid  UUID            -- sale UUID for reservas
);

CREATE INDEX idx_movimentos_variante ON movimentos_estoque(variante_uuid);

CREATE TABLE reservas_estoque (
    id              BIGSERIAL       PRIMARY KEY,
    uuid            UUID            NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    variante_uuid   UUID            NOT NULL REFERENCES variantes(uuid),
    sale_uuid       UUID            NOT NULL,
    quantity        INT             NOT NULL CHECK (quantity > 0),
    status          VARCHAR(15)     NOT NULL DEFAULT 'ACTIVE'
                        CHECK (status IN ('ACTIVE','COMMITTED','RELEASED','EXPIRED')),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    expires_at      TIMESTAMPTZ     NOT NULL
);

CREATE INDEX idx_reservas_sale_uuid  ON reservas_estoque(sale_uuid);
CREATE INDEX idx_reservas_expires_at ON reservas_estoque(expires_at) WHERE status = 'ACTIVE';
