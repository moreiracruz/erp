import { Routes } from '@angular/router';

export const STOREFRONT_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./home/home.component').then(m => m.HomeComponent) },
  { path: 'catalog', loadComponent: () => import('./catalog/catalog-page.component').then(m => m.CatalogPageComponent) },
  { path: 'product/:uuid', loadComponent: () => import('./product-detail/product-detail-page.component').then(m => m.ProductDetailPageComponent) },
];
