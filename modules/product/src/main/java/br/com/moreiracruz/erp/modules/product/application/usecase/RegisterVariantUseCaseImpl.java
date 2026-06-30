package br.com.moreiracruz.erp.modules.product.application.usecase;

import br.com.moreiracruz.erp.modules.product.domain.model.Produto;
import br.com.moreiracruz.erp.modules.product.domain.model.VarianteProduto;
import br.com.moreiracruz.erp.modules.product.domain.port.in.RegisterVariantCommand;
import br.com.moreiracruz.erp.modules.product.domain.port.in.RegisterVariantUseCase;
import br.com.moreiracruz.erp.modules.product.domain.port.in.VarianteResponse;
import br.com.moreiracruz.erp.modules.product.domain.port.out.ProdutoRepository;
import br.com.moreiracruz.erp.modules.product.domain.port.out.VarianteRepository;
import br.com.moreiracruz.erp.shared.exceptions.ConflictException;
import br.com.moreiracruz.erp.shared.exceptions.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case implementation for registering a new variant under an existing product.
 */
@Service
@Transactional
public class RegisterVariantUseCaseImpl implements RegisterVariantUseCase {

    private final ProdutoRepository produtoRepository;
    private final VarianteRepository varianteRepository;

    public RegisterVariantUseCaseImpl(ProdutoRepository produtoRepository,
                                      VarianteRepository varianteRepository) {
        this.produtoRepository = produtoRepository;
        this.varianteRepository = varianteRepository;
    }

    @Override
    public VarianteResponse register(UUID produtoUuid, RegisterVariantCommand cmd) {
        Produto produto = produtoRepository.findByUuid(produtoUuid)
                .filter(Produto::isActive)
                .orElseThrow(() -> new ValidationException("Produto inexistente ou inativo"));

        if (varianteRepository.existsBySku(cmd.sku())) {
            throw new ConflictException("SKU já cadastrado");
        }
        if (varianteRepository.existsByBarcode(cmd.barcode())) {
            throw new ConflictException("Código de barras já cadastrado");
        }

        VarianteProduto variante = VarianteProduto.create(
                produto.getId(),
                produto.getUuid(),
                cmd.sku(),
                cmd.size(),
                cmd.color(),
                cmd.barcode(),
                cmd.price(),
                cmd.cost()
        );
        VarianteProduto saved = varianteRepository.save(variante);
        return toResponse(saved);
    }

    // ── Mapper ───────────────────────────────────────────────────────────────

    static VarianteResponse toResponse(VarianteProduto v) {
        return new VarianteResponse(
                v.getUuid(),
                v.getProdutoUuid(),
                v.getSku().value(),
                v.getSize(),
                v.getColor(),
                v.getBarcode().value(),
                v.getPrice().amount(),
                v.getCost().amount(),
                v.isActive()
        );
    }
}
