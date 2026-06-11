package com.erp.infrastructure.security;

import com.erp.modules.auth.domain.model.Role;
import com.erp.modules.auth.domain.model.Usuario;
import com.erp.modules.auth.domain.port.out.UsuarioRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing the domain's {@link UsuarioRepository} outbound port
 * using Spring Data JPA.
 *
 * <p>Performs manual mapping between {@link UsuarioJpaEntity} (persistence concern)
 * and {@link Usuario} (domain aggregate root).
 */
@Repository
public class UsuarioRepositoryAdapter implements UsuarioRepository {

    private final UsuarioJpaRepository jpaRepository;

    public UsuarioRepositoryAdapter(UsuarioJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Usuario> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(this::toDomain);
    }

    @Override
    public Optional<Usuario> findByUuid(UUID uuid) {
        return jpaRepository.findByUuid(uuid).map(this::toDomain);
    }

    @Override
    public Usuario save(Usuario usuario) {
        UsuarioJpaEntity entity = toEntity(usuario);
        UsuarioJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    // -------------------------------------------------------------------------
    // Mapping helpers
    // -------------------------------------------------------------------------

    private Usuario toDomain(UsuarioJpaEntity e) {
        return Usuario.reconstruct(
                e.getId(),
                e.getUuid(),
                e.getUsername(),
                e.getPasswordHash(),
                Role.valueOf(e.getRole()),
                e.isActive(),
                e.getFailedAttempts(),
                e.getLockedUntil(),
                e.getCreatedAt());
    }

    private UsuarioJpaEntity toEntity(Usuario u) {
        UsuarioJpaEntity e = new UsuarioJpaEntity();
        e.setId(u.getId());
        e.setUuid(u.getUuid());
        e.setUsername(u.getUsername());
        e.setPasswordHash(u.getPasswordHash());
        e.setRole(u.getRole().name());
        e.setActive(u.isActive());
        e.setFailedAttempts(u.getFailedAttempts());
        e.setLockedUntil(u.getLockedUntil());
        e.setCreatedAt(u.getCreatedAt());
        return e;
    }
}
