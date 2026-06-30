import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { TokenStorageService } from '../storage/token-storage.service';

/**
 * HTTP interceptor that attaches the JWT Bearer token to outgoing requests.
 */
export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const tokenStorage = inject(TokenStorageService);
  const token = tokenStorage.getAccessToken();

  if (token
    && !req.url.includes('/auth/login')
    && !req.url.includes('/auth/register')
    && !req.url.includes('/auth/activation')
    && !req.url.includes('/auth/refresh')) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });
  }

  return next(req);
};
