package com.erp.modules.inventory.domain.port.out;

import com.erp.modules.inventory.domain.model.EstoqueItem;

import java.util.Optional;
import java.util.UUID;

public interface EstoqueItemRepository {

    Optional<EstoqueItem> findByVarianteUuid(UUID uuid);

    /** Issues SELECT FOR UPDATE — must be called within an active transaction. */
    EstoqueItem findByVarianteUuidForUpdate(UUID uuid);

    /** Returns the existing item or creates a new one with zero counters. */
    EstoqueItem findOrCreateByVarianteUuid(UUID uuid);

    EstoqueItem save(EstoqueItem item);
}
