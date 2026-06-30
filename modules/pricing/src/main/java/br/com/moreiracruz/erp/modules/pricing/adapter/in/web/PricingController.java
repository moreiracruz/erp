package br.com.moreiracruz.erp.modules.pricing.adapter.in.web;

import br.com.moreiracruz.erp.modules.pricing.domain.model.Campanha;
import br.com.moreiracruz.erp.modules.pricing.domain.port.in.CalculateDiscountUseCase;
import br.com.moreiracruz.erp.modules.pricing.domain.port.in.CampanhaResponse;
import br.com.moreiracruz.erp.modules.pricing.domain.port.in.ConfirmCouponUsageUseCase;
import br.com.moreiracruz.erp.modules.pricing.domain.port.in.CreateCampaignCommand;
import br.com.moreiracruz.erp.modules.pricing.domain.port.in.CreateCampaignUseCase;
import br.com.moreiracruz.erp.modules.pricing.domain.port.in.CreateCouponCommand;
import br.com.moreiracruz.erp.modules.pricing.domain.port.in.CreateCouponUseCase;
import br.com.moreiracruz.erp.modules.pricing.domain.port.in.CupomResponse;
import br.com.moreiracruz.erp.modules.pricing.domain.port.in.DiscountQuery;
import br.com.moreiracruz.erp.modules.pricing.domain.port.in.DiscountResult;
import br.com.moreiracruz.erp.modules.pricing.domain.port.out.CampanhaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST adapter exposing pricing operations under {@code /api/v1/pricing}.
 */
@RestController
@RequestMapping("/api/v1/pricing")
public class PricingController {

    private final CreateCampaignUseCase createCampaignUseCase;
    private final CreateCouponUseCase createCouponUseCase;
    private final CalculateDiscountUseCase calculateDiscountUseCase;
    private final ConfirmCouponUsageUseCase confirmCouponUsageUseCase;
    private final CampanhaRepository campanhaRepository;

    public PricingController(CreateCampaignUseCase createCampaignUseCase,
                             CreateCouponUseCase createCouponUseCase,
                             CalculateDiscountUseCase calculateDiscountUseCase,
                             ConfirmCouponUsageUseCase confirmCouponUsageUseCase,
                             CampanhaRepository campanhaRepository) {
        this.createCampaignUseCase = createCampaignUseCase;
        this.createCouponUseCase = createCouponUseCase;
        this.calculateDiscountUseCase = calculateDiscountUseCase;
        this.confirmCouponUsageUseCase = confirmCouponUsageUseCase;
        this.campanhaRepository = campanhaRepository;
    }

    @PostMapping("/campaigns")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<CampanhaResponse> createCampaign(@RequestBody CreateCampaignCommand cmd) {
        CampanhaResponse response = createCampaignUseCase.create(cmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/campaigns")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<List<CampanhaResponse>> listActiveCampaigns() {
        List<CampanhaResponse> campaigns = campanhaRepository.findAllActive().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(campaigns);
    }

    @PutMapping("/campaigns/{uuid}/deactivate")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<Void> deactivateCampaign(@PathVariable UUID uuid) {
        Campanha campanha = campanhaRepository.findByUuid(uuid)
                .orElseThrow(() -> new br.com.moreiracruz.erp.shared.exceptions.NotFoundException(
                        "Campanha não encontrada: " + uuid));
        campanha.deactivate();
        campanhaRepository.save(campanha);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/coupons")
    @PreAuthorize("hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<CupomResponse> createCoupon(@RequestBody CreateCouponCommand cmd) {
        CupomResponse response = createCouponUseCase.create(cmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/calculate")
    @PreAuthorize("hasAuthority('ROLE_CASHIER') or hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<DiscountResult> calculateDiscount(@RequestBody DiscountQuery query) {
        DiscountResult result = calculateDiscountUseCase.calculate(query);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/coupons/{code}/confirm")
    @PreAuthorize("hasAuthority('ROLE_CASHIER') or hasAuthority('ROLE_MANAGER')")
    public ResponseEntity<Void> confirmCouponUsage(@PathVariable String code) {
        confirmCouponUsageUseCase.confirm(code);
        return ResponseEntity.noContent().build();
    }

    private CampanhaResponse toResponse(Campanha c) {
        return new CampanhaResponse(
                c.getUuid(), c.getName(), c.getType().name(), c.getTargetType().name(),
                c.getTargetUuid(), c.getTargetCategory(), c.getDiscountValue(),
                c.getCashbackPct(), c.getStartsAt(), c.getEndsAt(), c.isActive());
    }
}
