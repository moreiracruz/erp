package com.erp.modules.product.application.usecase;

import com.erp.modules.product.domain.model.Produto;
import com.erp.modules.product.domain.port.in.ProdutoResponse;
import com.erp.modules.product.domain.port.in.UpdateProductCommand;
import com.erp.modules.product.domain.port.in.UpdateProductUseCase;
import com.erp.modules.product.domain.port.out.ProdutoRepository;
import com.erp.shared.exceptions.ConflictException;
import com.erp.shared.exceptions.NotFoundException;
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
