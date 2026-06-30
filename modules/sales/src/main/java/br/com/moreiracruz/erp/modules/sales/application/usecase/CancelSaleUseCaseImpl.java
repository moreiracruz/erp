package br.com.moreiracruz.erp.modules.sales.application.usecase;

import br.com.moreiracruz.erp.modules.sales.domain.model.Venda;
import br.com.moreiracruz.erp.modules.sales.domain.port.in.CancelSaleCommand;
import br.com.moreiracruz.erp.modules.sales.domain.port.in.CancelSaleUseCase;
import br.com.moreiracruz.erp.modules.sales.domain.port.out.VendaRepository;
import br.com.moreiracruz.erp.shared.exceptions.NotFoundException;
import br.com.moreiracruz.erp.shared.kernel.InventoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CancelSaleUseCaseImpl implements CancelSaleUseCase {

    private final VendaRepository vendaRepository;
    private final InventoryPort inventoryPort;

    public CancelSaleUseCaseImpl(VendaRepository vendaRepository, InventoryPort inventoryPort) {
        this.vendaRepository = vendaRepository;
        this.inventoryPort = inventoryPort;
    }

    @Override
    public void cancel(UUID vendaUuid, CancelSaleCommand cmd) {
        Venda venda = vendaRepository.findByUuid(vendaUuid)
                .orElseThrow(() -> new NotFoundException("Venda não encontrada: " + vendaUuid));

        inventoryPort.releaseAll(venda.getUuid());
        venda.cancel(cmd.reason());
        vendaRepository.save(venda);
    }
}
