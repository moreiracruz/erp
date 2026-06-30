ALTER TABLE usuarios DROP CONSTRAINT IF EXISTS usuarios_role_check;

ALTER TABLE usuarios
    ADD CONSTRAINT usuarios_role_check
        CHECK (role IN ('ROLE_USER', 'ROLE_MANAGER', 'ROLE_CASHIER', 'ROLE_STOCK', 'ROLE_FINANCE'));
