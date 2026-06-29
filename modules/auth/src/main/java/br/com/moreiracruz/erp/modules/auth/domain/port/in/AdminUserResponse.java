package br.com.moreiracruz.erp.modules.auth.domain.port.in;

import java.time.Instant;
import java.util.UUID;

public record AdminUserResponse(
        UUID uuid,
        String username,
        String role,
        boolean active,
        int failedAttempts,
        Instant lockedUntil,
        Instant createdAt
) {}
