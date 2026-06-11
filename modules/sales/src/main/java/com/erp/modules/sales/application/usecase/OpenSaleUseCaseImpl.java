package com.erp.modules.sales.application.usecase;

import com.erp.modules.sales.domain.model.Venda;
import com.erp.modules.sales.domain.port.in.OpenSaleCommand;
import com.erp.modules.sales.domain.port.in.OpenSaleUseCase;
import com.erp.modules.sales.domain.port.in.VendaResponse;
import com.erp.modules.sales.domain.port.out.VendaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class OpenSaleUseCaseImpl implements OpenSaleUseCase {

    private final VendaRepository vendaRepository;

    public OpenSaleUseCaseImpl(VendaRepository vendaRepository) {
        this.vendaRepository = vendaRepository;
    }

    @Override
    public VendaResponse open(UUID operatorUuid, OpenSaleCommand cmd) {
        Venda venda = Venda.create(operatorUuid, cmd.terminalId(), cmd.clienteUuid());
        venda = vendaRepository.save(venda);
        return VendaResponseMapper.toResponse(venda);
    }
}
