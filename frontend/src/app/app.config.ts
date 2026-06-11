import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter, withViewTransitions } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';
import { jwtInterceptor } from './infrastructure/auth/jwt.interceptor';
import { AuthPort } from './core/ports';
import { AuthHttpAdapter } from './infrastructure/http/auth-http.adapter';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes, withViewTransitions()),
    provideHttpClient(withInterceptors([jwtInterceptor])),

    // Hexagonal: bind ports to adapters
    { provide: AuthPort, useClass: AuthHttpAdapter },
  ],
};
