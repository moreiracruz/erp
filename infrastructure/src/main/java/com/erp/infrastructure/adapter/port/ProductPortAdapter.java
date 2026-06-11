package com.erp.infrastructure.adapter.port;

import com.erp.modules.product.domain.port.in.SearchVariantUseCase;
import com.erp.modules.product.domain.port.in.VarianteResponse;
import com.erp.modules.sales.domain.port.out.ProductPort;
import org.springframework.stereotype.Component;

/**
 * Adapter that bridges the sales module's {@link ProductPort} to the Product
 * module's inbound use case. Lives in infrastructure to avoid direct
 * module-to-module dependencies.
 */
@Component
public class ProductPortAdapter implements ProductPort {

    private final SearchVariantUseCase searchVariantUseCase;

    public ProductPortAdapter(SearchVariantUseCase searchVariantUseCase) {
        this.searchVariantUseCase = searchVariantUseCase;
    }

    @Override
    public VariantInfo findByBarcode(String barcode) {
        VarianteResponse response = searchVariantUseCase.findByBarcode(barcode);
        return new VariantInfo(response.uuid(), response.sku(), response.price());
    }
}
