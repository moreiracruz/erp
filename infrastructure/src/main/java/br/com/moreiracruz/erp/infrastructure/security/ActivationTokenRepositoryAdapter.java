package br.com.moreiracruz.erp.infrastructure.security;

import br.com.moreiracruz.erp.modules.auth.domain.model.ActivationToken;
import br.com.moreiracruz.erp.modules.auth.domain.model.ActivationTokenPurpose;
import br.com.moreiracruz.erp.modules.auth.domain.port.out.ActivationTokenRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ActivationTokenRepositoryAdapter implements ActivationTokenRepository {

    private final ActivationTokenJpaRepository jpaRepository;

    public ActivationTokenRepositoryAdapter(ActivationTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<ActivationToken> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash).map(this::toDomain);
    }

    @Override
    public void markActiveTokensUsed(UUID usuarioUuid, ActivationTokenPurpose purpose) {
        jpaRepository.markActiveTokensUsed(usuarioUuid, purpose.name(), Instant.now());
    }

    @Override
    public ActivationToken save(ActivationToken token) {
        return toDomain(jpaRepository.save(toEntity(token)));
    }

    private ActivationToken toDomain(ActivationTokenJpaEntity entity) {
        return ActivationToken.reconstruct(
                entity.getId(),
                entity.getUuid(),
                entity.getUsuarioUuid(),
                entity.getTokenHash(),
                ActivationTokenPurpose.valueOf(entity.getPurpose()),
                entity.getExpiresAt(),
                entity.getUsedAt(),
                entity.getCreatedAt());
    }

    private ActivationTokenJpaEntity toEntity(ActivationToken token) {
        ActivationTokenJpaEntity entity = new ActivationTokenJpaEntity();
        entity.setId(token.getId());
        entity.setUuid(token.getUuid());
        entity.setUsuarioUuid(token.getUsuarioUuid());
        entity.setTokenHash(token.getTokenHash());
        entity.setPurpose(token.getPurpose().name());
        entity.setExpiresAt(token.getExpiresAt());
        entity.setUsedAt(token.getUsedAt());
        entity.setCreatedAt(token.getCreatedAt());
        return entity;
    }
}
