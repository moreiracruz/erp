import { ProductSummary } from '../../../../core/models';
import { SortOption } from '../models';

/**
 * Returns a new sorted array of products based on the specified sort option.
 * Uses a stable sort (spread + sort) to preserve original order for equal elements.
 *
 * - 'newest': preserves original API order (treated as newest-first from backend)
 * - 'price-asc': sort by minPrice ascending
 * - 'price-desc': sort by minPrice descending
 * - 'popularity': preserves original order (placeholder until popularity metric exists)
 */
export function sortProducts(products: ProductSummary[], option: SortOption): ProductSummary[] {
  const sorted = [...products];

  switch (option) {
    case 'price-asc':
      sorted.sort((a, b) => a.minPrice - b.minPrice);
      break;
    case 'price-desc':
      sorted.sort((a, b) => b.minPrice - a.minPrice);
      break;
    case 'newest':
      // Original API order is treated as newest-first; no re-sorting needed.
      break;
    case 'popularity':
      // Placeholder: maintain original order until a popularity metric is available.
      break;
  }

  return sorted;
}
