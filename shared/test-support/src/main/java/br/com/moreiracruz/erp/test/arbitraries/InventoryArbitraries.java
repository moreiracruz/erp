package br.com.moreiracruz.erp.test.arbitraries;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

import java.util.List;

/**
 * Jqwik Arbitrary providers for the Inventory module domain objects.
 */
public class InventoryArbitraries {

    public static Arbitrary<Integer> validStockLevel() {
        return Arbitraries.integers().between(1, 100);
    }

    public static Arbitrary<Integer> validThreadCount() {
        return Arbitraries.integers().between(2, 20);
    }

    public static Arbitrary<Integer> validOperationQuantity() {
        return Arbitraries.integers().between(1, 100);
    }

    public enum StockOperation { ENTRY, WITHDRAWAL, RESERVE, RELEASE }

    public static Arbitrary<StockOperation> stockOperation() {
        return Arbitraries.of(StockOperation.values());
    }

    public static Arbitrary<List<StockOperation>> operationSequence() {
        return stockOperation().list().ofMinSize(5).ofMaxSize(50);
    }
}
