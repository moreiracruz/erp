-- V9: Seed data for development and testing
-- This migration inserts sample data for all modules

-- ============================================================================
-- USUARIOS (password: 'Senha123!' for all users, bcrypt hash)
-- ============================================================================
INSERT INTO usuarios (uuid, username, password_hash, role, active, failed_attempts, created_at) VALUES
  (gen_random_uuid(), 'gerente@reinoeflor.com.br', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_MANAGER', true, 0, NOW()),
  (gen_random_uuid(), 'caixa@reinoeflor.com.br', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_CASHIER', true, 0, NOW()),
  (gen_random_uuid(), 'estoque@reinoeflor.com.br', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_STOCK', true, 0, NOW()),
  (gen_random_uuid(), 'financeiro@reinoeflor.com.br', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ROLE_FINANCE', true, 0, NOW());

-- ============================================================================
-- PRODUTOS
-- ============================================================================
INSERT INTO produtos (uuid, name, brand, category, active, created_at) VALUES
  ('a1b2c3d4-1111-4000-a000-000000000001', 'Vestido Floral Primavera', 'Reino & Flor', 'Vestidos', true, NOW()),
  ('a1b2c3d4-1111-4000-a000-000000000002', 'Blusa Seda Natural', 'Reino & Flor', 'Blusas', true, NOW()),
  ('a1b2c3d4-1111-4000-a000-000000000003', 'Saia Midi Linho', 'Reino & Flor', 'Saias', true, NOW()),
  ('a1b2c3d4-1111-4000-a000-000000000004', 'Vestido Renda Dourada', 'Reino & Flor', 'Vestidos', true, NOW()),
  ('a1b2c3d4-1111-4000-a000-000000000005', 'Conjunto Elegance', 'Reino & Flor', 'Conjuntos', true, NOW()),
  ('a1b2c3d4-1111-4000-a000-000000000006', 'Blusa Bordada Artesanal', 'Reino & Flor', 'Blusas', true, NOW()),
  ('a1b2c3d4-1111-4000-a000-000000000007', 'Calça Pantalona Linho', 'Reino & Flor', 'Calças', true, NOW()),
  ('a1b2c3d4-1111-4000-a000-000000000008', 'Jaqueta Couro Eco', 'Parceira', 'Jaquetas', true, NOW()),
  ('a1b2c3d4-1111-4000-a000-000000000009', 'Shorts Alfaiataria', 'Reino & Flor', 'Shorts', true, NOW()),
  ('a1b2c3d4-1111-4000-a000-000000000010', 'Camiseta Basic Algodão', 'Reino & Flor', 'Camisetas', true, NOW());

-- ============================================================================
-- VARIANTES
-- ============================================================================
INSERT INTO variantes (uuid, produto_id, sku, size, color, barcode, price, cost, active) VALUES
  -- Vestido Floral Primavera
  (gen_random_uuid(), 1, 'VFP-P-ROSA', 'P', 'Rosa', '7891000000101', 289.90, 120.00, true),
  (gen_random_uuid(), 1, 'VFP-M-ROSA', 'M', 'Rosa', '7891000000102', 289.90, 120.00, true),
  (gen_random_uuid(), 1, 'VFP-G-ROSA', 'G', 'Rosa', '7891000000103', 289.90, 120.00, true),
  (gen_random_uuid(), 1, 'VFP-M-AZUL', 'M', 'Azul', '7891000000104', 299.90, 125.00, true),
  -- Blusa Seda Natural
  (gen_random_uuid(), 2, 'BSN-P-BEGE', 'P', 'Bege', '7891000000201', 189.90, 75.00, true),
  (gen_random_uuid(), 2, 'BSN-M-BEGE', 'M', 'Bege', '7891000000202', 189.90, 75.00, true),
  (gen_random_uuid(), 2, 'BSN-G-BEGE', 'G', 'Bege', '7891000000203', 189.90, 75.00, true),
  (gen_random_uuid(), 2, 'BSN-M-PRETO', 'M', 'Preto', '7891000000204', 189.90, 75.00, true),
  -- Saia Midi Linho
  (gen_random_uuid(), 3, 'SML-P-BEGE', 'P', 'Bege', '7891000000301', 219.90, 90.00, true),
  (gen_random_uuid(), 3, 'SML-M-BEGE', 'M', 'Bege', '7891000000302', 219.90, 90.00, true),
  (gen_random_uuid(), 3, 'SML-G-VERDE', 'G', 'Verde', '7891000000303', 229.90, 95.00, true),
  -- Vestido Renda Dourada
  (gen_random_uuid(), 4, 'VRD-P-DOURADO', 'P', 'Dourado', '7891000000401', 349.90, 145.00, true),
  (gen_random_uuid(), 4, 'VRD-M-DOURADO', 'M', 'Dourado', '7891000000402', 349.90, 145.00, true),
  (gen_random_uuid(), 4, 'VRD-G-DOURADO', 'G', 'Dourado', '7891000000403', 349.90, 145.00, true),
  -- Conjunto Elegance
  (gen_random_uuid(), 5, 'CE-P-PRETO', 'P', 'Preto', '7891000000501', 459.90, 190.00, true),
  (gen_random_uuid(), 5, 'CE-M-PRETO', 'M', 'Preto', '7891000000502', 459.90, 190.00, true),
  (gen_random_uuid(), 5, 'CE-G-MARROM', 'G', 'Marrom', '7891000000503', 479.90, 200.00, true),
  -- Blusa Bordada Artesanal
  (gen_random_uuid(), 6, 'BBA-P-BRANCO', 'P', 'Branco', '7891000000601', 159.90, 65.00, true),
  (gen_random_uuid(), 6, 'BBA-M-BRANCO', 'M', 'Branco', '7891000000602', 159.90, 65.00, true),
  (gen_random_uuid(), 6, 'BBA-G-BRANCO', 'G', 'Branco', '7891000000603', 159.90, 65.00, true),
  -- Calça Pantalona
  (gen_random_uuid(), 7, 'CPL-M-BEGE', 'M', 'Bege', '7891000000701', 249.90, 100.00, true),
  (gen_random_uuid(), 7, 'CPL-G-BEGE', 'G', 'Bege', '7891000000702', 249.90, 100.00, true),
  -- Jaqueta Couro Eco
  (gen_random_uuid(), 8, 'JCE-M-PRETO', 'M', 'Preto', '7891000000801', 399.90, 170.00, true),
  (gen_random_uuid(), 8, 'JCE-G-PRETO', 'G', 'Preto', '7891000000802', 399.90, 170.00, true),
  -- Shorts Alfaiataria
  (gen_random_uuid(), 9, 'SA-P-PRETO', 'P', 'Preto', '7891000000901', 149.90, 60.00, true),
  (gen_random_uuid(), 9, 'SA-M-BEGE', 'M', 'Bege', '7891000000902', 149.90, 60.00, true),
  -- Camiseta Basic
  (gen_random_uuid(), 10, 'CB-P-BRANCO', 'P', 'Branco', '7891000001001', 79.90, 30.00, true),
  (gen_random_uuid(), 10, 'CB-M-PRETO', 'M', 'Preto', '7891000001002', 79.90, 30.00, true),
  (gen_random_uuid(), 10, 'CB-G-BEGE', 'G', 'Bege', '7891000001003', 79.90, 30.00, true);

-- ============================================================================
-- ESTOQUE
-- ============================================================================
INSERT INTO estoque_items (variante_uuid, physical_stock, reserved_stock, version)
SELECT uuid, floor(random() * 40 + 5)::int, 0, 0
FROM variantes;

-- ============================================================================
-- CLIENTES
-- ============================================================================
INSERT INTO clientes (uuid, full_name, cpf, email, phone, active, created_at) VALUES
  (gen_random_uuid(), 'Maria Silva Santos', '52998224725', 'maria.silva@email.com', '11999880001', true, NOW()),
  (gen_random_uuid(), 'Ana Paula Oliveira', '71428793003', 'ana.oliveira@email.com', '11999880002', true, NOW()),
  (gen_random_uuid(), 'Fernanda Costa Lima', '89012345600', 'fernanda.lima@email.com', '21988770003', true, NOW()),
  (gen_random_uuid(), 'Juliana Mendes Rocha', '45612378900', 'juliana.rocha@email.com', '31977660004', true, NOW()),
  (gen_random_uuid(), 'Camila Ferreira Dias', '78945612300', 'camila.dias@email.com', '41966550005', true, NOW());

-- ============================================================================
-- CAMPANHAS DE DESCONTO
-- ============================================================================
INSERT INTO campanhas (uuid, name, type, discount_value, starts_at, ends_at, target_type, active) VALUES
  (gen_random_uuid(), 'Promoção de Inverno', 'PERCENTAGE', 15.00, NOW() - INTERVAL '5 days', NOW() + INTERVAL '30 days', 'ALL', true),
  (gen_random_uuid(), 'Desconto Vestidos', 'PERCENTAGE', 10.00, NOW(), NOW() + INTERVAL '15 days', 'CATEGORY', true);

-- ============================================================================
-- CUPONS
-- ============================================================================
INSERT INTO cupons (uuid, code, type, discount_value, starts_at, ends_at, max_usages, usage_count, active, version) VALUES
  (gen_random_uuid(), 'BEMVINDA10', 'PERCENTAGE', 10.00, NOW() - INTERVAL '30 days', NOW() + INTERVAL '60 days', 100, 12, true, 0),
  (gen_random_uuid(), 'FRETE0', 'FIXED', 15.90, NOW(), NOW() + INTERVAL '30 days', 50, 5, true, 0),
  (gen_random_uuid(), 'VIP20', 'PERCENTAGE', 20.00, NOW(), NOW() + INTERVAL '7 days', 10, 2, true, 0);
