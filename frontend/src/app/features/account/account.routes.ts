import { Routes } from '@angular/router';

export const ACCOUNT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./account-layout.component').then(m => m.AccountLayoutComponent),
    children: [
      { path: '', redirectTo: 'profile', pathMatch: 'full' },
      { path: 'profile', loadComponent: () => import('./profile/profile.component').then(m => m.ProfileComponent) },
      { path: 'orders', loadComponent: () => import('./orders/orders.component').then(m => m.OrdersComponent) },
      { path: 'addresses', loadComponent: () => import('./addresses/addresses.component').then(m => m.AddressesComponent) },
      { path: 'password', loadComponent: () => import('./change-password/change-password.component').then(m => m.ChangePasswordComponent) },
    ],
  },
];
