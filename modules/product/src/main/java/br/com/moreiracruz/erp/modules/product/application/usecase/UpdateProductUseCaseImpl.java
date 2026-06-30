package br.com.moreiracruz.erp.modules.product.application.usecase;

import br.com.moreiracruz.erp.modules.product.domain.model.Produto;
import br.com.moreiracruz.erp.modules.product.domain.port.in.ProdutoResponse;
import br.com.moreiracruz.erp.modules.product.domain.port.in.UpdateProductCommand;
import br.com.moreiracruz.erp.modules.product.domain.port.in.UpdateProductUseCase;
import br.com.moreiracruz.erp.modules.product.domain.port.out.ProdutoRepository;
import br.com.moreiracruz.erp.shared.exceptions.ConflictException;
import br.com.moreiracruz.erp.shared.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Use case implementation for updating an existing product's attributes.
 */
@Service
@Transactional
public class UpdateProductUseCaseImpl implements UpdateProductUseCase {

    private final ProdutoRepository produtoRepository;

    public UpdateProductUseCaseImpl(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Override
    public ProdutoResponse update(UUID uuid, UpdateProductCommand cmd) {
        Produto produto = produtoRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException("Produto não encontrado"));

        String existingName = produto.getName();
        if (!existingName.equalsIgnoreCase(cmd.name())
                && produtoRepository.existsByNameIgnoreCaseAndActiveTrue(cmd.name())) {
            throw new ConflictException("Produto com este nome já existe");
        }

        produto.update(cmd.name(), cmd.brand(), cmd.category());
        Produto saved = produtoRepository.save(produto);
        return RegisterProductUseCaseImpl.toResponse(saved);
    }
}
