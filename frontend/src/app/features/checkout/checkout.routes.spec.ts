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

  it('keeps cart public and protects checkout', () => {
    const cartRoute = CHECKOUT_ROUTES.find((r) => r.path === 'cart');
    const checkoutRoute = CHECKOUT_ROUTES.find((r) => r.path === '');

    expect(cartRoute?.canActivate).toBeUndefined();
    expect(checkoutRoute?.canActivate).toBeDefined();
    expect(checkoutRoute?.canActivate?.length).toBe(2);
  });
});
