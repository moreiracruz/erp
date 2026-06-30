package br.com.moreiracruz.erp.modules.sales.domain.port.in;

import java.util.UUID;

/**
 * Inbound port for adding an item to an existing sale.
 */
public interface AddItemUseCase {

    VendaResponse addItem(UUID vendaUuid, AddItemCommand cmd);
}
