package com.erp.modules.inventory.adapter.in.web;

import com.erp.modules.inventory.domain.model.MovimentoEstoque;
import com.erp.modules.inventory.domain.port.in.GetStockUseCase;
import com.erp.modules.inventory.domain.port.in.MovimentoResponse;
import com.erp.modules.inventory.domain.port.in.RegisterEntryUseCase;
import com.erp.modules.inventory.domain.port.in.RegisterWithdrawalUseCase;
import com.erp.modules.inventory.domain.port.in.StockEntryCommand;
import com.erp.modules.inventory.domain.port.in.StockResponse;
import com.erp.modules.inventory.domain.port.in.StockWithdrawalCommand;
import com.erp.modules.inventory.domain.port.out.MovimentoEstoqueRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST adapter exposing inventory operations under {@code /api/v1/inventory}.
 *
 * <p>Requires callers to hold either {@code ROLE_MANAGER} or {@code ROLE_STOCK}.
 */
@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final RegisterEntryUseCase registerEntryUseCase;
    private final RegisterWithdrawalUseCase registerWithdrawalUseCase;
    private final GetStockUseCase getStockUseCase;
    private final MovimentoEstoqueRepository movimentoRepository;

    public InventoryController(RegisterEntryUseCase registerEntryUseCase,
                                RegisterWithdrawalUseCase registerWithdrawalUseCase,
                                GetStockUseCase getStockUseCase,
                                MovimentoEstoqueRepository movimentoRepository) {
        this.registerEntryUseCase = registerEntryUseCase;
        this.registerWithdrawalUseCase = registerWithdrawalUseCase;
        this.getStockUseCase = getStockUseCase;
        this.movimentoRepository = movimentoRepository;
    }

    /**
     * Returns the current stock position for a product variant.
     *
     * @param uuid the variant's UUID
     */
    @GetMapping("/variants/{uuid}/stock")
    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_STOCK')")
    public ResponseEntity<StockResponse> getStock(@PathVariable UUID uuid) {
        return ResponseEntity.ok(getStockUseCase.getStock(uuid));
    }

    /**
     * Registers a stock entry (goods received) for a product variant.
     *
     * @param uuid    the variant's UUID
     * @param request quantity and actor
     */
    @PostMapping("/variants/{uuid}/entries")
    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_STOCK')")
    public ResponseEntity<Void> registerEntry(@PathVariable UUID uuid,
                                               @RequestBody StockOperationRequest request) {
        registerEntryUseCase.registerEntry(
                new StockEntryCommand(uuid, request.quantity(), request.actorUuid()));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Registers a stock withdrawal (goods dispatched) for a product variant.
     *
     * @param uuid    the variant's UUID
     * @param request quantity and actor
     */
    @PostMapping("/variants/{uuid}/withdrawals")
    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_STOCK')")
    public ResponseEntity<Void> registerWithdrawal(@PathVariable UUID uuid,
                                                    @RequestBody StockOperationRequest request) {
        registerWithdrawalUseCase.registerWithdrawal(
                new StockWithdrawalCommand(uuid, request.quantity(), request.actorUuid()));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Returns the full movement history for a product variant.
     *
     * @param uuid the variant's UUID
     */
    @GetMapping("/variants/{uuid}/movements")
    @PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_STOCK')")
    public ResponseEntity<List<MovimentoResponse>> getMovements(@PathVariable UUID uuid) {
        List<MovimentoEstoque> movements = movimentoRepository.findByVarianteUuid(uuid);
        List<MovimentoResponse> response = movements.stream()
                .map(m -> new MovimentoResponse(
                        m.getUuid(),
                        m.getOperationType().name(),
                        m.getQuantity(),
                        m.getOccurredAt(),
                        m.getActorUuid()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
