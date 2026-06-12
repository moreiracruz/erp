import { Routes } from '@angular/router';

export const CHECKOUT_ROUTES: Routes = [
  {
    path: 'cart',
    loadComponent: () =>
      import('./cart/cart-page.component').then((m) => m.CartPageComponent),
  },
  {
    path: '',
    loadComponent: () =>
      import('./checkout-page/checkout-page.component').then((m) => m.CheckoutPageComponent),
  },
];
