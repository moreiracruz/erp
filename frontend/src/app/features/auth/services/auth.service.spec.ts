import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { AuthService } from './auth.service';
import { AuthPort } from '../../../core/ports/auth.port';
import { TokenPair } from '../../../core/models/user.model';
import { TokenStorageService } from '../../../infrastructure/storage/token-storage.service';

/**
 * Unit tests for AuthService — Token refresh and initFromStorage.
 *
 * Validates: Requirements 10.1, 5.3, 5.4
 */
describe('AuthService — Token refresh success and failure', () => {
  let authService: AuthService;
  let authPortMock: {
    login: ReturnType<typeof vi.fn>;
    logout: ReturnType<typeof vi.fn>;
    refresh: ReturnType<typeof vi.fn>;
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

  function createFakeJwt(payload: Record<string, unknown>): string {
    const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
    const body = btoa(JSON.stringify(payload));
    const signature = 'fakesignature';
    return `${header}.${body}.${signature}`;
  }

  const validPayload = {
    sub: 'user-uuid-123',
    username: 'user@example.com',
    role: 'ROLE_MANAGER',
    active: true,
    exp: Math.floor(Date.now() / 1000) + 3600,
  };

  const validAccessToken = createFakeJwt(validPayload);

  beforeEach(() => {
    vi.useFakeTimers();

    authPortMock = {
      login: vi.fn(),
      logout: vi.fn().mockReturnValue(of(void 0)),
      refresh: vi.fn(),
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

  afterEach(() => {
    vi.useRealTimers();
  });

  describe('Token refresh success', () => {
    it('should save new tokens and update signals when refresh succeeds', () => {
      // First login to establish a session and schedule refresh
      const initialTokenPair: TokenPair = {
        accessToken: validAccessToken,
        refreshToken: 'initial-refresh-token',
        expiresIn: 3600,
      };

      authPortMock.login.mockReturnValue(of(initialTokenPair));
      authService.login('user@example.com', 'password123', false).subscribe();

      // Verify initial state
      expect(authService.currentUser()).not.toBeNull();
      expect(authService.currentUser()!.uuid).toBe('user-uuid-123');

      // Set up refresh mock with new tokens
      const newPayload = {
        sub: 'user-uuid-123',
        username: 'user@example.com',
        role: 'ROLE_FINANCE',
        active: true,
        exp: Math.floor(Date.now() / 1000) + 7200,
      };
      const newAccessToken = createFakeJwt(newPayload);
      const newTokenPair: TokenPair = {
        accessToken: newAccessToken,
        refreshToken: 'new-refresh-token',
        expiresIn: 7200,
      };

      tokenStorageMock.getRefreshToken.mockReturnValue('initial-refresh-token');
      authPortMock.refresh.mockReturnValue(of(newTokenPair));

      // Advance timer to 80% of expiresIn (3600 * 0.8 * 1000 = 2880000ms)
      vi.advanceTimersByTime(3600 * 0.8 * 1000);

      // Verify refresh was called with the stored refresh token
      expect(authPortMock.refresh).toHaveBeenCalledWith('initial-refresh-token');

      // Verify new tokens were saved
      expect(tokenStorageMock.saveTokens).toHaveBeenCalledWith(
        newAccessToken,
        'new-refresh-token',
      );

      // Verify signals were updated with new user data
      expect(authService.currentUser()).not.toBeNull();
      expect(authService.currentUser()!.role).toBe('ROLE_FINANCE');
      expect(authService.isAuthenticated()).toBe(true);
      expect(authService.userRole()).toBe('ROLE_FINANCE');
    });
  });

  describe('Token refresh failure', () => {
    it('should clear session and navigate to login with session_expired when refresh fails', () => {
      // First login to establish a session
      const initialTokenPair: TokenPair = {
        accessToken: validAccessToken,
        refreshToken: 'initial-refresh-token',
        expiresIn: 3600,
      };

      authPortMock.login.mockReturnValue(of(initialTokenPair));
      authService.login('user@example.com', 'password123', false).subscribe();

      // Verify session is established
      expect(authService.isAuthenticated()).toBe(true);

      // Set up refresh to fail
      tokenStorageMock.getRefreshToken.mockReturnValue('initial-refresh-token');
      authPortMock.refresh.mockReturnValue(
        throwError(() => new Error('Token expired')),
      );

      // Advance timer to trigger refresh
      vi.advanceTimersByTime(3600 * 0.8 * 1000);

      // Verify refresh was attempted
      expect(authPortMock.refresh).toHaveBeenCalledWith('initial-refresh-token');

      // Verify session was cleared
      expect(tokenStorageMock.clear).toHaveBeenCalled();
      expect(authService.currentUser()).toBeNull();
      expect(authService.isAuthenticated()).toBe(false);
      expect(authService.userRole()).toBeNull();

      // Verify navigation to login with session_expired message
      expect(routerMock.navigate).toHaveBeenCalledWith(['/auth/login'], {
        queryParams: { message: 'session_expired' },
      });
    });

    it('should handle refresh failure when no refresh token is in storage', () => {
      // First login to establish a session
      const initialTokenPair: TokenPair = {
        accessToken: validAccessToken,
        refreshToken: 'initial-refresh-token',
        expiresIn: 3600,
      };

      authPortMock.login.mockReturnValue(of(initialTokenPair));
      authService.login('user@example.com', 'password123', false).subscribe();

      // Simulate no refresh token in storage (e.g., manually cleared)
      tokenStorageMock.getRefreshToken.mockReturnValue(null);

      // Advance timer to trigger refresh
      vi.advanceTimersByTime(3600 * 0.8 * 1000);

      // Verify session was cleared without calling refresh endpoint
      expect(authPortMock.refresh).not.toHaveBeenCalled();
      expect(tokenStorageMock.clear).toHaveBeenCalled();
      expect(authService.currentUser()).toBeNull();

      // Verify navigation to login with session_expired message
      expect(routerMock.navigate).toHaveBeenCalledWith(['/auth/login'], {
        queryParams: { message: 'session_expired' },
      });
    });
  });

  describe('initFromStorage with valid token', () => {
    it('should populate signals when a valid JWT is found in storage', () => {
      tokenStorageMock.getAccessToken.mockReturnValue(validAccessToken);

      authService.initFromStorage();

      // Verify signals are populated
      expect(authService.currentUser()).not.toBeNull();
      expect(authService.currentUser()!.uuid).toBe('user-uuid-123');
      expect(authService.currentUser()!.username).toBe('user@example.com');
      expect(authService.currentUser()!.role).toBe('ROLE_MANAGER');
      expect(authService.currentUser()!.active).toBe(true);
      expect(authService.isAuthenticated()).toBe(true);
      expect(authService.userRole()).toBe('ROLE_MANAGER');
    });

    it('should schedule a refresh based on remaining token time', () => {
      tokenStorageMock.getAccessToken.mockReturnValue(validAccessToken);

      const setTimeoutSpy = vi.spyOn(globalThis, 'setTimeout');
      authService.initFromStorage();

      // Verify setTimeout was called (scheduleRefresh)
      expect(setTimeoutSpy).toHaveBeenCalled();

      setTimeoutSpy.mockRestore();
    });
  });

  describe('initFromStorage with invalid/malformed token', () => {
    it('should clear session when token is not valid base64', () => {
      tokenStorageMock.getAccessToken.mockReturnValue('not-a-valid-jwt');

      authService.initFromStorage();

      // Verify session was cleared
      expect(tokenStorageMock.clear).toHaveBeenCalled();
      expect(authService.currentUser()).toBeNull();
      expect(authService.isAuthenticated()).toBe(false);
    });

    it('should clear session when token payload is malformed JSON', () => {
      // Create a token with invalid base64 payload
      const header = btoa(JSON.stringify({ alg: 'HS256' }));
      const malformedToken = `${header}.not-valid-base64!!!.signature`;
      tokenStorageMock.getAccessToken.mockReturnValue(malformedToken);

      authService.initFromStorage();

      // Verify session was cleared
      expect(tokenStorageMock.clear).toHaveBeenCalled();
      expect(authService.currentUser()).toBeNull();
      expect(authService.isAuthenticated()).toBe(false);
    });

    it('should not clear session when no token exists in storage', () => {
      tokenStorageMock.getAccessToken.mockReturnValue(null);

      authService.initFromStorage();

      // No token → no action, no session clear
      expect(tokenStorageMock.clear).not.toHaveBeenCalled();
      expect(authService.currentUser()).toBeNull();
    });
  });
});
