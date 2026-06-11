package com.erp.modules.auth.adapter.in.web;

/**
 * Request body for POST /api/v1/auth/login.
 *
 * @param username login name / e-mail
 * @param password raw (unhashed) password
 */
public record LoginRequest(String username, String password) {}
