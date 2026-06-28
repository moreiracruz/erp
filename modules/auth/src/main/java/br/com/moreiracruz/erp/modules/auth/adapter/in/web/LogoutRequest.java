package br.com.moreiracruz.erp.modules.auth.adapter.in.web;

/**
 * Request body for POST /api/v1/auth/logout.
 *
 * @param refreshToken the refresh token to be revoked
 */
public record LogoutRequest(String refreshToken) {}
