package br.com.moreiracruz.erp.shared.exceptions;

/**
 * Thrown when a stock operation cannot be fulfilled due to insufficient available quantity.
 */
public class InsufficientStockException extends BusinessException {

    private final int availableStock;

    public InsufficientStockException(int availableStock) {
        super("Estoque insuficiente. Disponível: " + availableStock);
        this.availableStock = availableStock;
    }

    public int getAvailableStock() {
        return availableStock;
    }
}
