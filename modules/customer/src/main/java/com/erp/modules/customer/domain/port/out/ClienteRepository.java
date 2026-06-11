package com.erp.modules.customer.domain.port.out;

import com.erp.modules.customer.domain.model.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    Page<Cliente> searchByName(String partial, Pageable pageable);
}
