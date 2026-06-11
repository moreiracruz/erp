package com.erp.modules.sales.application.usecase;

import com.erp.modules.sales.domain.model.ItemVenda;
import com.erp.modules.sales.domain.model.Venda;
import com.erp.modules.sales.domain.port.in.AddItemCommand;
import com.erp.modules.sales.domain.port.in.AddItemUseCase;
import com.erp.modules.sales.domain.port.in.VendaResponse;
import com.erp.modules.sales.domain.port.out.VendaRepository;
import com.erp.shared.exceptions.NotFoundException;
import com.erp.shared.exceptions.ValidationException;
import com.erp.shared.kernel.InventoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class AddItemUseCaseImpl implements AddItemUseCase {

    private final VendaRepository vendaRepository;
    private final InventoryPort inventoryPort;

    public AddItemUseCaseImpl(VendaRepository vendaRepository, InventoryPort inventoryPort) {
        this.vendaRepository = vendaRepository;
        this.inventoryPort = inventoryPort;
    }

    @Override
    public VendaResponse addItem(UUID vendaUuid, AddItemCommand cmd) {
        Venda venda = vendaRepository.findByUuid(vendaUuid)
                .orElseThrow(() -> new NotFoundException("Venda não encontrada: " + vendaUuid));

        int result = inventoryPort.reserve(cmd.varianteUuid(), venda.getUuid(), cmd.quantity());
        if (result != -1) {
            throw new ValidationException("Estoque insuficiente. Disponível: " + result);
        }

        ItemVenda item = ItemVenda.create(venda.getId(), cmd.varianteUuid(), cmd.sku(),
                cmd.quantity(), cmd.unitPrice());
        venda.addItem(item);

        venda = vendaRepository.save(venda);
        return VendaResponseMapper.toResponse(venda);
    }
}
