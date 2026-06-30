import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import * as fc from 'fast-check';

import { jwtInterceptor } from './jwt.interceptor';
import { TokenStorageService } from '../storage/token-storage.service';

/**
 * Property 10: JWT interceptor excludes auth endpoints
 *
 * For any HTTP request URL, if the URL contains `/auth/login`, `/auth/register` or `/auth/refresh`,
 * then the jwtInterceptor SHALL NOT attach an Authorization header; for all other URLs,
 * if a token is present in storage, the interceptor SHALL attach `Authorization: Bearer <token>`.
 *
 * **Validates: Requirements 7.5**
 */
describe('Feature: frontend-auth, Property 10: JWT interceptor excludes auth endpoints', () => {
  let httpClient: HttpClient;
  let httpTesting: HttpTestingController;
  let tokenStorageMock: { getAccessToken: () => string | null };

  const FAKE_TOKEN = 'fake-jwt-access-token-value';

  beforeEach(() => {
    tokenStorageMock = {
      getAccessToken: () => FAKE_TOKEN,
    };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([jwtInterceptor])),
        provideHttpClientTesting(),
        { provide: TokenStorageService, useValue: tokenStorageMock },
      ],
    });

    httpClient = TestBed.inject(HttpClient);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should NOT attach Authorization header when URL contains /auth/login, /auth/register or /auth/refresh', () => {
    fc.assert(
      fc.property(
        fc.oneof(
          fc.constant('http://api.example.com/auth/login'),
          fc.constant('http://api.example.com/auth/register'),
          fc.constant('http://api.example.com/auth/refresh'),
          fc.constant('http://api.example.com/api/v1/auth/login'),
          fc.constant('http://api.example.com/api/v1/auth/register'),
          fc.constant('http://api.example.com/api/v1/auth/refresh'),
          fc.constant('/auth/login'),
          fc.constant('/auth/register'),
          fc.constant('/auth/refresh'),
        ),
        (authUrl) => {
          httpClient.get(authUrl).subscribe();

          const req = httpTesting.expectOne(authUrl);
          expect(req.request.headers.has('Authorization')).toBe(false);
          req.flush({});
        },
      ),
      { numRuns: 100 },
    );
  });

  it('should attach Authorization: Bearer <token> header when URL does NOT contain auth endpoints and token exists', () => {
    fc.assert(
      fc.property(
        fc.string({ minLength: 1 })
          .filter((s) => !s.includes('/auth/login')
            && !s.includes('/auth/register')
            && !s.includes('/auth/refresh'))
          .map((s) => 'http://api.example.com/' + encodeURIComponent(s)),
        (nonAuthUrl) => {
          httpClient.get(nonAuthUrl).subscribe();

          const req = httpTesting.expectOne(nonAuthUrl);
          expect(req.request.headers.has('Authorization')).toBe(true);
          expect(req.request.headers.get('Authorization')).toBe(`Bearer ${FAKE_TOKEN}`);
          req.flush({});
        },
      ),
      { numRuns: 100 },
    );
  });

  it('should NOT attach Authorization header on non-auth URLs when no token is present', () => {
    // Override mock to return null (no token)
    tokenStorageMock.getAccessToken = () => null;

    fc.assert(
      fc.property(
        fc.string({ minLength: 1 })
          .filter((s) => !s.includes('/auth/login')
            && !s.includes('/auth/register')
            && !s.includes('/auth/refresh'))
          .map((s) => 'http://api.example.com/' + encodeURIComponent(s)),
        (nonAuthUrl) => {
          httpClient.get(nonAuthUrl).subscribe();

          const req = httpTesting.expectOne(nonAuthUrl);
          expect(req.request.headers.has('Authorization')).toBe(false);
          req.flush({});
        },
      ),
      { numRuns: 100 },
    );
  });
});
