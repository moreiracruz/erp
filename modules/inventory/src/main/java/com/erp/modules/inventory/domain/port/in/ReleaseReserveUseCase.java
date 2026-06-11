package com.erp.modules.inventory.domain.port.in;

import java.util.UUID;

public interface ReleaseReserveUseCase {
    void release(UUID reservaUuid);
}
