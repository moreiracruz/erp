import { Routes } from '@angular/router';
import { authGuard, roleGuard } from '../../infrastructure/auth/auth.guard';

export const CHECKOUT_ROUTES: Routes = [
  {
    path: 'cart',
    loadComponent: () =>
      import('./cart/cart-page.component').then((m) => m.CartPageComponent),
  },
  {
    path: '',
    canActivate: [authGuard, roleGuard('ROLE_USER')],
    loadComponent: () =>
      import('./checkout-page/checkout-page.component').then((m) => m.CheckoutPageComponent),
  },
];
