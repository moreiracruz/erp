package com.erp.modules.auth.domain.port.out;

import com.erp.modules.auth.domain.model.Usuario;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port: persistence operations for {@link Usuario}.
 */
public interface UsuarioRepository {

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByUuid(UUID uuid);

    Usuario save(Usuario usuario);
}
