package br.com.moreiracruz.erp.modules.auth.domain.port.in;

import br.com.moreiracruz.erp.modules.auth.domain.model.TokenPair;

public interface RegisterUserUseCase {

    TokenPair register(RegisterUserCommand command);
}
