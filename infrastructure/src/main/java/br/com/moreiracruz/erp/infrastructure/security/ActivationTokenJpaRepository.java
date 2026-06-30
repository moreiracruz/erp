package br.com.moreiracruz.erp.infrastructure.security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ActivationTokenJpaRepository extends JpaRepository<ActivationTokenJpaEntity, Long> {

    Optional<ActivationTokenJpaEntity> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
            update ActivationTokenJpaEntity token
               set token.usedAt = :usedAt
             where token.usuarioUuid = :usuarioUuid
               and token.purpose = :purpose
               and token.usedAt is null
            """)
    void markActiveTokensUsed(@Param("usuarioUuid") UUID usuarioUuid,
                              @Param("purpose") String purpose,
                              @Param("usedAt") Instant usedAt);
}
