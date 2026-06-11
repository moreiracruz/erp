package com.erp.modules.customer.application.usecase;

import com.erp.modules.customer.domain.port.in.DeactivateCustomerUseCase;
import com.erp.modules.customer.domain.port.out.ClienteRepository;
import com.erp.shared.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DeactivateCustomerUseCaseImpl implements DeactivateCustomerUseCase {

    private final ClienteRepository clienteRepository;

    public DeactivateCustomerUseCaseImpl(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public void deactivate(UUID uuid) {
        var cliente = clienteRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado"));
        cliente.deactivate();
        clienteRepository.save(cliente);
    }
}
