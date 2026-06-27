// Feature: storefront-catalog, Property 9: Variant attribute extraction
// Feature: storefront-catalog, Property 12: Default variant selection
import * as fc from 'fast-check';
import { Variant } from '../../../../core/models';
import {
  getAvailableSizes,
  getAvailableColors,
  getDefaultVariant,
} from './variant.utils';

/**
 * Arbitrary generator for a Variant object.
 */
function arbVariant(active?: boolean): fc.Arbitrary<Variant> {
  return fc.record({
    uuid: fc.uuid(),
    sku: fc.string({ minLength: 1, maxLength: 20 }),
    size: fc.constantFrom('PP', 'P', 'M', 'G', 'GG', 'XG'),
    color: fc.constantFrom('Preto', 'Branco', 'Azul', 'Vermelho', 'Verde', 'Rosa'),
    barcode: fc.string({ minLength: 8, maxLength: 13 }),
    price: fc.float({ min: 1, max: 9999, noNaN: true }),
    cost: fc.float({ min: 0.5, max: 5000, noNaN: true }),
    active: active !== undefined ? fc.constant(active) : fc.boolean(),
  });
}

/**
 * Validates: Requirements 6.1, 6.2
 *
 * Property 9: Variant attribute extraction
 * For any product with one or more active variants, the set of sizes displayed SHALL equal
 * the distinct set of variant.size values from all active variants, and the set of colors
 * displayed SHALL equal the distinct set of variant.color values from all active variants.
 */
describe('variant.utils - Property 9: Variant attribute extraction', () => {
  // **Validates: Requirements 6.1, 6.2**

  it('getAvailableSizes returns exactly the distinct sizes from active variants', () => {
    fc.assert(
      fc.property(
        fc.array(arbVariant(), { minLength: 1, maxLength: 30 }),
        (variants: Variant[]) => {
          const result = getAvailableSizes(variants);

          // Compute expected: distinct sizes from active variants
          const expectedSet = new Set(
            variants.filter((v) => v.active).map((v) => v.size)
          );

          // Set equality (order-independent)
          const resultSet = new Set(result);
          expect(resultSet.size).toBe(expectedSet.size);
          for (const size of expectedSet) {
            expect(resultSet.has(size)).toBe(true);
          }

          // No duplicates in result
          expect(result.length).toBe(resultSet.size);
        }
      ),
      { numRuns: 100 }
    );
  });

  it('getAvailableColors returns exactly the distinct colors from active variants', () => {
    fc.assert(
      fc.property(
        fc.array(arbVariant(), { minLength: 1, maxLength: 30 }),
        (variants: Variant[]) => {
          const result = getAvailableColors(variants);

          // Compute expected: distinct colors from active variants
          const expectedSet = new Set(
            variants.filter((v) => v.active).map((v) => v.color)
          );

          // Set equality (order-independent)
          const resultSet = new Set(result);
          expect(resultSet.size).toBe(expectedSet.size);
          for (const color of expectedSet) {
            expect(resultSet.has(color)).toBe(true);
          }

          // No duplicates in result
          expect(result.length).toBe(resultSet.size);
        }
      ),
      { numRuns: 100 }
    );
  });
});

describe('variant.utils - Property 12: Default variant selection', () => {
  // **Validates: Requirements 6.6**

  it('should return size/color matching the first active variant in the array', () => {
    fc.assert(
      fc.property(
        fc
          .tuple(
            fc.array(arbVariant(), { minLength: 0, maxLength: 10 }),
            arbVariant(true),
            fc.array(arbVariant(), { minLength: 0, maxLength: 10 })
          )
          .map(([before, active, after]) => [...before, active, ...after]),
        (variants: Variant[]) => {
          const result = getDefaultVariant(variants);
          const firstActive = variants.find((v) => v.active);

          expect(firstActive).toBeDefined();
          expect(result.size).toBe(firstActive!.size);
          expect(result.color).toBe(firstActive!.color);
          expect(result.variant).toBe(firstActive);
        }
      ),
      { numRuns: 100 }
    );
  });

  it('should return nulls when no active variants exist', () => {
    fc.assert(
      fc.property(
        fc.array(arbVariant(false), { minLength: 0, maxLength: 10 }),
        (variants: Variant[]) => {
          const result = getDefaultVariant(variants);

          expect(result.size).toBeNull();
          expect(result.color).toBeNull();
          expect(result.variant).toBeNull();
        }
      ),
      { numRuns: 100 }
    );
  });
});
