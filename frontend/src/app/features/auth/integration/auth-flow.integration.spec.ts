// @vitest-environment jsdom
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { TestBed, ComponentFixture, fakeAsync, tick } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';

import { LoginComponent } from '../login/login.component';
import { AuthService } from '../services/auth.service';
import { AuthPort } from '../../../core/ports/auth.port';
import { TokenPair } from '../../../core/models/user.model';
import { TokenStorageService } from '../../../infrastructure/storage/token-storage.service';

/**
 * Integration tests for the complete login flow.
 * Uses REAL AuthService and TokenStorageService with mocked AuthPort and Router.
 *
 * Flow under test:
 * LoginComponent.onSubmit() → AuthService.login() → TokenStorageService → signals → navigation
 *
 * Validates: Requirements 10.4, 1.1, 1.6
 */
describe('Auth Flow Integration — Full Login', () => {
  let fixture: ComponentFixture<LoginComponent>;
  let component: LoginComponent;
  let authService: AuthService;
  let tokenStorage: TokenStorageService;
  let authPortMock: {
    login: ReturnType<typeof vi.fn>;
    register: ReturnType<typeof vi.fn>;
    activate: ReturnType<typeof vi.fn>;
    refresh: ReturnType<typeof vi.fn>;
    logout: ReturnType<typeof vi.fn>;
    getCurrentUser: ReturnType<typeof vi.fn>;
    recoverPassword: ReturnType<typeof vi.fn>;
  };
  let routerMock: { navigateByUrl: ReturnType<typeof vi.fn>; navigate: ReturnType<typeof vi.fn> };
  let activatedRouteMock: { snapshot: { queryParamMap: { get: ReturnType<typeof vi.fn> } } };

  const ACCESS_TOKEN_KEY = 'rf_access_token';
  const REFRESH_TOKEN_KEY = 'rf_refresh_token';
  const STORAGE_TYPE_KEY = 'rf_storage_type';

  function createFakeJwt(payload: Record<string, unknown>): string {
    const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
    const body = btoa(JSON.stringify(payload));
    return `${header}.${body}.fakesignature`;
  }

  const userPayload = {
    sub: 'uuid-abc-123',
    username: 'maria@reinoeflor.com',
    role: 'ROLE_MANAGER',
    active: true,
    exp: Math.floor(Date.now() / 1000) + 3600,
  };

  const fakeAccessToken = createFakeJwt(userPayload);

  const fakeTokenPair: TokenPair = {
    accessToken: fakeAccessToken,
    refreshToken: 'fake-refresh-token-xyz',
    expiresIn: 3600,
  };

  function setupTestBed(returnUrl: string | null = null) {
    authPortMock = {
      login: vi.fn().mockReturnValue(of(fakeTokenPair)),
      register: vi.fn(),
      activate: vi.fn(),
      refresh: vi.fn(),
      logout: vi.fn().mockReturnValue(of(void 0)),
      getCurrentUser: vi.fn(),
      recoverPassword: vi.fn(),
    };

    routerMock = {
      navigateByUrl: vi.fn(),
      navigate: vi.fn(),
    };

    activatedRouteMock = {
      snapshot: {
        queryParamMap: {
          get: vi.fn((key: string) => {
            if (key === 'returnUrl') return returnUrl;
            return null;
          }),
        },
      },
    };

    TestBed.configureTestingModule({
      imports: [LoginComponent, ReactiveFormsModule],
      providers: [
        AuthService,
        TokenStorageService,
        { provide: AuthPort, useValue: authPortMock },
        { provide: Router, useValue: routerMock },
        { provide: ActivatedRoute, useValue: activatedRouteMock },
      ],
    });

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService);
    tokenStorage = TestBed.inject(TokenStorageService);
    fixture.detectChanges();
  }

  beforeEach(() => {
    vi.useFakeTimers();
    localStorage.clear();
    sessionStorage.clear();
  });

  afterEach(() => {
    vi.useRealTimers();
    localStorage.clear();
    sessionStorage.clear();
  });

  describe('Full login flow: form submit → token stored → signals updated → navigation', () => {
    it('should complete the full login flow with correct signal states and token storage', () => {
      setupTestBed(null);

      // Fill form with valid credentials
      component.form.setValue({
        email: 'maria@reinoeflor.com',
        password: 'SecurePass123',
        rememberMe: false,
      });

      // Submit form
      component.onSubmit();

      // 1. Verify AuthPort.login was called with correct credentials
      expect(authPortMock.login).toHaveBeenCalledWith({
        username: 'maria@reinoeflor.com',
        password: 'SecurePass123',
      });

      // 2. Verify tokens are actually stored in sessionStorage (rememberMe = false)
      expect(sessionStorage.getItem(ACCESS_TOKEN_KEY)).toBe(fakeAccessToken);
      expect(sessionStorage.getItem(REFRESH_TOKEN_KEY)).toBe('fake-refresh-token-xyz');

      // 3. Verify AuthService signals are updated correctly
      expect(authService.currentUser()).not.toBeNull();
      expect(authService.currentUser()!.uuid).toBe('uuid-abc-123');
      expect(authService.currentUser()!.username).toBe('maria@reinoeflor.com');
      expect(authService.currentUser()!.role).toBe('ROLE_MANAGER');
      expect(authService.currentUser()!.active).toBe(true);
      expect(authService.isAuthenticated()).toBe(true);
      expect(authService.userRole()).toBe('ROLE_MANAGER');

      // 4. Verify navigation to default route for role
      expect(routerMock.navigateByUrl).toHaveBeenCalledWith('/admin/dashboard');
    });

    it('should navigate to returnUrl when present after successful login', () => {
      setupTestBed('/inventory/products');

      component.form.setValue({
        email: 'maria@reinoeflor.com',
        password: 'SecurePass123',
        rememberMe: false,
      });

      component.onSubmit();

      // Should navigate to the returnUrl, not the default route
      expect(routerMock.navigateByUrl).toHaveBeenCalledWith('/inventory/products');
    });

    it('should reset loading state after successful login', () => {
      setupTestBed(null);

      component.form.setValue({
        email: 'maria@reinoeflor.com',
        password: 'SecurePass123',
        rememberMe: false,
      });

      component.onSubmit();

      expect(component.loading()).toBe(false);
    });
  });

  describe('Remember-me persistence: localStorage vs sessionStorage', () => {
    it('should store tokens in localStorage when rememberMe is true', () => {
      setupTestBed(null);

      component.form.setValue({
        email: 'maria@reinoeflor.com',
        password: 'SecurePass123',
        rememberMe: true,
      });

      component.onSubmit();

      // Tokens should be in localStorage
      expect(localStorage.getItem(ACCESS_TOKEN_KEY)).toBe(fakeAccessToken);
      expect(localStorage.getItem(REFRESH_TOKEN_KEY)).toBe('fake-refresh-token-xyz');
      expect(localStorage.getItem(STORAGE_TYPE_KEY)).toBe('local');

      // Tokens should NOT be in sessionStorage
      expect(sessionStorage.getItem(ACCESS_TOKEN_KEY)).toBeNull();
      expect(sessionStorage.getItem(REFRESH_TOKEN_KEY)).toBeNull();
    });

    it('should store tokens in sessionStorage when rememberMe is false', () => {
      setupTestBed(null);

      component.form.setValue({
        email: 'maria@reinoeflor.com',
        password: 'SecurePass123',
        rememberMe: false,
      });

      component.onSubmit();

      // Tokens should be in sessionStorage
      expect(sessionStorage.getItem(ACCESS_TOKEN_KEY)).toBe(fakeAccessToken);
      expect(sessionStorage.getItem(REFRESH_TOKEN_KEY)).toBe('fake-refresh-token-xyz');

      // storage type key should NOT be set (it's removed for session mode)
      expect(localStorage.getItem(STORAGE_TYPE_KEY)).toBeNull();

      // Tokens should NOT be in localStorage
      expect(localStorage.getItem(ACCESS_TOKEN_KEY)).toBeNull();
      expect(localStorage.getItem(REFRESH_TOKEN_KEY)).toBeNull();
    });

    it('should allow token retrieval after remember-me login via TokenStorageService', () => {
      setupTestBed(null);

      component.form.setValue({
        email: 'maria@reinoeflor.com',
        password: 'SecurePass123',
        rememberMe: true,
      });

      component.onSubmit();

      // TokenStorageService should be able to retrieve the stored tokens
      expect(tokenStorage.getAccessToken()).toBe(fakeAccessToken);
      expect(tokenStorage.getRefreshToken()).toBe('fake-refresh-token-xyz');
      expect(tokenStorage.isAuthenticated()).toBe(true);
    });

    it('should allow token retrieval after session-only login via TokenStorageService', () => {
      setupTestBed(null);

      component.form.setValue({
        email: 'maria@reinoeflor.com',
        password: 'SecurePass123',
        rememberMe: false,
      });

      component.onSubmit();

      // TokenStorageService should be able to retrieve the stored tokens
      expect(tokenStorage.getAccessToken()).toBe(fakeAccessToken);
      expect(tokenStorage.getRefreshToken()).toBe('fake-refresh-token-xyz');
      expect(tokenStorage.isAuthenticated()).toBe(true);
    });
  });
});


/**
 * Integration tests for the token refresh cycle.
 * Uses REAL AuthService and TokenStorageService with mocked AuthPort and Router.
 *
 * Flow under test:
 * Timer fires at 80% of expiresIn → AuthService.refreshToken() → AuthPort.refresh()
 * → new tokens stored → signals updated (or failure → session cleared → redirect)
 *
 * Validates: Requirements 10.4, 5.3, 5.4
 */
describe('Integration: Token Refresh Cycle', () => {
  let authService: AuthService;
  let tokenStorage: TokenStorageService;
  let authPortMock: {
    login: ReturnType<typeof vi.fn>;
    register: ReturnType<typeof vi.fn>;
    activate: ReturnType<typeof vi.fn>;
    logout: ReturnType<typeof vi.fn>;
    refresh: ReturnType<typeof vi.fn>;
    getCurrentUser: ReturnType<typeof vi.fn>;
    recoverPassword: ReturnType<typeof vi.fn>;
  };
  let routerMock: { navigate: ReturnType<typeof vi.fn>; navigateByUrl: ReturnType<typeof vi.fn> };

  function createFakeJwt(payload: Record<string, unknown>): string {
    const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
    const body = btoa(JSON.stringify(payload));
    return `${header}.${body}.fakesignature`;
  }

  const userPayload = {
    sub: 'uuid-integration-001',
    username: 'integration@reinoflor.com',
    role: 'ROLE_CASHIER',
    active: true,
    exp: Math.floor(Date.now() / 1000) + 3600,
  };

  const initialAccessToken = createFakeJwt(userPayload);

  const initialTokenPair: TokenPair = {
    accessToken: initialAccessToken,
    refreshToken: 'initial-refresh-token-abc',
    expiresIn: 100, // 100 seconds — refresh fires at 80s
  };

  beforeEach(() => {
    vi.useFakeTimers();
    localStorage.clear();
    sessionStorage.clear();

    authPortMock = {
      login: vi.fn(),
      register: vi.fn(),
      activate: vi.fn(),
      logout: vi.fn().mockReturnValue(of(void 0)),
      refresh: vi.fn(),
      getCurrentUser: vi.fn(),
      recoverPassword: vi.fn(),
    };

    routerMock = {
      navigate: vi.fn(),
      navigateByUrl: vi.fn(),
    };

    TestBed.configureTestingModule({
      providers: [
        AuthService,
        TokenStorageService, // REAL service
        { provide: AuthPort, useValue: authPortMock },
        { provide: Router, useValue: routerMock },
      ],
    });

    authService = TestBed.inject(AuthService);
    tokenStorage = TestBed.inject(TokenStorageService);
  });

  afterEach(() => {
    vi.useRealTimers();
    localStorage.clear();
    sessionStorage.clear();
  });

  describe('Successful token refresh cycle', () => {
    it('should refresh tokens after 80% of expiresIn and update storage and signals', () => {
      // Step 1: Login to establish session
      authPortMock.login.mockReturnValue(of(initialTokenPair));
      authService.login('integration@reinoflor.com', 'password123', false).subscribe();

      // Verify initial state — tokens stored in REAL storage
      expect(tokenStorage.getAccessToken()).toBe(initialAccessToken);
      expect(tokenStorage.getRefreshToken()).toBe('initial-refresh-token-abc');
      expect(authService.currentUser()).not.toBeNull();
      expect(authService.currentUser()!.uuid).toBe('uuid-integration-001');
      expect(authService.currentUser()!.role).toBe('ROLE_CASHIER');
      expect(authService.isAuthenticated()).toBe(true);

      // Step 2: Prepare new tokens for refresh response
      const refreshedPayload = {
        sub: 'uuid-integration-001',
        username: 'integration@reinoflor.com',
        role: 'ROLE_CASHIER',
        active: true,
        exp: Math.floor(Date.now() / 1000) + 7200,
      };
      const refreshedAccessToken = createFakeJwt(refreshedPayload);
      const refreshedTokenPair: TokenPair = {
        accessToken: refreshedAccessToken,
        refreshToken: 'refreshed-token-xyz',
        expiresIn: 200,
      };

      authPortMock.refresh.mockReturnValue(of(refreshedTokenPair));

      // Step 3: Advance timer to 80% of expiresIn (100 * 0.8 * 1000 = 80000ms)
      vi.advanceTimersByTime(80000);

      // Step 4: Verify refresh was called with correct refresh token
      expect(authPortMock.refresh).toHaveBeenCalledWith('initial-refresh-token-abc');

      // Step 5: Verify new tokens stored in REAL storage
      expect(tokenStorage.getAccessToken()).toBe(refreshedAccessToken);
      expect(tokenStorage.getRefreshToken()).toBe('refreshed-token-xyz');

      // Step 6: Verify signals updated
      expect(authService.currentUser()).not.toBeNull();
      expect(authService.currentUser()!.uuid).toBe('uuid-integration-001');
      expect(authService.isAuthenticated()).toBe(true);
      expect(authService.userRole()).toBe('ROLE_CASHIER');
    });

    it('should schedule another refresh after the first refresh succeeds', () => {
      // Login
      authPortMock.login.mockReturnValue(of(initialTokenPair));
      authService.login('integration@reinoflor.com', 'password123', false).subscribe();

      // Prepare first refresh response with expiresIn: 200
      const refreshedPayload = {
        sub: 'uuid-integration-001',
        username: 'integration@reinoflor.com',
        role: 'ROLE_CASHIER',
        active: true,
        exp: Math.floor(Date.now() / 1000) + 7200,
      };
      const refreshedAccessToken = createFakeJwt(refreshedPayload);
      const firstRefreshTokenPair: TokenPair = {
        accessToken: refreshedAccessToken,
        refreshToken: 'second-refresh-token',
        expiresIn: 200,
      };

      authPortMock.refresh.mockReturnValue(of(firstRefreshTokenPair));

      // Trigger first refresh at 80s
      vi.advanceTimersByTime(80000);
      expect(authPortMock.refresh).toHaveBeenCalledTimes(1);

      // Prepare second refresh response
      const secondRefreshPayload = {
        sub: 'uuid-integration-001',
        username: 'integration@reinoflor.com',
        role: 'ROLE_CASHIER',
        active: true,
        exp: Math.floor(Date.now() / 1000) + 14400,
      };
      const secondRefreshedAccessToken = createFakeJwt(secondRefreshPayload);
      const secondRefreshTokenPair: TokenPair = {
        accessToken: secondRefreshedAccessToken,
        refreshToken: 'third-refresh-token',
        expiresIn: 300,
      };

      authPortMock.refresh.mockReturnValue(of(secondRefreshTokenPair));

      // Advance timer to 80% of the new expiresIn (200 * 0.8 * 1000 = 160000ms)
      vi.advanceTimersByTime(160000);

      // Verify second refresh was called
      expect(authPortMock.refresh).toHaveBeenCalledTimes(2);
      expect(authPortMock.refresh).toHaveBeenLastCalledWith('second-refresh-token');

      // Verify final tokens in storage
      expect(tokenStorage.getAccessToken()).toBe(secondRefreshedAccessToken);
      expect(tokenStorage.getRefreshToken()).toBe('third-refresh-token');
    });
  });

  describe('Token refresh failure → session cleared → redirect to login', () => {
    it('should clear session and redirect to login with session_expired on refresh failure', () => {
      // Login to establish session
      authPortMock.login.mockReturnValue(of(initialTokenPair));
      authService.login('integration@reinoflor.com', 'password123', false).subscribe();

      // Verify session established
      expect(authService.isAuthenticated()).toBe(true);
      expect(tokenStorage.getAccessToken()).toBe(initialAccessToken);

      // Mock refresh to fail
      authPortMock.refresh.mockReturnValue(
        throwError(() => new Error('Refresh token expired')),
      );

      // Advance timer to trigger refresh (80% of 100s = 80000ms)
      vi.advanceTimersByTime(80000);

      // Verify refresh was attempted
      expect(authPortMock.refresh).toHaveBeenCalledWith('initial-refresh-token-abc');

      // Verify session was fully cleared from REAL storage
      expect(tokenStorage.getAccessToken()).toBeNull();
      expect(tokenStorage.getRefreshToken()).toBeNull();

      // Verify signals reset
      expect(authService.currentUser()).toBeNull();
      expect(authService.isAuthenticated()).toBe(false);
      expect(authService.userRole()).toBeNull();

      // Verify navigation to login with session_expired message
      expect(routerMock.navigate).toHaveBeenCalledWith(['/auth/login'], {
        queryParams: { message: 'session_expired' },
      });
    });

    it('should clear session when refresh token is missing from storage at refresh time', () => {
      // Login to establish session
      authPortMock.login.mockReturnValue(of(initialTokenPair));
      authService.login('integration@reinoflor.com', 'password123', false).subscribe();

      expect(authService.isAuthenticated()).toBe(true);

      // Simulate token being removed from storage externally
      tokenStorage.clear();

      // Advance timer to trigger refresh
      vi.advanceTimersByTime(80000);

      // Verify refresh endpoint was NOT called (no token to refresh with)
      expect(authPortMock.refresh).not.toHaveBeenCalled();

      // Verify signals reset
      expect(authService.currentUser()).toBeNull();
      expect(authService.isAuthenticated()).toBe(false);

      // Verify navigation to login with session_expired
      expect(routerMock.navigate).toHaveBeenCalledWith(['/auth/login'], {
        queryParams: { message: 'session_expired' },
      });
    });

    it('should not attempt refresh after logout cancels the timer', () => {
      // Login to establish session
      authPortMock.login.mockReturnValue(of(initialTokenPair));
      authService.login('integration@reinoflor.com', 'password123', false).subscribe();

      expect(authService.isAuthenticated()).toBe(true);

      // Logout before refresh timer fires
      authService.logout();

      // Verify session cleared immediately
      expect(authService.currentUser()).toBeNull();
      expect(authService.isAuthenticated()).toBe(false);
      expect(tokenStorage.getAccessToken()).toBeNull();

      // Advance timer past when refresh would have fired
      vi.advanceTimersByTime(80000);

      // Verify refresh was never called
      expect(authPortMock.refresh).not.toHaveBeenCalled();
    });
  });
});
