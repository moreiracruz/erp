package com.erp.modules.product.application.usecase;

import com.erp.modules.product.domain.model.Produto;
import com.erp.modules.product.domain.model.VarianteProduto;
import com.erp.modules.product.domain.port.in.DeactivateProductUseCase;
import com.erp.modules.product.domain.port.out.ProdutoRepository;
import com.erp.modules.product.domain.port.out.VarianteRepository;
import com.erp.shared.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Use case implementation for deactivating a product and all its variants
 * within a single transaction.
 */
@Service
@Transactional
public class DeactivateProductUseCaseImpl implements DeactivateProductUseCase {

    private final ProdutoRepository produtoRepository;
    private final VarianteRepository varianteRepository;

    public DeactivateProductUseCaseImpl(ProdutoRepository produtoRepository,
                                        VarianteRepository varianteRepository) {
        this.produtoRepository = produtoRepository;
        this.varianteRepository = varianteRepository;
    }

    @Override
    public void deactivate(UUID uuid) {
        Produto produto = produtoRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException("Produto não encontrado"));

        produto.deactivate();

        List<VarianteProduto> variantes = varianteRepository.findByProdutoId(produto.getId());
        for (VarianteProduto variante : variantes) {
            variante.deactivate();
            varianteRepository.save(variante);
        }

        produtoRepository.save(produto);
    }
}
