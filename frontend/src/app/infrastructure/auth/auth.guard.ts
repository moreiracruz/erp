import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { TokenStorageService } from '../storage/token-storage.service';

/**
 * Guard that protects routes requiring authentication.
 */
export const authGuard: CanActivateFn = () => {
  const tokenStorage = inject(TokenStorageService);
  const router = inject(Router);

  if (tokenStorage.getAccessToken()) {
    return true;
  }

  return router.createUrlTree(['/auth/login']);
};

/**
 * Guard that protects routes requiring specific roles.
 */
export const roleGuard = (...allowedRoles: string[]): CanActivateFn => {
  return () => {
    const tokenStorage = inject(TokenStorageService);
    const router = inject(Router);
    const role = tokenStorage.getUserRole();

    const effectiveRoles = role === 'ROLE_SUPER_ADMIN'
      ? ['ROLE_SUPER_ADMIN', 'ROLE_MANAGER', 'ROLE_CASHIER', 'ROLE_STOCK', 'ROLE_FINANCE']
      : role ? [role] : [];

    if (effectiveRoles.some(effectiveRole => allowedRoles.includes(effectiveRole))) {
      return true;
    }

    return router.createUrlTree(['/']);
  };
};
