package com.erp.modules.inventory.domain.port.in;

import java.time.Instant;
import java.util.UUID;

public record MovimentoResponse(UUID uuid, String operationType, int quantity, Instant occurredAt, UUID actorUuid) {}
