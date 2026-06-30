package br.com.moreiracruz.erp.modules.auth.domain.port.out;

import br.com.moreiracruz.erp.modules.auth.domain.model.Usuario;
import br.com.moreiracruz.erp.modules.auth.domain.model.Role;
import br.com.moreiracruz.erp.modules.auth.domain.model.UsuarioStatus;

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

    boolean existsByRoleIn(List<Role> roles);

    long countByRoleAndStatus(Role role, UsuarioStatus status);

    Usuario save(Usuario usuario);
}
