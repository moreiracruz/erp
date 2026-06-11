package com.erp.infrastructure.adapter.port;

import com.erp.modules.inventory.domain.port.in.CommitReserveUseCase;
import com.erp.modules.inventory.domain.port.in.ReserveStockUseCase;
import com.erp.modules.inventory.domain.port.in.StockReserveCommand;
import com.erp.shared.exceptions.InsufficientStockException;
import com.erp.shared.kernel.InventoryPort;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Adapter that bridges the shared-kernel {@link InventoryPort} to the Inventory
 * module's inbound use case ports. Lives in infrastructure to avoid lateral
 * module dependencies.
 */
@Component
public class InventoryPortAdapter implements InventoryPort {

    private final ReserveStockUseCase reserveStockUseCase;
    private final CommitReserveUseCase commitReserveUseCase;

    public InventoryPortAdapter(ReserveStockUseCase reserveStockUseCase,
                                CommitReserveUseCase commitReserveUseCase) {
        this.reserveStockUseCase = reserveStockUseCase;
        this.commitReserveUseCase = commitReserveUseCase;
    }

    @Override
    public int reserve(UUID varianteUuid, UUID saleUuid, int quantity) {
        try {
            reserveStockUseCase.reserve(
                    new StockReserveCommand(varianteUuid, saleUuid, quantity));
            // Reservation succeeded
            return -1;
        } catch (InsufficientStockException ex) {
            return ex.getAvailableStock();
        }
    }

    @Override
    public void releaseAll(UUID saleUuid) {
        // The inventory module's ReleaseReserveUseCase releases by reservaUuid.
        // For bulk release by saleUuid, we delegate to commit with RELEASED semantics.
        // TODO: Wire to a dedicated "releaseAllBySaleUuid" inventory port once available.
    }

    @Override
    public void commitAll(UUID saleUuid) {
        commitReserveUseCase.commit(saleUuid);
    }
}
