import { describe, it, expect, beforeEach, vi } from 'vitest';
import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';

import { CartPageComponent } from './cart-page.component';
import { CartService } from '../../storefront/services/cart.service';
import { CartItem } from '../../storefront/catalog/models';

function makeCartItem(overrides: Partial<CartItem> = {}): CartItem {
  return {
    productUuid: 'prod-1',
    variantUuid: 'var-1',
    productName: 'Camiseta Básica',
    size: 'M',
    color: 'Azul',
    price: 59.9,
    quantity: 2,
    ...overrides,
  };
}

/**
 * Validates: Requirements 1.1, 1.2, 1.4, 1.5
 */
describe('CartPageComponent - Unit Tests', () => {
  let component: CartPageComponent;
  let fixture: ComponentFixture<CartPageComponent>;
  let cartService: CartService;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CartPageComponent],
      providers: [
        provideRouter([{ path: 'checkout', component: CartPageComponent }]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CartPageComponent);
    component = fixture.componentInstance;
    cartService = TestBed.inject(CartService);
    router = TestBed.inject(Router);
  });

  describe('Rendering items (Requirement 1.1)', () => {
    it('renders item list when cart has items', () => {
      const item = makeCartItem();
      cartService.addItem(item);
      fixture.detectChanges();

      const el: HTMLElement = fixture.nativeElement;
      const items = el.querySelectorAll('.cart-page__item');
      expect(items.length).toBe(1);
      expect(el.querySelector('.cart-page__item-name')?.textContent).toContain('Camiseta Básica');
    });
  });

  describe('Empty state (Requirement 1.5)', () => {
    it('shows empty state with message when cart is empty', () => {
      fixture.detectChanges();

      const el: HTMLElement = fixture.nativeElement;
      const emptyDiv = el.querySelector('.cart-page__empty');
      expect(emptyDiv).not.toBeNull();
      expect(el.querySelector('.cart-page__empty-text')?.textContent).toContain('Seu carrinho está vazio');
    });
  });

  describe('Increment quantity', () => {
    it('calls updateQuantity with qty + 1', () => {
      const item = makeCartItem({ variantUuid: 'var-inc', quantity: 3 });
      cartService.addItem(item);

      const spy = vi.spyOn(cartService, 'updateQuantity');
      component.increment('var-inc');

      expect(spy).toHaveBeenCalledWith('var-inc', 4);
    });
  });

  describe('Decrement quantity', () => {
    it('calls updateQuantity with qty - 1', () => {
      const item = makeCartItem({ variantUuid: 'var-dec', quantity: 3 });
      cartService.addItem(item);

      const spy = vi.spyOn(cartService, 'updateQuantity');
      component.decrement('var-dec');

      expect(spy).toHaveBeenCalledWith('var-dec', 2);
    });

    it('does not go below 1', () => {
      const item = makeCartItem({ variantUuid: 'var-min', quantity: 1 });
      cartService.addItem(item);

      const spy = vi.spyOn(cartService, 'updateQuantity');
      component.decrement('var-min');

      expect(spy).not.toHaveBeenCalled();
    });
  });

  describe('Remove item (Requirement 1.2)', () => {
    it('calls removeItem with correct variantUuid', () => {
      const item = makeCartItem({ variantUuid: 'var-remove' });
      cartService.addItem(item);

      const spy = vi.spyOn(cartService, 'removeItem');
      component.remove('var-remove');

      expect(spy).toHaveBeenCalledWith('var-remove');
    });
  });

  describe('Go to checkout (Requirement 1.4)', () => {
    it('navigates to /checkout', () => {
      const spy = vi.spyOn(router, 'navigate');
      component.goToCheckout();

      expect(spy).toHaveBeenCalledWith(['/checkout']);
    });
  });
});
