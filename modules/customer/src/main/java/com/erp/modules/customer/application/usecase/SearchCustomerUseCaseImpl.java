package com.erp.modules.customer.application.usecase;

import com.erp.modules.customer.domain.model.Cliente;
import com.erp.modules.customer.domain.port.in.ClienteResponse;
import com.erp.modules.customer.domain.port.in.CustomerSearchQuery;
import com.erp.modules.customer.domain.port.in.SearchCustomerUseCase;
import com.erp.modules.customer.domain.port.out.ClienteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SearchCustomerUseCaseImpl implements SearchCustomerUseCase {

    private final ClienteRepository clienteRepository;

    public SearchCustomerUseCaseImpl(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public Page<ClienteResponse> search(CustomerSearchQuery query) {
        Pageable pageable = PageRequest.of(query.page(), query.size());

        // Search by UUID
        if (query.uuid() != null) {
            return clienteRepository.findByUuid(query.uuid())
                    .<Page<ClienteResponse>>map(c -> new PageImpl<>(List.of(toResponse(c)), pageable, 1))
                    .orElse(Page.empty(pageable));
        }

        // Search by CPF
        if (query.cpf() != null && !query.cpf().isBlank()) {
            return clienteRepository.findByCpf(query.cpf())
                    .<Page<ClienteResponse>>map(c -> new PageImpl<>(List.of(toResponse(c)), pageable, 1))
                    .orElse(Page.empty(pageable));
        }

        // Search by name (partial match)
        if (query.name() != null && !query.name().isBlank()) {
            Page<Cliente> page = clienteRepository.searchByName(query.name(), pageable);
            return page.map(this::toResponse);
        }

        // No filter — return empty
        return Page.empty(pageable);
    }

    private ClienteResponse toResponse(Cliente c) {
        return new ClienteResponse(
                c.getUuid(),
                c.getFullName(),
                c.getCpf().value(),
                c.getEmail() != null ? c.getEmail().value() : null,
                c.getPhone(),
                c.getBirthDate(),
                c.isActive()
        );
    }
}
