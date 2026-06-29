package br.com.moreiracruz.erp.shared.kernel;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Cross-module port for financial entries created by operational modules.
 */
public interface FinancePort {

    /**
     * Registers a supplier expense for an operational reference.
     */
    UUID registerSupplierExpense(BigDecimal amount, String description, UUID responsibleUuid, UUID referenceUuid);

    /**
     * Registers revenue for an operational consignment settlement.
     */
    UUID registerConsignmentRevenue(BigDecimal amount, String description, UUID responsibleUuid, UUID referenceUuid);
}
