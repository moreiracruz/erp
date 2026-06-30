package br.com.moreiracruz.erp.modules.auth.adapter.in.web;

/**
 * Response body carrying an issued token pair.
 *
 * @param accessToken  signed JWT access token
 * @param refreshToken opaque refresh token
 * @param expiresIn    remaining validity of the access token in seconds
 */
public record TokenPairResponse(String accessToken, String refreshToken, long expiresIn) {}
