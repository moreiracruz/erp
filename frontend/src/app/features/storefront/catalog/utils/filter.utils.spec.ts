// Feature: storefront-catalog, Property 3: Filter counts accuracy
import * as fc from 'fast-check';
import { computeFilterCounts, filterProducts } from './filter.utils';
import { DEFAULT_FILTERS, FilterState } from '../models';
import { ProductSummary } from '../../../../core/models';

/**
 * Validates: Requirements 2.5
 *
 * Property 3: Filter counts accuracy
 * For any product list and any current active filter state, the count displayed next to each
 * category filter option SHALL equal the number of products that would appear if that category
 * were the only active category (with all other filters unchanged).
 */
describe('Property 3: Filter counts accuracy', () => {
  const CATEGORIES = ['Vestidos', 'Camisetas', 'Calças', 'Saias', 'Acessórios'];

  const arbProduct: fc.Arbitrary<ProductSummary> = fc.record({
    uuid: fc.uuid(),
    name: fc.string({ minLength: 1, maxLength: 30 }),
    brand: fc.string({ minLength: 1, maxLength: 20 }),
    category: fc.constantFrom(...CATEGORIES),
    imageUrl: fc.constant(undefined),
    minPrice: fc.float({ min: 1, max: 500, noNaN: true }),
    maxPrice: fc.float({ min: 500, max: 1000, noNaN: true }),
  });

  const arbPriceRange: fc.Arbitrary<{ min: number; max: number } | null> = fc.oneof(
    fc.constant(null),
    fc.record({
      min: fc.float({ min: 0, max: 400, noNaN: true }),
      max: fc.float({ min: 400, max: 1200, noNaN: true }),
    })
  );

  const arbFilterState: fc.Arbitrary<FilterState> = fc.record({
    categories: fc.subarray(CATEGORIES, { minLength: 0, maxLength: CATEGORIES.length }),
    sizes: fc.constant([]),
    colors: fc.constant([]),
    priceRange: arbPriceRange,
  });

  it('category counts equal products matching all other filters per category', () => {
    fc.assert(
      fc.property(
        fc.array(arbProduct, { minLength: 0, maxLength: 30 }),
        arbFilterState,
        (products, currentFilters) => {
          const counts = computeFilterCounts(products, currentFilters);

          // For each category that appears in the counts, verify
          // the count equals the number of products with that category
          // that pass all filters EXCEPT category.
          const filtersWithoutCategory: FilterState = { ...currentFilters, categories: [] };
          const matchingAllOtherFilters = filterProducts(products, filtersWithoutCategory);

          // Build expected counts from products that match other filters
          const expectedCounts: Record<string, number> = {};
          for (const product of matchingAllOtherFilters) {
            expectedCounts[product.category] = (expectedCounts[product.category] ?? 0) + 1;
          }

          // Every category in computed counts must match expected
          for (const [category, count] of Object.entries(counts.categories)) {
            expect(count).toBe(expectedCounts[category] ?? 0);
          }

          // Every category with products matching other filters must appear in counts
          for (const [category, expectedCount] of Object.entries(expectedCounts)) {
            expect(counts.categories[category]).toBe(expectedCount);
          }
        }
      ),
      { numRuns: 100 }
    );
  });
});

// Feature: storefront-catalog, Property 4: Clear filters idempotence
describe('Property 4: Clear filters idempotence', () => {
  /**
   * **Validates: Requirements 2.6**
   *
   * Applying DEFAULT_FILTERS (no categories, no price range) should return
   * the complete unfiltered list in original order.
   */

  const arbProductSummary: fc.Arbitrary<ProductSummary> = fc.record({
    uuid: fc.uuid(),
    name: fc.string({ minLength: 1, maxLength: 50 }),
    brand: fc.string({ minLength: 1, maxLength: 30 }),
    category: fc.string({ minLength: 1, maxLength: 20 }),
    imageUrl: fc.constant(undefined),
    minPrice: fc.float({ min: 1, max: 500, noNaN: true }),
    maxPrice: fc.float({ min: 500, max: 1000, noNaN: true }),
  });

  const arbProductList: fc.Arbitrary<ProductSummary[]> = fc.array(arbProductSummary, {
    minLength: 0,
    maxLength: 50,
  });

  it('should return the complete unfiltered list in original order when DEFAULT_FILTERS applied', () => {
    fc.assert(
      fc.property(arbProductList, (products) => {
        const result = filterProducts(products, DEFAULT_FILTERS);

        // Same length
        expect(result.length).toBe(products.length);

        // Same items in same order
        expect(result).toEqual(products);
      }),
      { numRuns: 100 }
    );
  });
});
