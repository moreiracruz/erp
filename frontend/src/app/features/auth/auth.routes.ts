import { Routes } from '@angular/router';

export const AUTH_ROUTES: Routes = [
  { path: 'login', loadComponent: () => import('./login/login.component').then(m => m.LoginComponent) },
  { path: 'register', loadComponent: () => import('./register/register.component').then(m => m.RegisterComponent) },
  { path: 'activate', loadComponent: () => import('./activate/activate.component').then(m => m.ActivateComponent) },
  { path: 'recovery', loadComponent: () => import('./recovery/recovery.component').then(m => m.RecoveryComponent) },
  { path: 'unauthorized', loadComponent: () => import('./unauthorized/unauthorized.component').then(m => m.UnauthorizedComponent) },
];
