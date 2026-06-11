package com.erp.modules.inventory.application.usecase;

import com.erp.modules.inventory.domain.port.in.GetStockUseCase;
import com.erp.modules.inventory.domain.port.in.StockResponse;
import com.erp.modules.inventory.domain.port.out.EstoqueItemRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GetStockUseCaseImpl implements GetStockUseCase {

    private final EstoqueItemRepository estoqueItemRepo;

    public GetStockUseCaseImpl(EstoqueItemRepository estoqueItemRepo) {
        this.estoqueItemRepo = estoqueItemRepo;
    }

    @Override
    public StockResponse getStock(UUID varianteUuid) {
        return estoqueItemRepo.findByVarianteUuid(varianteUuid)
                .map(item -> new StockResponse(
                        item.getVarianteUuid(),
                        item.getPhysicalStock(),
                        item.getReservedStock(),
                        item.availableStock()))
                .orElseGet(() -> new StockResponse(varianteUuid, 0, 0, 0));
    }
}
