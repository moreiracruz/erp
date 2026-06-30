package br.com.moreiracruz.erp.bootstrap;

import br.com.moreiracruz.erp.modules.auth.domain.model.ActivationToken;
import br.com.moreiracruz.erp.modules.auth.domain.model.ActivationTokenPurpose;
import br.com.moreiracruz.erp.modules.auth.domain.model.Role;
import br.com.moreiracruz.erp.modules.auth.domain.model.Usuario;
import br.com.moreiracruz.erp.modules.auth.domain.model.UsuarioStatus;
import br.com.moreiracruz.erp.modules.auth.domain.port.out.ActivationTokenRepository;
import br.com.moreiracruz.erp.modules.auth.domain.port.out.UsuarioRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SuperAdminBootstrapperTest {

    @Test
    void createsPendingSuperAdminAndActivationTokenWhenNoAdminExists() {
        InMemoryUsuarioRepository usuarios = new InMemoryUsuarioRepository(false);
        InMemoryActivationTokenRepository tokens = new InMemoryActivationTokenRepository();
        SuperAdminBootstrapper bootstrapper = new SuperAdminBootstrapper(
                usuarios,
                tokens,
                "admin@example.com",
                30,
                "http://localhost/activate",
                false);

        bootstrapper.run(null);

        assertThat(usuarios.saved).hasSize(1);
        assertThat(usuarios.saved.getFirst().getUsername()).isEqualTo("admin@example.com");
        assertThat(usuarios.saved.getFirst().getRole()).isEqualTo(Role.ROLE_SUPER_ADMIN);
        assertThat(usuarios.saved.getFirst().getStatus()).isEqualTo(UsuarioStatus.PENDING_ACTIVATION);
        assertThat(tokens.saved).hasSize(1);
    }

    @Test
    void doesNothingWhenAnAdminAlreadyExists() {
        InMemoryUsuarioRepository usuarios = new InMemoryUsuarioRepository(true);
        InMemoryActivationTokenRepository tokens = new InMemoryActivationTokenRepository();
        SuperAdminBootstrapper bootstrapper = new SuperAdminBootstrapper(
                usuarios,
                tokens,
                "admin@example.com",
                30,
                "http://localhost/activate",
                false);

        bootstrapper.run(null);

        assertThat(usuarios.saved).isEmpty();
        assertThat(tokens.saved).isEmpty();
    }

    private static final class InMemoryUsuarioRepository implements UsuarioRepository {
        private final boolean adminExists;
        private final List<Usuario> saved = new ArrayList<>();

        private InMemoryUsuarioRepository(boolean adminExists) {
            this.adminExists = adminExists;
        }

        @Override
        public Optional<Usuario> findByUsername(String username) {
            return Optional.empty();
        }

        @Override
        public Optional<Usuario> findByUuid(UUID uuid) {
            return Optional.empty();
        }

        @Override
        public List<Usuario> findAll() {
            return List.copyOf(saved);
        }

        @Override
        public boolean existsByRoleIn(List<Role> roles) {
            return adminExists;
        }

        @Override
        public long countByRoleAndStatus(Role role, UsuarioStatus status) {
            return saved.stream()
                    .filter(usuario -> usuario.getRole() == role)
                    .filter(usuario -> usuario.getStatus() == status)
                    .count();
        }

        @Override
        public Usuario save(Usuario usuario) {
            saved.add(usuario);
            return usuario;
        }
    }

    private static final class InMemoryActivationTokenRepository implements ActivationTokenRepository {
        private final List<ActivationToken> saved = new ArrayList<>();

        @Override
        public Optional<ActivationToken> findByTokenHash(String tokenHash) {
            return Optional.empty();
        }

        @Override
        public void markActiveTokensUsed(UUID usuarioUuid, ActivationTokenPurpose purpose) {
            saved.clear();
        }

        @Override
        public ActivationToken save(ActivationToken token) {
            saved.add(token);
            return token;
        }
    }
}
