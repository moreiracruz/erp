package br.com.moreiracruz.erp.modules.auth.domain.model;

/**
 * Value object carrying raw login credentials submitted by the user.
 *
 * <p>The password field is intentionally a plain {@code String} at this layer;
 * it is compared against the stored bcrypt hash inside the login use case and
 * never persisted.
 *
 * @param username login name / e-mail
 * @param password raw (unhashed) password
 */
public record Credentials(String username, String password) {}
