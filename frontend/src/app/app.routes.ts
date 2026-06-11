import { Routes } from '@angular/router';
import { authGuard, roleGuard } from './infrastructure/auth/auth.guard';

export const routes: Routes = [
  // Public routes
  {
    path: '',
    loadChildren: () => import('./features/storefront/storefront.routes').then(m => m.STOREFRONT_ROUTES),
  },
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.routes').then(m => m.AUTH_ROUTES),
  },

  // Protected: Customer area
  {
    path: 'account',
    canActivate: [authGuard],
    loadChildren: () => import('./features/account/account.routes').then(m => m.ACCOUNT_ROUTES),
  },

  // Protected: Operations (cashier, stock, finance, manager)
  {
    path: 'pos',
    canActivate: [authGuard, roleGuard('ROLE_CASHIER', 'ROLE_MANAGER')],
    loadChildren: () => import('./features/pos/pos.routes').then(m => m.POS_ROUTES),
  },
  {
    path: 'inventory',
    canActivate: [authGuard, roleGuard('ROLE_STOCK', 'ROLE_MANAGER')],
    loadChildren: () => import('./features/inventory/inventory.routes').then(m => m.INVENTORY_ROUTES),
  },
  {
    path: 'dashboard',
    canActivate: [authGuard, roleGuard('ROLE_MANAGER', 'ROLE_FINANCE')],
    loadChildren: () => import('./features/dashboard/dashboard.routes').then(m => m.DASHBOARD_ROUTES),
  },
  {
    path: 'admin',
    canActivate: [authGuard, roleGuard('ROLE_MANAGER')],
    loadChildren: () => import('./features/admin/admin.routes').then(m => m.ADMIN_ROUTES),
  },

  // Fallback
  { path: '**', redirectTo: '' },
];
