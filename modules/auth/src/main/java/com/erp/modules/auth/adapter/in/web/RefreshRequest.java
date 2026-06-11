package com.erp.modules.auth.adapter.in.web;

/**
 * Request body for POST /api/v1/auth/refresh.
 *
 * @param refreshToken the opaque refresh token string received from the client
 */
public record RefreshRequest(String refreshToken) {}
