package br.com.moreiracruz.erp.shared.kernel.pagination;

import java.util.List;
import java.util.function.Function;

/**
 * Framework-neutral paginated result used by domain ports and use cases.
 */
public record PageResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements
) {

    public PageResult {
        content = List.copyOf(content);
        if (page < 0) {
            throw new IllegalArgumentException("page must be >= 0");
        }
        if (size < 1) {
            throw new IllegalArgumentException("size must be >= 1");
        }
        if (totalElements < 0) {
            throw new IllegalArgumentException("totalElements must be >= 0");
        }
    }

    public static <T> PageResult<T> empty(PageQuery query) {
        return new PageResult<>(List.of(), query.page(), query.size(), 0);
    }

    public static <T> PageResult<T> single(T item, PageQuery query) {
        return new PageResult<>(List.of(item), query.page(), query.size(), 1);
    }

    public <R> PageResult<R> map(Function<T, R> mapper) {
        return new PageResult<>(
                content.stream().map(mapper).toList(),
                page,
                size,
                totalElements);
    }
}
