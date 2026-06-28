package br.com.moreiracruz.erp.modules.product.domain.port.in;

/**
 * Command to update an existing product's attributes.
 */
public record UpdateProductCommand(String name, String brand, String category) {}
