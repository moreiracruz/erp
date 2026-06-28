package br.com.moreiracruz.erp.infrastructure.security;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link UsuarioJpaEntity}.
 */
public interface UsuarioJpaRepository extends JpaRepository<UsuarioJpaEntity, Long> {

    Optional<UsuarioJpaEntity> findByUsername(String username);

    Optional<UsuarioJpaEntity> findByUuid(UUID uuid);
}
