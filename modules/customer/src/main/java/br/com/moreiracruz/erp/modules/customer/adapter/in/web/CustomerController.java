package br.com.moreiracruz.erp.modules.customer.adapter.in.web;

import br.com.moreiracruz.erp.modules.customer.domain.port.in.ClienteResponse;
import br.com.moreiracruz.erp.modules.customer.domain.port.in.CustomerSearchQuery;
import br.com.moreiracruz.erp.modules.customer.domain.port.in.DeactivateCustomerUseCase;
import br.com.moreiracruz.erp.modules.customer.domain.port.in.RegisterCustomerCommand;
import br.com.moreiracruz.erp.modules.customer.domain.port.in.RegisterCustomerUseCase;
import br.com.moreiracruz.erp.modules.customer.domain.port.in.SearchCustomerUseCase;
import br.com.moreiracruz.erp.modules.customer.domain.port.out.ClienteRepository;
import br.com.moreiracruz.erp.shared.exceptions.NotFoundException;
import br.com.moreiracruz.erp.shared.kernel.pagination.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST adapter exposing customer operations under {@code /api/v1/customers}.
 */
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final RegisterCustomerUseCase registerCustomerUseCase;
    private final DeactivateCustomerUseCase deactivateCustomerUseCase;
    private final SearchCustomerUseCase searchCustomerUseCase;
    private final ClienteRepository clienteRepository;

    public CustomerController(RegisterCustomerUseCase registerCustomerUseCase,
                               DeactivateCustomerUseCase deactivateCustomerUseCase,
                               SearchCustomerUseCase searchCustomerUseCase,
                               ClienteRepository clienteRepository) {
        this.registerCustomerUseCase = registerCustomerUseCase;
        this.deactivateCustomerUseCase = deactivateCustomerUseCase;
        this.searchCustomerUseCase = searchCustomerUseCase;
        this.clienteRepository = clienteRepository;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_CASHIER')")
    public ResponseEntity<ClienteResponse> register(@RequestBody RegisterCustomerCommand cmd) {
        ClienteResponse response = registerCustomerUseCase.register(cmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_CASHIER')")
    public ResponseEntity<ClienteResponse> findByUuid(@PathVariable UUID uuid) {
        var cliente = clienteRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado"));
        return ResponseEntity.ok(toResponse(cliente));
    }

    @PutMapping("/{uuid}")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<ClienteResponse> update(@PathVariable UUID uuid,
                                                   @RequestBody RegisterCustomerCommand cmd) {
        // Find existing customer, recreate with new data
        var existing = clienteRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException("Cliente não encontrado"));

        var updated = br.com.moreiracruz.erp.modules.customer.domain.model.Cliente.create(
                cmd.fullName(),
                cmd.cpf(),
                cmd.email(),
                cmd.phone(),
                cmd.birthDate()
        );
        // Preserve the UUID by restoring
        var restored = br.com.moreiracruz.erp.modules.customer.domain.model.Cliente.restore(
                uuid,
                cmd.fullName(),
                cmd.cpf(),
                cmd.email(),
                cmd.phone(),
                cmd.birthDate(),
                existing.isActive(),
                existing.getCreatedAt()
        );
        var saved = clienteRepository.save(restored);
        return ResponseEntity.ok(toResponse(saved));
    }

    @DeleteMapping("/{uuid}/deactivate")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<Void> deactivate(@PathVariable UUID uuid) {
        deactivateCustomerUseCase.deactivate(uuid);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('ROLE_MANAGER') or hasAuthority('ROLE_CASHIER')")
    public ResponseEntity<Page<ClienteResponse>> search(
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) UUID uuid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var query = new CustomerSearchQuery(cpf, name, uuid, page, size);
        return ResponseEntity.ok(toSpringPage(searchCustomerUseCase.search(query)));
    }

    private Page<ClienteResponse> toSpringPage(PageResult<ClienteResponse> result) {
        return new PageImpl<>(
                result.content(),
                PageRequest.of(result.page(), result.size()),
                result.totalElements());
    }

    private ClienteResponse toResponse(br.com.moreiracruz.erp.modules.customer.domain.model.Cliente c) {
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
