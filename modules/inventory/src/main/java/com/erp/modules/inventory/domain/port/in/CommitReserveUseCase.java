package com.erp.modules.inventory.domain.port.in;

import java.util.UUID;

public interface CommitReserveUseCase {
    void commit(UUID saleUuid);
}
