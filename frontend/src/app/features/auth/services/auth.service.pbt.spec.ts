import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import * as fc from 'fast-check';

import { AuthService } from './auth.service';
import { AuthPort } from '../../../core/ports/auth.port';
import { TokenPair, UserRole } from '../../../core/models/user.model';
import { TokenStorageService } from '../../../infrastructure/storage/token-storage.service';

/**
 * Property-Based Tests for AuthService error mapping.
 *
 * **Validates: Requirements 1.4, 7.3**
 */
describe('AuthService — Property 1: Error mapping preserves dynamic parameters', () => {
  let authService: AuthService;
  let authPortMock: { login: ReturnType<typeof vi.fn> };
  let tokenStorageMock: { setStorageType: ReturnType<typeof vi.fn> };
  let routerMock: { navigate: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    authPortMock = {
      login: vi.fn(),
    };

    tokenStorageMock = {
      setStorageType: vi.fn(),
    };

    routerMock = {
      navigate: vi.fn(),
    };

    TestBed.configureTestingModule({
      providers: [
        AuthService,
        { provide: AuthPort, useValue: authPortMock },
        { provide: TokenStorageService, useValue: tokenStorageMock },
        { provide: Router, useValue: routerMock },
      ],
    });

    authService = TestBed.inject(AuthService);
  });

  it('should include the exact remainingMinutes value in the error message for HTTP 423', () => {
    fc.assert(
      fc.property(fc.nat(), (minutes) => {
        authPortMock.login.mockReturnValue(
          throwError(
            () =>
              new HttpErrorResponse({
                status: 423,
                error: { remainingMinutes: minutes },
              }),
          ),
        );

        let errorMessage: string | undefined;
        authService.login('test@example.com', 'password123', false).subscribe({
          error: (err: string) => {
            errorMessage = err;
          },
        });

        expect(errorMessage).toBeDefined();
        expect(errorMessage).toContain(String(minutes));
        expect(errorMessage).toBe(
          `Conta bloqueada. Tente novamente em ${minutes} minutos`,
        );
      }),
      { numRuns: 100 },
    );
  });

  it('should include the exact retryAfterMinutes value in the error message for HTTP 429', () => {
    fc.assert(
      fc.property(fc.nat(), (minutes) => {
        authPortMock.login.mockReturnValue(
          throwError(
            () =>
              new HttpErrorResponse({
                status: 429,
                error: { retryAfterMinutes: minutes },
              }),
          ),
        );

        let errorMessage: string | undefined;
        authService.login('test@example.com', 'password123', false).subscribe({
          error: (err: string) => {
            errorMessage = err;
          },
        });

        expect(errorMessage).toBeDefined();
        expect(errorMessage).toContain(String(minutes));
        expect(errorMessage).toBe(
          `Muitas tentativas. Tente novamente em ${minutes} minutos`,
        );
      }),
      { numRuns: 100 },
    );
  });
});

/**
 * Property-Based Tests for JWT decode round trip.
 *
 * Property 8: JWT decode round trip
 * For any valid User object (uuid, username, role, active), encoding it as a JWT payload
 * (base64) and passing it through decodeToken SHALL produce a User object with matching
 * uuid, username, role, and active values.
 *
 * **Validates: Requirements 5.2**
 */
describe('AuthService — Property 8: JWT decode round trip', () => {
  let authService: AuthService;
  let authPortMock: {
    login: ReturnType<typeof vi.fn>;
    logout: ReturnType<typeof vi.fn>;
  };
  let tokenStorageMock: {
    setStorageType: ReturnType<typeof vi.fn>;
    saveTokens: ReturnType<typeof vi.fn>;
    saveUserRole: ReturnType<typeof vi.fn>;
    getAccessToken: ReturnType<typeof vi.fn>;
    getRefreshToken: ReturnType<typeof vi.fn>;
    clear: ReturnType<typeof vi.fn>;
  };
  let routerMock: { navigate: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    authPortMock = {
      login: vi.fn(),
      logout: vi.fn().mockReturnValue(of(void 0)),
    };

    tokenStorageMock = {
      setStorageType: vi.fn(),
      saveTokens: vi.fn(),
      saveUserRole: vi.fn(),
      getAccessToken: vi.fn().mockReturnValue(null),
      getRefreshToken: vi.fn().mockReturnValue(null),
      clear: vi.fn(),
    };

    routerMock = {
      navigate: vi.fn(),
    };

    TestBed.configureTestingModule({
      providers: [
        AuthService,
        { provide: AuthPort, useValue: authPortMock },
        { provide: TokenStorageService, useValue: tokenStorageMock },
        { provide: Router, useValue: routerMock },
      ],
    });

    authService = TestBed.inject(AuthService);
  });

  /**
   * Helper to create a fake JWT token from a payload object.
   * JWT format: base64(header).base64(payload).signature
   */
  function createFakeJwt(payload: Record<string, unknown>): string {
    const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
    const body = btoa(JSON.stringify(payload));
    const signature = 'fake-signature';
    return `${header}.${body}.${signature}`;
  }

  it('should decode any valid User from a JWT payload and produce matching fields', () => {
    const userArb = fc.record({
      uuid: fc.uuid(),
      username: fc.string({ minLength: 1 }),
      role: fc.constantFrom(
        'ROLE_MANAGER' as UserRole,
        'ROLE_CASHIER' as UserRole,
        'ROLE_STOCK' as UserRole,
        'ROLE_FINANCE' as UserRole,
      ),
      active: fc.boolean(),
    });

    fc.assert(
      fc.property(userArb, (user) => {
        // Encode user data as JWT payload using the same field names decodeToken expects
        const payload = {
          sub: user.uuid,
          username: user.username,
          role: user.role,
          active: user.active,
        };

        const fakeAccessToken = createFakeJwt(payload);
        const tokenPair: TokenPair = {
          accessToken: fakeAccessToken,
          refreshToken: 'fake-refresh-token',
          expiresIn: 3600,
        };

        authPortMock.login.mockReturnValue(of(tokenPair));

        authService.login('test@example.com', 'password123', false).subscribe();

        const currentUser = authService.currentUser();
        expect(currentUser).not.toBeNull();
        expect(currentUser!.uuid).toBe(user.uuid);
        expect(currentUser!.username).toBe(user.username);
        expect(currentUser!.role).toBe(user.role);
        expect(currentUser!.active).toBe(user.active);
      }),
      { numRuns: 100 },
    );
  });
});

/**
 * Property-Based Tests for AuthService token refresh scheduling.
 *
 * Property 9: Token refresh scheduled at 80% of expiry
 * For any positive expiresIn value (in seconds), the scheduleRefresh function
 * SHALL set a timer with delay equal to expiresIn × 0.8 × 1000 milliseconds.
 *
 * **Validates: Requirements 5.3**
 */
describe('AuthService — Property 9: Token refresh scheduled at 80% of expiry', () => {
  let authService: AuthService;
  let authPortMock: {
    login: ReturnType<typeof vi.fn>;
    logout: ReturnType<typeof vi.fn>;
  };
  let tokenStorageMock: {
    setStorageType: ReturnType<typeof vi.fn>;
    saveTokens: ReturnType<typeof vi.fn>;
    saveUserRole: ReturnType<typeof vi.fn>;
    getAccessToken: ReturnType<typeof vi.fn>;
    getRefreshToken: ReturnType<typeof vi.fn>;
    clear: ReturnType<typeof vi.fn>;
  };
  let routerMock: { navigate: ReturnType<typeof vi.fn> };
  let setTimeoutSpy: ReturnType<typeof vi.spyOn>;

  function createFakeJwt(payload: Record<string, unknown>): string {
    const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
    const body = btoa(JSON.stringify(payload));
    const signature = 'fakesignature';
    return `${header}.${body}.${signature}`;
  }

  beforeEach(() => {
    authPortMock = {
      login: vi.fn(),
      logout: vi.fn(),
    };

    tokenStorageMock = {
      setStorageType: vi.fn(),
      saveTokens: vi.fn(),
      saveUserRole: vi.fn(),
      getAccessToken: vi.fn().mockReturnValue(null),
      getRefreshToken: vi.fn().mockReturnValue(null),
      clear: vi.fn(),
    };

    routerMock = {
      navigate: vi.fn(),
    };

    TestBed.configureTestingModule({
      providers: [
        AuthService,
        { provide: AuthPort, useValue: authPortMock },
        { provide: TokenStorageService, useValue: tokenStorageMock },
        { provide: Router, useValue: routerMock },
      ],
    });

    authService = TestBed.inject(AuthService);
    setTimeoutSpy = vi.spyOn(globalThis, 'setTimeout');
  });

  afterEach(() => {
    setTimeoutSpy.mockRestore();
  });

  it('should schedule refresh at exactly 80% of expiresIn converted to milliseconds', () => {
    fc.assert(
      fc.property(fc.integer({ min: 1, max: 86400 }), (expiresIn) => {
        setTimeoutSpy.mockClear();

        const fakeAccessToken = createFakeJwt({
          sub: 'user-uuid-123',
          username: 'testuser@example.com',
          role: 'ROLE_CASHIER',
          active: true,
        });

        authPortMock.login.mockReturnValue(
          of({
            accessToken: fakeAccessToken,
            refreshToken: 'fake-refresh-token',
            expiresIn,
          }),
        );

        authService.login('test@example.com', 'password123', false).subscribe();

        const expectedDelay = expiresIn * 0.8 * 1000;

        // Find the setTimeout call with our expected delay
        const matchingCall = setTimeoutSpy.mock.calls.find(
          (call: unknown[]) => call[1] === expectedDelay,
        );
        expect(matchingCall).toBeDefined();
        expect(matchingCall![1]).toBe(expectedDelay);
      }),
      { numRuns: 100 },
    );
  });
});
