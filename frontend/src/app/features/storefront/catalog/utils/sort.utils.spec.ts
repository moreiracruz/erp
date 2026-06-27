// Feature: storefront-catalog, Property 5: Sort ordering invariant
import { describe, it, expect } from 'vitest';
import * as fc from 'fast-check';
import { ProductSummary } from '../../../../core/models';
import { SortOption } from '../models';
import { sortProducts } from './sort.utils';

/**
 * Validates: Requirements 3.2
 *
 * Property 5: Sort ordering invariant
 * For any non-empty product list and sort option, the sorted output must satisfy
 * the adjacent pair ordering predicate for all consecutive pairs.
 */

const sortOptionArb: fc.Arbitrary<SortOption> = fc.constantFrom(
  'newest',
  'price-asc',
  'price-desc',
  'popularity'
);

const productSummaryArb: fc.Arbitrary<ProductSummary> = fc.record({
  uuid: fc.uuid(),
  name: fc.string({ minLength: 1, maxLength: 50 }),
  brand: fc.string({ minLength: 1, maxLength: 30 }),
  category: fc.string({ minLength: 1, maxLength: 30 }),
  imageUrl: fc.option(fc.webUrl(), { nil: undefined }),
  minPrice: fc.float({ min: Math.fround(0.01), max: Math.fround(10000), noNaN: true, noDefaultInfinity: true }),
  maxPrice: fc.float({ min: Math.fround(0.01), max: Math.fround(10000), noNaN: true, noDefaultInfinity: true }),
});

const nonEmptyProductListArb: fc.Arbitrary<ProductSummary[]> = fc.array(productSummaryArb, {
  minLength: 1,
  maxLength: 30,
});

describe('sortProducts - Property 5: Sort ordering invariant', () => {
  it('should maintain adjacent pair ordering for all sort options', () => {
    fc.assert(
      fc.property(nonEmptyProductListArb, sortOptionArb, (products, option) => {
        const sorted = sortProducts(products, option);

        for (let i = 0; i < sorted.length - 1; i++) {
          switch (option) {
            case 'price-asc':
              expect(sorted[i].minPrice).toBeLessThanOrEqual(sorted[i + 1].minPrice);
              break;
            case 'price-desc':
              expect(sorted[i].minPrice).toBeGreaterThanOrEqual(sorted[i + 1].minPrice);
              break;
            case 'newest':
            case 'popularity':
              // These options preserve original order (no re-sorting)
              expect(sorted[i]).toBe(products[i]);
              break;
          }
        }
      }),
      { numRuns: 100 }
    );
  });
});
