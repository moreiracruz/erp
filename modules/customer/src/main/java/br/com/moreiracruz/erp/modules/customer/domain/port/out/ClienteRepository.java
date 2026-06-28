package br.com.moreiracruz.erp.modules.customer.domain.port.out;

import br.com.moreiracruz.erp.modules.customer.domain.model.Cliente;
import br.com.moreiracruz.erp.shared.kernel.pagination.PageQuery;
import br.com.moreiracruz.erp.shared.kernel.pagination.PageResult;

import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port for customer persistence.
 */
public interface ClienteRepository {

    Optional<Cliente> findByUuid(UUID uuid);

    Optional<Cliente> findByCpf(String cpf);

    boolean existsByCpf(String cpf);

    Cliente save(Cliente cliente);

    PageResult<Cliente> searchByName(String partial, PageQuery pageQuery);
}
