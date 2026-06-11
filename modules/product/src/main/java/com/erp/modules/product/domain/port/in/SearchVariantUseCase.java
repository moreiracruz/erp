package com.erp.modules.product.domain.port.in;

/**
 * Inbound port for searching product variants by SKU or barcode.
 */
public interface SearchVariantUseCase {

    VarianteResponse findBySku(String sku);

    VarianteResponse findByBarcode(String barcode);
}
