package br.com.moreiracruz.erp.modules.auth.domain.port.in;

public record CreateUserCommand(String username, String password, String role) {}
