package com.erp.modules.auth.application.usecase;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility for hashing refresh-token strings before persistence.
 *
 * <p>Only the SHA-256 digest is stored in the database; the plaintext raw token
 * travels to the client once and is never persisted.
 */
public final class TokenHasher {

    private TokenHasher() {}

    /**
     * Computes the SHA-256 hex digest of the given raw string.
     *
     * @param raw the plaintext string to hash (e.g., a raw refresh token)
     * @return lowercase hex representation of the SHA-256 digest (64 characters)
     * @throws IllegalStateException if SHA-256 is unavailable on the JVM — should never happen
     */
    public static String sha256hex(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hashBytes.length * 2);
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
