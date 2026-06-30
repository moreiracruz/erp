package br.com.moreiracruz.erp.modules.inventory.adapter.in.web;

import java.util.UUID;

/**
 * Request payload for stock entry and withdrawal operations.
 *
 * @param quantity  number of units to add or remove (must be &gt; 0)
 * @param actorUuid UUID of the user performing the operation
 */
public record StockOperationRequest(int quantity, UUID actorUuid) {}
