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

-- Seed: assign main images to existing products (using the real filenames)
INSERT INTO produto_imagens (produto_uuid, filename, original_name, content_type, file_size, sort_order, is_main) VALUES
  ('a1b2c3d4-1111-4000-a000-000000000001', 'vestido_floral_primavera.png', 'vestido_floral_primavera.png', 'image/png', 236622, 0, true),
  ('a1b2c3d4-1111-4000-a000-000000000002', 'blusa_seda_natural.png', 'blusa_seda_natural.png', 'image/png', 162830, 0, true),
  ('a1b2c3d4-1111-4000-a000-000000000003', 'saia_midi_linho.png', 'saia_midi_linho.png', 'image/png', 185173, 0, true),
  ('a1b2c3d4-1111-4000-a000-000000000004', 'vestido_renda_dourada.png', 'vestido_renda_dourada.png', 'image/png', 142183, 0, true),
  ('a1b2c3d4-1111-4000-a000-000000000006', 'blusa_bordada_artesanal.png', 'blusa_bordada_artesanal.png', 'image/png', 110129, 0, true),
  ('a1b2c3d4-1111-4000-a000-000000000008', 'jaqueta_couro_eco.png', 'jaqueta_couro_eco.png', 'image/png', 108752, 0, true),
  ('a1b2c3d4-1111-4000-a000-000000000009', 'shorts_alfaiataria.png', 'shorts_alfaiataria.png', 'image/png', 103680, 0, true),
  ('a1b2c3d4-1111-4000-a000-000000000010', 'camiseta_basic_algodao.png', 'camiseta_basic_algodao.png', 'image/png', 116170, 0, true);
