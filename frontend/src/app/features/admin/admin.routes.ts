import { Routes } from '@angular/router';

export const ADMIN_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./products/products.component').then(m => m.ProductsComponent) },
];
