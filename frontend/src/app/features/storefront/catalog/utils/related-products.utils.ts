import { ProductSummary } from '../../../../core/models';

/**
 * Returns related products from the same category, excluding the current product.
 * Limited to `limit` items (default 4).
 */
export function getRelatedProducts(
  catalog: ProductSummary[],
  currentProductUuid: string,
  currentCategory: string,
  limit: number = 4,
): ProductSummary[] {
  return catalog
    .filter((p) => p.category === currentCategory && p.uuid !== currentProductUuid)
    .slice(0, limit);
}
