package br.com.moreiracruz.erp.modules.sales.adapter.out.persistence;

import br.com.moreiracruz.erp.modules.sales.domain.model.ItemVenda;
import br.com.moreiracruz.erp.modules.sales.domain.model.PaymentMethod;
import br.com.moreiracruz.erp.modules.sales.domain.model.Venda;
import br.com.moreiracruz.erp.modules.sales.domain.model.VendaStatus;
import br.com.moreiracruz.erp.modules.sales.domain.port.out.VendaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class VendaRepositoryAdapter implements VendaRepository {

    private final VendaJpaRepository vendaJpaRepository;

    public VendaRepositoryAdapter(VendaJpaRepository vendaJpaRepository) {
        this.vendaJpaRepository = vendaJpaRepository;
    }

    @Override
    public Optional<Venda> findByUuid(UUID uuid) {
        return vendaJpaRepository.findByUuid(uuid).map(this::toDomain);
    }

    @Override
    public Venda save(Venda venda) {
        VendaJpaEntity entity = toJpaEntity(venda);
        entity = vendaJpaRepository.save(entity);
        return toDomain(entity);
    }

    // --- Mapping: JPA → Domain ---

    private Venda toDomain(VendaJpaEntity entity) {
        List<ItemVenda> items = entity.getItems().stream()
                .map(this::toDomainItem)
                .toList();

        return Venda.restore(
                entity.getId(),
                entity.getUuid(),
                entity.getOperatorUuid(),
                entity.getTerminalId(),
                entity.getClienteUuid(),
                VendaStatus.valueOf(entity.getStatus()),
                entity.getPaymentMethod() != null ? PaymentMethod.valueOf(entity.getPaymentMethod()) : null,
                entity.getSubtotal(),
                entity.getDiscountAmount(),
                entity.getTaxAmount(),
                entity.getTotal(),
                entity.getChangeAmount(),
                entity.getCouponCode(),
                entity.getCancellationReason(),
                entity.getCreatedAt(),
                entity.getFinalizedAt(),
                items
        );
    }

    private ItemVenda toDomainItem(ItemVendaJpaEntity entity) {
        return ItemVenda.restore(
                entity.getId(),
                entity.getVenda().getId(),
                entity.getVarianteUuid(),
                entity.getSku(),
                entity.getQuantity(),
                entity.getUnitPrice(),
                entity.getLineTotal()
        );
    }

    // --- Mapping: Domain → JPA ---

    private VendaJpaEntity toJpaEntity(Venda venda) {
        VendaJpaEntity entity = new VendaJpaEntity();
        entity.setId(venda.getId());
        entity.setUuid(venda.getUuid());
        entity.setOperatorUuid(venda.getOperatorUuid());
        entity.setTerminalId(venda.getTerminalId());
        entity.setClienteUuid(venda.getClienteUuid());
        entity.setStatus(venda.getStatus().name());
        entity.setPaymentMethod(venda.getPaymentMethod() != null ? venda.getPaymentMethod().name() : null);
        entity.setSubtotal(venda.getSubtotal());
        entity.setDiscountAmount(venda.getDiscountAmount());
        entity.setTaxAmount(venda.getTaxAmount());
        entity.setTotal(venda.getTotal());
        entity.setChangeAmount(venda.getChangeAmount());
        entity.setCouponCode(venda.getCouponCode());
        entity.setCancellationReason(venda.getCancellationReason());
        entity.setCreatedAt(venda.getCreatedAt());
        entity.setFinalizedAt(venda.getFinalizedAt());

        // Map items
        List<ItemVendaJpaEntity> jpaItems = venda.getItems().stream()
                .map(item -> toJpaItemEntity(item, entity))
                .toList();
        entity.getItems().clear();
        entity.getItems().addAll(jpaItems);

        return entity;
    }

    private ItemVendaJpaEntity toJpaItemEntity(ItemVenda item, VendaJpaEntity vendaEntity) {
        ItemVendaJpaEntity entity = new ItemVendaJpaEntity();
        entity.setId(item.getId());
        entity.setVenda(vendaEntity);
        entity.setVarianteUuid(item.getVarianteUuid());
        entity.setSku(item.getSku());
        entity.setQuantity(item.getQuantity());
        entity.setUnitPrice(item.getUnitPrice());
        entity.setLineTotal(item.getLineTotal());
        return entity;
    }
}
