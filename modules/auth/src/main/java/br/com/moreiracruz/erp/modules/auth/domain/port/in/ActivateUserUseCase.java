package br.com.moreiracruz.erp.modules.auth.domain.port.in;

public interface ActivateUserUseCase {
    void activate(ActivateUserCommand command);
}
