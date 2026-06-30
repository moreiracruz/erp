package br.com.moreiracruz.erp.infrastructure.security;

import br.com.moreiracruz.erp.modules.auth.domain.port.out.JwtPort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Infrastructure component that signs and validates JWT tokens using JJWT 0.12.x.
 *
 * <p>Implements {@link JwtPort} for the auth domain's outbound port.
 *
 * <p>Configuration properties:
 * <ul>
 *   <li>{@code jwt.secret} — HS256 signing secret (loaded from environment)</li>
 *   <li>{@code jwt.expiration-seconds} — token lifetime in seconds (default 900)</li>
 * </ul>
 */
@Component
public class JwtTokenProvider implements JwtPort {

    private final SecretKey signingKey;
    private final long expirationSeconds;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-seconds:900}") long expirationSeconds) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = expirationSeconds;
    }

    /**
     * Generates a signed HS256 JWT.
     *
     * @param userUuid UUID of the authenticated user (becomes the {@code sub} claim)
     * @param role     role string (e.g., {@code "ROLE_MANAGER"})
     * @return compact JWT string
     */
    @Override
    public String generateToken(UUID userUuid, String role) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(expirationSeconds);

        return Jwts.builder()
                .subject(userUuid.toString())
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Parses and validates a JWT, returning its claims.
     *
     * @param token compact JWT string
     * @return validated {@link Claims}
     * @throws io.jsonwebtoken.JwtException if the token is invalid or expired
     */
    public Claims validateAndExtract(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extracts the user UUID from the {@code sub} claim.
     *
     * @param claims validated JWT claims
     * @return user UUID
     */
    public UUID extractUserUuid(Claims claims) {
        return UUID.fromString(claims.getSubject());
    }

    /**
     * Extracts the {@code role} claim.
     *
     * @param claims validated JWT claims
     * @return role string
     */
    public String extractRole(Claims claims) {
        return claims.get("role", String.class);
    }
}
