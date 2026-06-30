import { Routes } from '@angular/router';

export const CONSIGNMENT_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./consignment.component').then(m => m.ConsignmentComponent),
  },
];
