package br.com.moreiracruz.erp.modules.auth.domain.port.in;

public record ActivateUserCommand(String token, String password) {}
