package com.erp.modules.inventory.domain.port.in;

import java.util.UUID;

public record StockWithdrawalCommand(UUID varianteUuid, int quantity, UUID actorUuid) {}
