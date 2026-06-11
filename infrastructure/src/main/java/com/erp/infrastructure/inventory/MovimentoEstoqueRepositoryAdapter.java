package com.erp.infrastructure.inventory;

import com.erp.modules.inventory.domain.model.MovimentoEstoque;
import com.erp.modules.inventory.domain.model.OperationType;
import com.erp.modules.inventory.domain.port.out.MovimentoEstoqueRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter implementing the domain's {@link MovimentoEstoqueRepository} outbound port
 * using Spring Data JPA.
 *
 * <p>The {@link OperationType} enum uses ASCII Java identifiers; the database column
 * stores the Portuguese string forms. Mapping is done explicitly here to avoid
 * coupling the domain model to persistence details.
 */
@Repository
public class MovimentoEstoqueRepositoryAdapter implements MovimentoEstoqueRepository {

    private final MovimentoEstoqueJpaRepository jpaRepository;

    public MovimentoEstoqueRepositoryAdapter(MovimentoEstoqueJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(MovimentoEstoque movimento) {
        jpaRepository.save(toEntity(movimento));
    }

    @Override
    public List<MovimentoEstoque> findByVarianteUuid(UUID varianteUuid) {
        return jpaRepository.findByVarianteUuid(varianteUuid).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Mapping helpers
    // -------------------------------------------------------------------------

    private MovimentoEstoque toDomain(MovimentoEstoqueJpaEntity e) {
        return MovimentoEstoque.restore(
                e.getId(),
                e.getUuid(),
                e.getVarianteUuid(),
                operationTypeFromDb(e.getOperationType()),
                e.getQuantity(),
                e.getOccurredAt(),
                e.getActorUuid(),
                e.getReferenceUuid());
    }

    private MovimentoEstoqueJpaEntity toEntity(MovimentoEstoque m) {
        MovimentoEstoqueJpaEntity e = new MovimentoEstoqueJpaEntity();
        e.setId(m.getId());
        e.setUuid(m.getUuid());
        e.setVarianteUuid(m.getVarianteUuid());
        e.setOperationType(operationTypeToDb(m.getOperationType()));
        e.setQuantity(m.getQuantity());
        e.setOccurredAt(m.getOccurredAt());
        e.setActorUuid(m.getActorUuid());
        e.setReferenceUuid(m.getReferenceUuid());
        return e;
    }

    /**
     * Maps the Java enum to its Portuguese database representation.
     */
    private String operationTypeToDb(OperationType type) {
        return switch (type) {
            case ENTRADA -> "ENTRADA";
            case SAIDA -> "SAÍDA";
            case RESERVA -> "RESERVA";
            case LIBERACAO_RESERVA -> "LIBERAÇÃO_RESERVA";
        };
    }

    /**
     * Maps the Portuguese database string back to the Java enum.
     */
    private OperationType operationTypeFromDb(String dbValue) {
        return switch (dbValue) {
            case "ENTRADA" -> OperationType.ENTRADA;
            case "SAÍDA" -> OperationType.SAIDA;
            case "RESERVA" -> OperationType.RESERVA;
            case "LIBERAÇÃO_RESERVA" -> OperationType.LIBERACAO_RESERVA;
            default -> throw new IllegalArgumentException("Unknown operation type in DB: " + dbValue);
        };
    }
}
