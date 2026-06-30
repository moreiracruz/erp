package br.com.moreiracruz.erp.modules.auth.domain.model;

/**
 * Value object returned after a successful login or token refresh.
 *
 * @param accessToken  signed JWT, valid for {@code expiresIn} seconds
 * @param refreshToken opaque random token (hex string), valid for 7 days
 * @param expiresIn    remaining validity of the access token in seconds (typically 900)
 */
public record TokenPair(String accessToken, String refreshToken, long expiresIn) {}
