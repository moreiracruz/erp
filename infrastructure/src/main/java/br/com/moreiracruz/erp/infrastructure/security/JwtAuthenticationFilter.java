package br.com.moreiracruz.erp.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Servlet filter that extracts and validates Bearer JWT tokens from incoming
 * HTTP requests and populates the Spring Security {@code SecurityContext}.
 *
 * <p>Runs once per request, before the standard
 * {@link org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter}.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = jwtTokenProvider.validateAndExtract(token);
                UUID userUuid = UUID.fromString(claims.getSubject());
                String role = claims.get("role", String.class);

                List<GrantedAuthority> authorities = authoritiesFor(role);

                var auth = new UsernamePasswordAuthenticationToken(
                        userUuid, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"Não autenticado\"}");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private List<GrantedAuthority> authoritiesFor(String role) {
        if ("ROLE_SUPER_ADMIN".equals(role)) {
            return List.of(
                    new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_MANAGER"),
                    new SimpleGrantedAuthority("ROLE_CASHIER"),
                    new SimpleGrantedAuthority("ROLE_STOCK"),
                    new SimpleGrantedAuthority("ROLE_FINANCE"));
        }
        return List.of(new SimpleGrantedAuthority(role));
    }
}
