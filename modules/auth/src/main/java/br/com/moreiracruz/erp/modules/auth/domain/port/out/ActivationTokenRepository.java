package br.com.moreiracruz.erp.modules.auth.domain.port.out;

import br.com.moreiracruz.erp.modules.auth.domain.model.ActivationToken;
import br.com.moreiracruz.erp.modules.auth.domain.model.ActivationTokenPurpose;

import java.util.Optional;
import java.util.UUID;

public interface ActivationTokenRepository {

    Optional<ActivationToken> findByTokenHash(String tokenHash);

    void markActiveTokensUsed(UUID usuarioUuid, ActivationTokenPurpose purpose);

    ActivationToken save(ActivationToken token);
}
