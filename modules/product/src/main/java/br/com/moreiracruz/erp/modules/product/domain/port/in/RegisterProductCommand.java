package br.com.moreiracruz.erp.modules.product.domain.port.in;

/**
 * Command to register a new product in the catalog.
 */
public record RegisterProductCommand(String name, String brand, String category) {}
