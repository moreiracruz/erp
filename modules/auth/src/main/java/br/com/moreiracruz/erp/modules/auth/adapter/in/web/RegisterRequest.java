package br.com.moreiracruz.erp.modules.auth.adapter.in.web;

public record RegisterRequest(String fullName, String email, String password, String phone, String cpf) {}
