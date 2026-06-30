package br.com.moreiracruz.erp.modules.auth.domain.model;

import br.com.moreiracruz.erp.shared.exceptions.ValidationException;

import java.time.Instant;
import java.util.UUID;

public class ActivationToken {

    private Long id;
    private UUID uuid;
    private UUID usuarioUuid;
    private String tokenHash;
    private ActivationTokenPurpose purpose;
    private Instant expiresAt;
    private Instant usedAt;
    private Instant createdAt;

    protected ActivationToken() {}

    public static ActivationToken create(UUID usuarioUuid, String tokenHash, ActivationTokenPurpose purpose, Instant expiresAt) {
        if (usuarioUuid == null) {
            throw new ValidationException("Usuário do token é obrigatório");
        }
        if (tokenHash == null || tokenHash.isBlank()) {
            throw new ValidationException("Hash do token é obrigatório");
        }
        if (purpose == null) {
            throw new ValidationException("Finalidade do token é obrigatória");
        }
        if (expiresAt == null || !expiresAt.isAfter(Instant.now())) {
            throw new ValidationException("Expiração do token deve estar no futuro");
        }
        ActivationToken token = new ActivationToken();
        token.uuid = UUID.randomUUID();
        token.usuarioUuid = usuarioUuid;
        token.tokenHash = tokenHash;
        token.purpose = purpose;
        token.expiresAt = expiresAt;
        token.usedAt = null;
        token.createdAt = Instant.now();
        return token;
    }

    public static ActivationToken reconstruct(Long id, UUID uuid, UUID usuarioUuid, String tokenHash,
                                              ActivationTokenPurpose purpose, Instant expiresAt,
                                              Instant usedAt, Instant createdAt) {
        ActivationToken token = new ActivationToken();
        token.id = id;
        token.uuid = uuid;
        token.usuarioUuid = usuarioUuid;
        token.tokenHash = tokenHash;
        token.purpose = purpose;
        token.expiresAt = expiresAt;
        token.usedAt = usedAt;
        token.createdAt = createdAt;
        return token;
    }

    public boolean isValid() {
        return usedAt == null && Instant.now().isBefore(expiresAt);
    }

    public void markUsed() {
        this.usedAt = Instant.now();
    }

    public Long getId() { return id; }
    public UUID getUuid() { return uuid; }
    public UUID getUsuarioUuid() { return usuarioUuid; }
    public String getTokenHash() { return tokenHash; }
    public ActivationTokenPurpose getPurpose() { return purpose; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getUsedAt() { return usedAt; }
    public Instant getCreatedAt() { return createdAt; }
}
