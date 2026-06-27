import { TestBed } from '@angular/core/testing';
import * as fc from 'fast-check';

import { CartService } from './cart.service';
import { CartItem } from '../catalog/models';

/**
 * Arbitrary generator for CartItem with positive prices and quantities.
 */
function arbCartItem(): fc.Arbitrary<CartItem> {
  return fc.record({
    productUuid: fc.uuid(),
    variantUuid: fc.uuid(),
    productName: fc.string({ minLength: 1, maxLength: 50 }),
    size: fc.constantFrom('P', 'M', 'G', 'GG'),
    color: fc.constantFrom('Preto', 'Branco', 'Azul', 'Vermelho'),
    price: fc.integer({ min: 1, max: 999999 }).map((cents) => cents / 100),
    quantity: fc.integer({ min: 1, max: 100 }),
    imageUrl: fc.option(fc.webUrl(), { nil: undefined }),
  });
}

/**
 * Generates a CartItem array where each item has a unique variantUuid.
 */
function arbCartItems(): fc.Arbitrary<CartItem[]> {
  return fc.array(arbCartItem(), { minLength: 0, maxLength: 20 }).map((items) => {
    const seen = new Set<string>();
    return items.filter((item) => {
      if (seen.has(item.variantUuid)) return false;
      seen.add(item.variantUuid);
      return true;
    });
  });
}

describe('CartService - Property-Based Tests', () => {
  let service: CartService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CartService);
  });

  // Feature: frontend-checkout, Property 1: Subtotal equals sum of line totals
  // Validates: Requirements 1.3, 6.2
  describe('Property 1: Subtotal equals sum of line totals', () => {
    it('subtotal() equals Σ(price × quantity) for any cart items', () => {
      fc.assert(
        fc.property(arbCartItems(), (items) => {
          service.items.set(items);

          const expectedSubtotal = items.reduce(
            (sum, item) => sum + item.price * item.quantity,
            0
          );

          expect(service.subtotal()).toBeCloseTo(expectedSubtotal, 5);
        }),
        { numRuns: 100 }
      );
    });
  });

  // Feature: frontend-checkout, Property 2: Shipping cost threshold
  // Validates: Requirements 3.2, 6.2
  describe('Property 2: Shipping cost threshold', () => {
    it('shippingCost() is 0 when subtotal >= 299 and 15.90 when subtotal < 299', () => {
      fc.assert(
        fc.property(arbCartItems(), (items) => {
          service.items.set(items);
          const subtotal = service.subtotal();
          if (subtotal >= 299) {
            expect(service.shippingCost()).toBe(0);
          } else {
            expect(service.shippingCost()).toBe(15.90);
          }
        }),
        { numRuns: 100 }
      );
    });
  });

  // Feature: frontend-checkout, Property 3: Total is subtotal plus shipping
  // Validates: Requirements 6.2
  describe('Property 3: Total is subtotal plus shipping', () => {
    it('total() equals subtotal() + shippingCost() for any cart state', () => {
      fc.assert(
        fc.property(arbCartItems(), (items) => {
          service.items.set(items);
          expect(service.total()).toBeCloseTo(service.subtotal() + service.shippingCost(), 10);
        }),
        { numRuns: 100 }
      );
    });
  });

  // Feature: frontend-checkout, Property 4: Remove item decreases cart size and excludes item
  // Validates: Requirements 1.2
  describe('Property 4: Remove item decreases cart size and excludes item', () => {
    it('removeItem reduces cart length by 1 and excludes the removed UUID', () => {
      fc.assert(
        fc.property(
          arbCartItems().filter((items) => items.length > 0).chain((items) =>
            fc.record({
              items: fc.constant(items),
              index: fc.integer({ min: 0, max: items.length - 1 }),
            })
          ),
          ({ items, index }) => {
            service.items.set(items);
            const targetUuid = items[index].variantUuid;
            const originalLength = items.length;

            service.removeItem(targetUuid);

            expect(service.items().length).toBe(originalLength - 1);
            expect(service.items().some((i) => i.variantUuid === targetUuid)).toBe(false);
          }
        ),
        { numRuns: 100 }
      );
    });
  });

  // Feature: frontend-checkout, Property 5: updateQuantity changes only the target item
  // Validates: Requirements 6.1
  describe('Property 5: updateQuantity changes only the target item', () => {
    it('updateQuantity sets target qty and leaves other items unchanged', () => {
      fc.assert(
        fc.property(
          arbCartItems().filter((items) => items.length >= 2).chain((items) =>
            fc.record({
              items: fc.constant(items),
              index: fc.integer({ min: 0, max: items.length - 1 }),
              newQty: fc.integer({ min: 1, max: 999 }),
            })
          ),
          ({ items, index, newQty }) => {
            service.items.set(items);
            const targetUuid = items[index].variantUuid;

            service.updateQuantity(targetUuid, newQty);

            const updated = service.items();
            const target = updated.find((i) => i.variantUuid === targetUuid);
            expect(target!.quantity).toBe(newQty);

            // All other items unchanged
            for (let i = 0; i < items.length; i++) {
              if (items[i].variantUuid !== targetUuid) {
                const found = updated.find((u) => u.variantUuid === items[i].variantUuid);
                expect(found).toEqual(items[i]);
              }
            }
          }
        ),
        { numRuns: 100 }
      );
    });
  });
});
