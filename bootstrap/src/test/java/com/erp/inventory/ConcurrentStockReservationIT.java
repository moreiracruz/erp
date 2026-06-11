package com.erp.inventory;

import com.erp.modules.inventory.domain.port.in.RegisterEntryUseCase;
import com.erp.modules.inventory.domain.port.in.ReserveStockUseCase;
import com.erp.modules.inventory.domain.port.in.StockEntryCommand;
import com.erp.modules.inventory.domain.port.in.StockReserveCommand;
import com.erp.test.AbstractIntegrationTest;
import com.erp.test.ConcurrentTestResult;
import com.erp.test.ConcurrentTestRunner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test verifying concurrent stock reservation invariant.
 * Property 8: Concurrent reservation never exceeds available stock.
 */
class ConcurrentStockReservationIT extends AbstractIntegrationTest {

    @Autowired
    private RegisterEntryUseCase registerEntryUseCase;

    @Autowired
    private ReserveStockUseCase reserveStockUseCase;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("20 concurrent threads reserving 1 unit each from stock of 10 — at most 10 succeed")
    void concurrentReservations_neverExceedAvailableStock() {
        UUID varianteUuid = UUID.randomUUID();
        UUID actorUuid = UUID.randomUUID();
        UUID saleUuid = UUID.randomUUID();

        // Insert stock with physical_stock = 10
        registerEntryUseCase.registerEntry(new StockEntryCommand(varianteUuid, 10, actorUuid));

        // Launch 20 threads each trying to reserve 1 unit
        ConcurrentTestResult result = ConcurrentTestRunner.run(20, () -> {
            try {
                reserveStockUseCase.reserve(
                        new StockReserveCommand(varianteUuid, UUID.randomUUID(), 1));
                return true;
            } catch (Exception e) {
                return false;
            }
        });

        // Assert: successCount <= 10
        assertThat(result.successCount()).isLessThanOrEqualTo(10);
        assertThat(result.totalAttempts()).isEqualTo(20);

        // Assert DB: reserved_stock == successCount, physical_stock unchanged
        Integer physicalStock = jdbcTemplate.queryForObject(
                "SELECT physical_stock FROM estoque_items WHERE variante_uuid = ?",
                Integer.class, varianteUuid);
        Integer reservedStock = jdbcTemplate.queryForObject(
                "SELECT reserved_stock FROM estoque_items WHERE variante_uuid = ?",
                Integer.class, varianteUuid);

        assertThat(physicalStock).isEqualTo(10);
        assertThat(reservedStock).isEqualTo((int) result.successCount());

        // Assert invariant: physical_stock - reserved_stock >= 0
        assertThat(physicalStock - reservedStock).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("Concurrent reservations with larger stock — invariant holds")
    void concurrentReservations_largerStock_invariantHolds() {
        UUID varianteUuid = UUID.randomUUID();
        UUID actorUuid = UUID.randomUUID();

        // Insert stock with physical_stock = 5
        registerEntryUseCase.registerEntry(new StockEntryCommand(varianteUuid, 5, actorUuid));

        // Launch 15 threads each trying to reserve 1 unit
        ConcurrentTestResult result = ConcurrentTestRunner.run(15, () -> {
            try {
                reserveStockUseCase.reserve(
                        new StockReserveCommand(varianteUuid, UUID.randomUUID(), 1));
                return true;
            } catch (Exception e) {
                return false;
            }
        });

        // At most 5 should succeed
        assertThat(result.successCount()).isLessThanOrEqualTo(5);

        // Verify DB invariant
        Integer physicalStock = jdbcTemplate.queryForObject(
                "SELECT physical_stock FROM estoque_items WHERE variante_uuid = ?",
                Integer.class, varianteUuid);
        Integer reservedStock = jdbcTemplate.queryForObject(
                "SELECT reserved_stock FROM estoque_items WHERE variante_uuid = ?",
                Integer.class, varianteUuid);

        assertThat(physicalStock - reservedStock).isGreaterThanOrEqualTo(0);
    }
}
