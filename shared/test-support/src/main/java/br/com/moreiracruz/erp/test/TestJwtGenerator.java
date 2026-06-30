package br.com.moreiracruz.erp.test;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

/**
 * Generates JWT tokens for test authentication against all roles.
 * The secret must match the one configured in application-test.yml.
 */
public class TestJwtGenerator {

    private static final String DEFAULT_TEST_SECRET =
            "test-secret-key-that-is-at-least-256-bits-long-for-hs256-signing";
    private static final String TEST_SECRET =
            System.getenv().getOrDefault("JWT_SECRET", DEFAULT_TEST_SECRET);
    private static final SecretKey KEY =
            Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
    private static final long EXPIRY_MINUTES = 15;

    /**
     * Generate a valid JWT for the given role and user UUID.
     */
    public static String generateToken(UUID userUuid, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userUuid.toString())
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(EXPIRY_MINUTES, ChronoUnit.MINUTES)))
                .signWith(KEY)
                .compact();
    }

    /** Generate token for a specific role with a random user UUID. */
    public static String generateToken(String role) {
        return generateToken(UUID.randomUUID(), role);
    }

    /** Generate an expired JWT. */
    public static String generateExpired(UUID userUuid, String role) {
        Instant past = Instant.now().minus(1, ChronoUnit.HOURS);
        return Jwts.builder()
                .subject(userUuid.toString())
                .claim("role", role)
                .issuedAt(Date.from(past.minus(15, ChronoUnit.MINUTES)))
                .expiration(Date.from(past))
                .signWith(KEY)
                .compact();
    }

    /** Generate a JWT with a role value not in the valid set. */
    public static String generateWithInvalidRole(String invalidRole) {
        return generateToken(UUID.randomUUID(), invalidRole);
    }

    /** Generate a JWT with tampered signature. */
    public static String generateTampered(UUID userUuid, String role) {
        String valid = generateToken(userUuid, role);
        String[] parts = valid.split("\\.");
        char firstSignatureChar = parts[2].charAt(0);
        char replacement = firstSignatureChar == 'A' ? 'B' : 'A';
        parts[2] = replacement + parts[2].substring(1);
        return String.join(".", parts);
    }

    /** Get the test secret for application-test.yml configuration. */
    public static String getTestSecret() {
        return TEST_SECRET;
    }
}
