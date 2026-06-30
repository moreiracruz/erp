package br.com.moreiracruz.erp.shared.kernel.pagination;

/**
 * Framework-neutral pagination request used by domain ports.
 */
public record PageQuery(int page, int size) {

    public PageQuery {
        if (page < 0) {
            throw new IllegalArgumentException("page must be >= 0");
        }
        if (size < 1) {
            throw new IllegalArgumentException("size must be >= 1");
        }
    }
}
