package br.com.moreiracruz.erp.modules.auth.application.usecase;

import java.security.SecureRandom;

/**
 * Utility for generating cryptographically random opaque token strings.
 *
 * <p>Each token is 32 bytes of {@link SecureRandom} output encoded as a
 * 64-character lowercase hex string.
 */
public final class RandomTokenGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private RandomTokenGenerator() {}

    /**
     * Generates a new random token.
     *
     * @return a 64-character lowercase hex string derived from 32 random bytes
     */
    public static String generate() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
