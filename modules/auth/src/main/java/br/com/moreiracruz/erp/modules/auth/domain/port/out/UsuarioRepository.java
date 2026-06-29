package br.com.moreiracruz.erp.modules.auth.domain.port.out;

import br.com.moreiracruz.erp.modules.auth.domain.model.Usuario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port: persistence operations for {@link Usuario}.
 */
public interface UsuarioRepository {

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByUuid(UUID uuid);

    List<Usuario> findAll();

    Usuario save(Usuario usuario);
}
