package br.com.moreiracruz.erp.modules.customer.adapter.out.persistence;

import br.com.moreiracruz.erp.modules.customer.domain.model.Cliente;
import br.com.moreiracruz.erp.modules.customer.domain.port.out.ClienteRepository;
import br.com.moreiracruz.erp.shared.kernel.pagination.PageQuery;
import br.com.moreiracruz.erp.shared.kernel.pagination.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ClienteRepositoryAdapter implements ClienteRepository {

    private final ClienteJpaRepository jpaRepository;

    public ClienteRepositoryAdapter(ClienteJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Cliente> findByUuid(UUID uuid) {
        return jpaRepository.findByUuid(uuid).map(this::toDomain);
    }

    @Override
    public Optional<Cliente> findByCpf(String cpf) {
        return jpaRepository.findByCpf(cpf).map(this::toDomain);
    }

    @Override
    public boolean existsByCpf(String cpf) {
        return jpaRepository.existsByCpf(cpf);
    }

    @Override
    public Cliente save(Cliente cliente) {
        ClienteJpaEntity entity = toEntity(cliente);
        ClienteJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public PageResult<Cliente> searchByName(String partial, PageQuery pageQuery) {
        Pageable pageable = PageRequest.of(pageQuery.page(), pageQuery.size());
        Page<Cliente> page = jpaRepository.searchByName(partial, pageable).map(this::toDomain);
        return new PageResult<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements());
    }

    private Cliente toDomain(ClienteJpaEntity entity) {
        return Cliente.restore(
                entity.getUuid(),
                entity.getFullName(),
                entity.getCpf(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getBirthDate(),
                entity.isActive(),
                entity.getCreatedAt()
        );
    }

    private ClienteJpaEntity toEntity(Cliente c) {
        ClienteJpaEntity entity = new ClienteJpaEntity();
        entity.setUuid(c.getUuid());
        entity.setFullName(c.getFullName());
        entity.setCpf(c.getCpf().value());
        entity.setEmail(c.getEmail() != null ? c.getEmail().value() : null);
        entity.setPhone(c.getPhone());
        entity.setBirthDate(c.getBirthDate());
        entity.setActive(c.isActive());
        entity.setCreatedAt(c.getCreatedAt());
        return entity;
    }
}
