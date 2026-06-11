package com.erp.modules.auth.domain.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Entity representing a refresh token issued to a user session.
 *
 * <p>Only the SHA-256 hash of the raw token value is persisted; the plaintext
 * token is returned to the client once and never stored.
 *
 * <p>Use {@link #create(UUID, String, int)} to construct new instances.
 */
public class RefreshToken {

    private Long id;
    private String tokenHash;
    private UUID usuarioUuid;
    private Instant expiresAt;
    private Instant revokedAt;
    private Instant createdAt;

    /** Required by JPA — not for direct use in application code. */
    protected RefreshToken() {}

    // -------------------------------------------------------------------------
    // Factory
    // -------------------------------------------------------------------------

    /**
     * Creates a new refresh token entity.
     *
     * @param usuarioUuid public UUID of the owning {@link Usuario}
     * @param tokenHash   SHA-256 hex digest of the raw token string
     * @param daysValidity number of days until the token expires (e.g., 7)
     * @return a ready-to-persist {@code RefreshToken}
     */
    public static RefreshToken create(UUID usuarioUuid, String tokenHash, int daysValidity) {
        RefreshToken rt = new RefreshToken();
        rt.usuarioUuid = usuarioUuid;
        rt.tokenHash = tokenHash;
        rt.createdAt = Instant.now();
        rt.expiresAt = rt.createdAt.plus(daysValidity, ChronoUnit.DAYS);
        rt.revokedAt = null;
        return rt;
    }

    /**
     * Reconstitutes a {@code RefreshToken} from a persistence store (DDD restore pattern).
     *
     * <p>This factory bypasses business-rule validation and sets all fields directly,
     * because the data coming from the database is already assumed to be valid.
     *
     * @param id          internal surrogate key from the database
     * @param tokenHash   SHA-256 hex digest of the raw token string
     * @param usuarioUuid public UUID of the owning user
     * @param expiresAt   expiry instant stored in the database
     * @param revokedAt   revocation instant, or {@code null} if not revoked
     * @param createdAt   creation instant stored in the database
     */
    public static RefreshToken restore(Long id, String tokenHash, UUID usuarioUuid,
                                       Instant expiresAt, Instant revokedAt, Instant createdAt) {
        RefreshToken rt = new RefreshToken();
        rt.id = id;
        rt.tokenHash = tokenHash;
        rt.usuarioUuid = usuarioUuid;
        rt.expiresAt = expiresAt;
        rt.revokedAt = revokedAt;
        rt.createdAt = createdAt;
        return rt;
    }

    // -------------------------------------------------------------------------
    // Domain behaviour
    // -------------------------------------------------------------------------

    /**
     * Revokes this token immediately.
     * After calling this method {@link #isValid()} will return {@code false}.
     */
    public void revoke() {
        revokedAt = Instant.now();
    }

    /**
     * Returns {@code true} if the token has not been revoked and has not yet
     * reached its expiry instant.
     */
    public boolean isValid() {
        return revokedAt == null && Instant.now().isBefore(expiresAt);
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public Long getId() {
        return id;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public UUID getUsuarioUuid() {
        return usuarioUuid;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
