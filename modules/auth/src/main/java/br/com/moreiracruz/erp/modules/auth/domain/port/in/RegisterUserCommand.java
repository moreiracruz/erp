package br.com.moreiracruz.erp.modules.auth.domain.port.in;

public record RegisterUserCommand(String fullName, String email, String password, String phone, String cpf) {}
