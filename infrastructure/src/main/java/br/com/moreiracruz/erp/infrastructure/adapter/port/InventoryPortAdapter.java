package br.com.moreiracruz.erp.infrastructure.adapter.port;

import br.com.moreiracruz.erp.modules.inventory.domain.port.in.CommitReserveUseCase;
import br.com.moreiracruz.erp.modules.inventory.domain.port.in.ReleaseReserveUseCase;
import br.com.moreiracruz.erp.modules.inventory.domain.port.in.ReserveStockUseCase;
import br.com.moreiracruz.erp.modules.inventory.domain.port.in.StockReserveCommand;
import br.com.moreiracruz.erp.shared.exceptions.InsufficientStockException;
import br.com.moreiracruz.erp.shared.kernel.InventoryPort;
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
    private final ReleaseReserveUseCase releaseReserveUseCase;

    public InventoryPortAdapter(ReserveStockUseCase reserveStockUseCase,
                                CommitReserveUseCase commitReserveUseCase,
                                ReleaseReserveUseCase releaseReserveUseCase) {
        this.reserveStockUseCase = reserveStockUseCase;
        this.commitReserveUseCase = commitReserveUseCase;
        this.releaseReserveUseCase = releaseReserveUseCase;
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
        releaseReserveUseCase.releaseAllBySaleUuid(saleUuid);
    }

    @Override
    public void commitAll(UUID saleUuid) {
        commitReserveUseCase.commit(saleUuid);
    }
}
