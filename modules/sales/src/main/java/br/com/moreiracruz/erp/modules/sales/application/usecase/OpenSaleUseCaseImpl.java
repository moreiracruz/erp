package br.com.moreiracruz.erp.modules.sales.application.usecase;

import br.com.moreiracruz.erp.modules.sales.domain.model.Venda;
import br.com.moreiracruz.erp.modules.sales.domain.port.in.OpenSaleCommand;
import br.com.moreiracruz.erp.modules.sales.domain.port.in.OpenSaleUseCase;
import br.com.moreiracruz.erp.modules.sales.domain.port.in.VendaResponse;
import br.com.moreiracruz.erp.modules.sales.domain.port.out.VendaRepository;
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
