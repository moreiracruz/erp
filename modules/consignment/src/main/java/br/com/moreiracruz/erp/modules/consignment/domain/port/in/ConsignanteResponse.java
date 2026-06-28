package br.com.moreiracruz.erp.modules.consignment.domain.port.in;

import java.time.Instant;
import java.util.UUID;

public record ConsignanteResponse(
        UUID uuid,
        String name,
        String document,
        String email,
        String phone,
        boolean active,
        Instant createdAt
) {}
