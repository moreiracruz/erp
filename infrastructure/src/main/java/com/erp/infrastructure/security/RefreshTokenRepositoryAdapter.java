package com.erp.infrastructure.security;

import com.erp.modules.auth.domain.model.RefreshToken;
import com.erp.modules.auth.domain.port.out.RefreshTokenRepositoryPort;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Adapter implementing the domain's {@link RefreshTokenRepositoryPort} outbound port
 * using Spring Data JPA.
 *
 * <p>Performs manual mapping between {@link RefreshTokenJpaEntity} (persistence concern)
 * and {@link RefreshToken} (domain entity).
 */
@Repository
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepositoryPort {

    private final RefreshTokenJpaRepository jpaRepository;

    public RefreshTokenRepositoryAdapter(RefreshTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash).map(this::toDomain);
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenJpaEntity entity = toEntity(refreshToken);
        RefreshTokenJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    // -------------------------------------------------------------------------
    // Mapping helpers
    // -------------------------------------------------------------------------

    private RefreshToken toDomain(RefreshTokenJpaEntity e) {
        RefreshToken rt = RefreshToken.create(e.getUsuarioUuid(), e.getTokenHash(), 0);
        // Reconstruct full state via the restore helper
        return RefreshToken.restore(
                e.getId(),
                e.getTokenHash(),
                e.getUsuarioUuid(),
                e.getExpiresAt(),
                e.getRevokedAt(),
                e.getCreatedAt());
    }

    private RefreshTokenJpaEntity toEntity(RefreshToken rt) {
        RefreshTokenJpaEntity e = new RefreshTokenJpaEntity();
        e.setId(rt.getId());
        e.setTokenHash(rt.getTokenHash());
        e.setUsuarioUuid(rt.getUsuarioUuid());
        e.setExpiresAt(rt.getExpiresAt());
        e.setRevokedAt(rt.getRevokedAt());
        e.setCreatedAt(rt.getCreatedAt());
        return e;
    }
}
