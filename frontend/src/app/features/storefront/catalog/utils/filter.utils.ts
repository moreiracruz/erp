import { ProductSummary } from '../../../../core/models';
import { FilterState, DEFAULT_FILTERS, FilterCounts } from '../models';

/**
 * Filters products using AND semantics: a product must match ALL active filter criteria.
 *
 * Supported filters on ProductSummary:
 * - categories: product.category must be included in filters.categories (if non-empty)
 * - priceRange: product's price range [minPrice, maxPrice] must overlap with filter range
 *
 * Note: size/color filtering requires full Product[] with variants and is not supported
 * at the ProductSummary level.
 */
export function filterProducts(products: ProductSummary[], filters: FilterState): ProductSummary[] {
  return products.filter((product) => {
    // Category filter (AND with other criteria)
    if (filters.categories.length > 0 && !filters.categories.includes(product.category)) {
      return false;
    }

    // Price range filter (overlap check)
    if (filters.priceRange !== null) {
      const { min, max } = filters.priceRange;
      // Product's price band [minPrice, maxPrice] must overlap with filter [min, max]
      if (product.maxPrice < min || product.minPrice > max) {
        return false;
      }
    }

    return true;
  });
}

/**
 * Computes the number of products that would match if each individual filter value
 * were toggled on/off relative to the current filter state.
 *
 * For categories: counts products per category that match all OTHER current filters.
 */
export function computeFilterCounts(products: ProductSummary[], currentFilters: FilterState): FilterCounts {
  const categories: Record<string, number> = {};

  // For each product, check if it matches all filters EXCEPT category,
  // then increment the count for its category.
  const filtersWithoutCategory: FilterState = { ...currentFilters, categories: [] };

  for (const product of products) {
    const matchesOtherFilters = filterProducts([product], filtersWithoutCategory).length > 0;
    if (matchesOtherFilters) {
      categories[product.category] = (categories[product.category] ?? 0) + 1;
    }
  }

  // Sizes and colors cannot be computed from ProductSummary (requires variants).
  // Return empty records for now.
  const sizes: Record<string, number> = {};
  const colors: Record<string, number> = {};

  return { categories, sizes, colors };
}
