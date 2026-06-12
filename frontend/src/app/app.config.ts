import { ApplicationConfig, LOCALE_ID, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { registerLocaleData } from '@angular/common';
import localePt from '@angular/common/locales/pt';

import { routes } from './app.routes';
import { jwtInterceptor } from './infrastructure/auth/jwt.interceptor';
import { AuthPort, ProductPort } from './core/ports';
import { AuthHttpAdapter } from './infrastructure/http/auth-http.adapter';
import { ProductHttpAdapter } from './infrastructure/http/product-http.adapter';

registerLocaleData(localePt);

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([jwtInterceptor])),

    // Locale pt-BR
    { provide: LOCALE_ID, useValue: 'pt' },

    // Hexagonal: bind ports to adapters
    { provide: AuthPort, useClass: AuthHttpAdapter },
    { provide: ProductPort, useClass: ProductHttpAdapter },
  ],
};
