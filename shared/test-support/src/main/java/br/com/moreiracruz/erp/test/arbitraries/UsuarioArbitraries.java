package br.com.moreiracruz.erp.test.arbitraries;

import br.com.moreiracruz.erp.modules.auth.domain.model.Role;
import br.com.moreiracruz.erp.modules.auth.domain.model.Usuario;
import br.com.moreiracruz.erp.modules.auth.domain.model.UsuarioStatus;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;

import java.time.Instant;
import java.util.UUID;

/**
 * Jqwik Arbitrary providers for the Auth module domain objects.
 */
public class UsuarioArbitraries {

    public static Arbitrary<Usuario> validUsuario() {
        return Combinators.combine(
                Arbitraries.create(UUID::randomUUID),
                validUsername(),
                validRole()
        ).as((uuid, username, role) ->
                Usuario.reconstruct(null, uuid, username,
                        "$2a$10$dummyHash0000000000000000000000000000000000000000",
                        role, true, UsuarioStatus.ACTIVE, 0, null, Instant.now())
        );
    }

    public static Arbitrary<Role> validRole() {
        return Arbitraries.of(Role.ROLE_MANAGER, Role.ROLE_CASHIER,
                Role.ROLE_STOCK, Role.ROLE_FINANCE);
    }

    public static Arbitrary<String> validUsername() {
        return Arbitraries.strings()
                .alpha().numeric()
                .ofMinLength(3).ofMaxLength(20)
                .map(s -> s.toLowerCase() + "@example.com");
    }

    public static Arbitrary<Integer> failedAttemptsBelowThreshold() {
        return Arbitraries.integers().between(1, 4);
    }

    public static Arbitrary<Integer> failedAttemptsAboveThreshold() {
        return Arbitraries.integers().between(5, 20);
    }
}
