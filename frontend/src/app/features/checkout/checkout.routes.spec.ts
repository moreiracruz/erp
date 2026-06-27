import { describe, it, expect } from 'vitest';
import { CHECKOUT_ROUTES } from './checkout.routes';

describe('CHECKOUT_ROUTES', () => {
  it('should define cart route at path "cart"', () => {
    const cartRoute = CHECKOUT_ROUTES.find((r) => r.path === 'cart');
    expect(cartRoute).toBeDefined();
  });

  it('should define checkout route at path ""', () => {
    const checkoutRoute = CHECKOUT_ROUTES.find((r) => r.path === '');
    expect(checkoutRoute).toBeDefined();
  });

  it('should have no auth guards on any route', () => {
    for (const route of CHECKOUT_ROUTES) {
      expect(route.canActivate).toBeUndefined();
      expect(route.canMatch).toBeUndefined();
      expect((route as any).canActivateChild).toBeUndefined();
    }
  });
});
