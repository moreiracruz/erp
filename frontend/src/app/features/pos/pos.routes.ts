import { Routes } from '@angular/router';

export const POS_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./terminal/terminal.component').then(m => m.TerminalComponent) },
];
