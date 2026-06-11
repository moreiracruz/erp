import { Routes } from '@angular/router';

export const ACCOUNT_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./profile/profile.component').then(m => m.ProfileComponent) },
];
