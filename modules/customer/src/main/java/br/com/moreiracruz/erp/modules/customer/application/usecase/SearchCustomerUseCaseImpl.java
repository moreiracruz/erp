package br.com.moreiracruz.erp.modules.customer.application.usecase;

import br.com.moreiracruz.erp.modules.customer.domain.model.Cliente;
import br.com.moreiracruz.erp.modules.customer.domain.port.in.ClienteResponse;
import br.com.moreiracruz.erp.modules.customer.domain.port.in.CustomerSearchQuery;
import br.com.moreiracruz.erp.modules.customer.domain.port.in.SearchCustomerUseCase;
import br.com.moreiracruz.erp.modules.customer.domain.port.out.ClienteRepository;
import br.com.moreiracruz.erp.shared.kernel.pagination.PageQuery;
import br.com.moreiracruz.erp.shared.kernel.pagination.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SearchCustomerUseCaseImpl implements SearchCustomerUseCase {

    private final ClienteRepository clienteRepository;

    public SearchCustomerUseCaseImpl(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    public PageResult<ClienteResponse> search(CustomerSearchQuery query) {
        PageQuery pageQuery = new PageQuery(query.page(), query.size());

        // Search by UUID
        if (query.uuid() != null) {
            return clienteRepository.findByUuid(query.uuid())
                    .map(c -> PageResult.single(toResponse(c), pageQuery))
                    .orElse(PageResult.empty(pageQuery));
        }

        // Search by CPF
        if (query.cpf() != null && !query.cpf().isBlank()) {
            return clienteRepository.findByCpf(query.cpf())
                    .map(c -> PageResult.single(toResponse(c), pageQuery))
                    .orElse(PageResult.empty(pageQuery));
        }

        // Search by name (partial match)
        if (query.name() != null && !query.name().isBlank()) {
            return clienteRepository.searchByName(query.name(), pageQuery).map(this::toResponse);
        }

        // No filter — return empty
        return PageResult.empty(pageQuery);
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
