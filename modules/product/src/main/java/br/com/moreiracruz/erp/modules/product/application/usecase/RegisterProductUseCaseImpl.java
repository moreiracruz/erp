package br.com.moreiracruz.erp.modules.product.application.usecase;

import br.com.moreiracruz.erp.modules.product.domain.model.Produto;
import br.com.moreiracruz.erp.modules.product.domain.port.in.ProdutoResponse;
import br.com.moreiracruz.erp.modules.product.domain.port.in.RegisterProductCommand;
import br.com.moreiracruz.erp.modules.product.domain.port.in.RegisterProductUseCase;
import br.com.moreiracruz.erp.modules.product.domain.port.out.ProdutoRepository;
import br.com.moreiracruz.erp.shared.exceptions.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case implementation for registering a new product in the catalog.
 */
@Service
@Transactional
public class RegisterProductUseCaseImpl implements RegisterProductUseCase {

    private final ProdutoRepository produtoRepository;

    public RegisterProductUseCaseImpl(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Override
    public ProdutoResponse register(RegisterProductCommand cmd) {
        if (produtoRepository.existsByNameIgnoreCaseAndActiveTrue(cmd.name())) {
            throw new ConflictException("Produto com este nome já existe");
        }
        Produto produto = Produto.create(cmd.name(), cmd.brand(), cmd.category());
        Produto saved = produtoRepository.save(produto);
        return toResponse(saved);
    }

    // ── Mapper ───────────────────────────────────────────────────────────────

    static ProdutoResponse toResponse(Produto p) {
        return new ProdutoResponse(p.getUuid(), p.getName(), p.getBrand(), p.getCategory(), p.isActive());
    }
}
