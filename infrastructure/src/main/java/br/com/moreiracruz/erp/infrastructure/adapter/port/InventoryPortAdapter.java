package br.com.moreiracruz.erp.infrastructure.adapter.port;

import br.com.moreiracruz.erp.modules.inventory.domain.port.in.CommitReserveUseCase;
import br.com.moreiracruz.erp.modules.inventory.domain.port.in.ReleaseReserveUseCase;
import br.com.moreiracruz.erp.modules.inventory.domain.port.in.RegisterEntryUseCase;
import br.com.moreiracruz.erp.modules.inventory.domain.port.in.RegisterWithdrawalUseCase;
import br.com.moreiracruz.erp.modules.inventory.domain.port.in.ReserveStockUseCase;
import br.com.moreiracruz.erp.modules.inventory.domain.port.in.StockEntryCommand;
import br.com.moreiracruz.erp.modules.inventory.domain.port.in.StockReserveCommand;
import br.com.moreiracruz.erp.modules.inventory.domain.port.in.StockWithdrawalCommand;
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
    private final RegisterEntryUseCase registerEntryUseCase;
    private final RegisterWithdrawalUseCase registerWithdrawalUseCase;

    public InventoryPortAdapter(ReserveStockUseCase reserveStockUseCase,
                                CommitReserveUseCase commitReserveUseCase,
                                ReleaseReserveUseCase releaseReserveUseCase,
                                RegisterEntryUseCase registerEntryUseCase,
                                RegisterWithdrawalUseCase registerWithdrawalUseCase) {
        this.reserveStockUseCase = reserveStockUseCase;
        this.commitReserveUseCase = commitReserveUseCase;
        this.releaseReserveUseCase = releaseReserveUseCase;
        this.registerEntryUseCase = registerEntryUseCase;
        this.registerWithdrawalUseCase = registerWithdrawalUseCase;
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

    @Override
    public void registerEntry(UUID varianteUuid, int quantity, UUID actorUuid, UUID referenceUuid) {
        registerEntryUseCase.registerEntry(new StockEntryCommand(varianteUuid, quantity, actorUuid, referenceUuid));
    }

    @Override
    public void registerWithdrawal(UUID varianteUuid, int quantity, UUID actorUuid, UUID referenceUuid) {
        registerWithdrawalUseCase.registerWithdrawal(new StockWithdrawalCommand(varianteUuid, quantity, actorUuid, referenceUuid));
    }
}
