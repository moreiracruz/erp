package com.erp.modules.inventory.domain.port.in;

import java.time.Instant;
import java.util.UUID;

public record ReservaResponse(UUID uuid, UUID varianteUuid, UUID saleUuid, int quantity, Instant expiresAt) {}
