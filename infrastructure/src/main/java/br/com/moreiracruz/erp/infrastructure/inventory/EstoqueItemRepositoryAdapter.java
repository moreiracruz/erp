package br.com.moreiracruz.erp.infrastructure.inventory;

import br.com.moreiracruz.erp.modules.inventory.domain.model.EstoqueItem;
import br.com.moreiracruz.erp.modules.inventory.domain.port.out.EstoqueItemRepository;
import br.com.moreiracruz.erp.shared.exceptions.NotFoundException;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing the domain's {@link EstoqueItemRepository} outbound port
 * using Spring Data JPA.
 *
 * <p>Performs manual mapping between {@link EstoqueItemJpaEntity} (persistence concern)
 * and {@link EstoqueItem} (domain aggregate root).
 */
@Repository
public class EstoqueItemRepositoryAdapter implements EstoqueItemRepository {

    private final EstoqueItemJpaRepository jpaRepository;

    public EstoqueItemRepositoryAdapter(EstoqueItemJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<EstoqueItem> findByVarianteUuid(UUID uuid) {
        return jpaRepository.findByVarianteUuid(uuid).map(this::toDomain);
    }

    @Override
    public EstoqueItem findByVarianteUuidForUpdate(UUID uuid) {
        return jpaRepository.findByVarianteUuidForUpdate(uuid)
                .map(this::toDomain)
                .orElseThrow(() -> new NotFoundException(
                        "EstoqueItem not found for varianteUuid: " + uuid));
    }

    @Override
    public EstoqueItem findOrCreateByVarianteUuid(UUID uuid) {
        return jpaRepository.findByVarianteUuid(uuid)
                .map(this::toDomain)
                .orElseGet(() -> EstoqueItem.create(uuid));
    }

    @Override
    public EstoqueItem save(EstoqueItem item) {
        EstoqueItemJpaEntity entity = toEntity(item);
        EstoqueItemJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    // -------------------------------------------------------------------------
    // Mapping helpers
    // -------------------------------------------------------------------------

    private EstoqueItem toDomain(EstoqueItemJpaEntity e) {
        return EstoqueItem.restore(
                e.getId(),
                e.getVarianteUuid(),
                e.getPhysicalStock(),
                e.getReservedStock(),
                e.getVersion());
    }

    private EstoqueItemJpaEntity toEntity(EstoqueItem item) {
        EstoqueItemJpaEntity e = new EstoqueItemJpaEntity();
        e.setId(item.getId());
        e.setVarianteUuid(item.getVarianteUuid());
        e.setPhysicalStock(item.getPhysicalStock());
        e.setReservedStock(item.getReservedStock());
        e.setVersion(item.getVersion());
        return e;
    }
}
