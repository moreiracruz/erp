package com.erp.test.builders;

import com.erp.modules.auth.domain.model.Role;
import com.erp.modules.auth.domain.model.Usuario;

import java.time.Instant;
import java.util.UUID;

/**
 * Fluent builder for creating {@link Usuario} domain objects in tests.
 */
public class UsuarioTestBuilder {

    private UUID uuid = UUID.randomUUID();
    private String username = "user_" + uuid.toString().substring(0, 8) + "@example.com";
    private String passwordHash = "$2a$10$dummyBcryptHashForTestingPurposes000000000000000000";
    private Role role = Role.ROLE_CASHIER;
    private boolean active = true;
    private int failedAttempts = 0;
    private Instant lockedUntil = null;

    private UsuarioTestBuilder() {}

    // Named constructors for common personas
    public static UsuarioTestBuilder aManager() {
        return new UsuarioTestBuilder().withRole(Role.ROLE_MANAGER);
    }

    public static UsuarioTestBuilder aCashier() {
        return new UsuarioTestBuilder().withRole(Role.ROLE_CASHIER);
    }

    public static UsuarioTestBuilder aStockOperator() {
        return new UsuarioTestBuilder().withRole(Role.ROLE_STOCK);
    }

    public static UsuarioTestBuilder aFinanceOperator() {
        return new UsuarioTestBuilder().withRole(Role.ROLE_FINANCE);
    }

    public static UsuarioTestBuilder aLockedUser() {
        return new UsuarioTestBuilder()
                .withFailedAttempts(6)
                .withLockedUntil(Instant.now().plusSeconds(900));
    }

    // Fluent setters
    public UsuarioTestBuilder withUuid(UUID uuid) { this.uuid = uuid; return this; }
    public UsuarioTestBuilder withUsername(String username) { this.username = username; return this; }
    public UsuarioTestBuilder withPasswordHash(String hash) { this.passwordHash = hash; return this; }
    public UsuarioTestBuilder withRole(Role role) { this.role = role; return this; }
    public UsuarioTestBuilder withActive(boolean active) { this.active = active; return this; }
    public UsuarioTestBuilder withFailedAttempts(int n) { this.failedAttempts = n; return this; }
    public UsuarioTestBuilder withLockedUntil(Instant locked) { this.lockedUntil = locked; return this; }

    /** Build domain object (in-memory, no persistence). */
    public Usuario build() {
        return Usuario.reconstruct(null, uuid, username, passwordHash, role, active,
                failedAttempts, lockedUntil, Instant.now());
    }
}
