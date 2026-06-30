-- V11: Store product public UUID on variants for catalog read models

ALTER TABLE variantes
    ADD COLUMN produto_uuid UUID;

UPDATE variantes v
SET produto_uuid = p.uuid
FROM produtos p
WHERE v.produto_id = p.id;

ALTER TABLE variantes
    ALTER COLUMN produto_uuid SET NOT NULL,
    ADD CONSTRAINT fk_variantes_produto_uuid
        FOREIGN KEY (produto_uuid) REFERENCES produtos(uuid);

CREATE INDEX idx_variantes_produto_uuid ON variantes(produto_uuid);
