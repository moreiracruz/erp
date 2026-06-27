// Feature: storefront-catalog, Property 2: Query params round-trip
import * as fc from 'fast-check';

import { FilterState, SortOption } from '../models';
import { serializeFilters, deserializeFilters } from './query-params.utils';

/**
 * Arbitrary generator for a non-empty string that does not contain commas or empty strings.
 * Commas are used as delimiters in serialization, so individual values must not contain them.
 */
function arbFilterValue(): fc.Arbitrary<string> {
  return fc.string({ minLength: 1, maxLength: 20 }).filter(
    (s) => !s.includes(',') && s.trim().length > 0
  );
}

/**
 * Arbitrary generator for a price range where min <= max using finite positive numbers.
 */
function arbPriceRange(): fc.Arbitrary<{ min: number; max: number } | null> {
  return fc.oneof(
    fc.constant(null),
    fc
      .tuple(
        fc.double({ min: 0, max: 100000, noNaN: true, noDefaultInfinity: true }),
        fc.double({ min: 0, max: 100000, noNaN: true, noDefaultInfinity: true })
      )
      .map(([a, b]) => (a <= b ? { min: a, max: b } : { min: b, max: a }))
  );
}

/**
 * Arbitrary generator for FilterState with valid values.
 */
function arbFilterState(): fc.Arbitrary<FilterState> {
  return fc.record({
    categories: fc.array(arbFilterValue(), { minLength: 0, maxLength: 5 }),
    sizes: fc.array(arbFilterValue(), { minLength: 0, maxLength: 5 }),
    colors: fc.array(arbFilterValue(), { minLength: 0, maxLength: 5 }),
    priceRange: arbPriceRange(),
  });
}

/**
 * Arbitrary generator for SortOption.
 */
function arbSortOption(): fc.Arbitrary<SortOption> {
  return fc.constantFrom('newest', 'price-asc', 'price-desc', 'popularity');
}

describe('Query Params Utils - Property-Based Tests', () => {
  // Feature: storefront-catalog, Property 2: Query params round-trip
  // Validates: Requirements 2.3, 2.4, 3.4
  describe('Property 2: Query params round-trip', () => {
    it('deserializeFilters(serializeFilters(filters, sort)) produces equivalent state', () => {
      fc.assert(
        fc.property(arbFilterState(), arbSortOption(), (filters, sort) => {
          const params = serializeFilters(filters, sort);
          const result = deserializeFilters(params);

          // Verify categories round-trip
          expect(result.filters.categories).toEqual(filters.categories);

          // Verify sizes round-trip
          expect(result.filters.sizes).toEqual(filters.sizes);

          // Verify colors round-trip
          expect(result.filters.colors).toEqual(filters.colors);

          // Verify priceRange round-trip
          if (filters.priceRange === null) {
            expect(result.filters.priceRange).toBeNull();
          } else {
            expect(result.filters.priceRange).not.toBeNull();
            expect(result.filters.priceRange!.min).toBeCloseTo(filters.priceRange.min, 10);
            expect(result.filters.priceRange!.max).toBeCloseTo(filters.priceRange.max, 10);
          }

          // Verify sort round-trip
          expect(result.sort).toBe(sort);
        }),
        { numRuns: 100 }
      );
    });
  });
});
