package com.erp.modules.product.application.usecase;

import com.erp.modules.product.domain.port.in.SearchVariantUseCase;
import com.erp.modules.product.domain.port.in.VarianteResponse;
import com.erp.modules.product.domain.port.out.VarianteRepository;
import com.erp.shared.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

/**
 * Use case implementation for searching product variants by SKU or barcode.
 */
@Service
public class SearchVariantUseCaseImpl implements SearchVariantUseCase {

    private final VarianteRepository varianteRepository;

    public SearchVariantUseCaseImpl(VarianteRepository varianteRepository) {
        this.varianteRepository = varianteRepository;
    }

    @Override
    public VarianteResponse findBySku(String sku) {
        return varianteRepository.findBySku(sku)
                .map(RegisterVariantUseCaseImpl::toResponse)
                .orElseThrow(() -> new NotFoundException("Variante não encontrada"));
    }

    @Override
    public VarianteResponse findByBarcode(String barcode) {
        return varianteRepository.findByBarcode(barcode)
                .map(RegisterVariantUseCaseImpl::toResponse)
                .orElseThrow(() -> new NotFoundException("Variante não encontrada"));
    }
}
