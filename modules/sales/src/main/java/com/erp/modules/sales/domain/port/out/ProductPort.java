package com.erp.modules.sales.domain.port.out;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Output port for the sales module to look up product variant information
 * without depending directly on the product module.
 */
public interface ProductPort {

    VariantInfo findByBarcode(String barcode);

    record VariantInfo(UUID uuid, String sku, BigDecimal price) {}
}
