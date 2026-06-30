// @vitest-environment jsdom
import { describe, it, expect, beforeEach } from 'vitest';
import * as fc from 'fast-check';
import { TokenStorageService } from './token-storage.service';

/**
 * Property 2: Token storage strategy respects remember-me preference
 *
 * For any pair of non-empty token strings (access, refresh), if setStorageType(true)
 * is called before saveTokens, then localStorage SHALL contain both tokens and
 * sessionStorage SHALL NOT; conversely, if setStorageType(false) is called, then
 * sessionStorage SHALL contain both tokens and localStorage SHALL NOT.
 *
 * **Validates: Requirements 1.6**
 */
describe('Feature: frontend-auth, Property 2: Token storage strategy respects remember-me preference', () => {
  let service: TokenStorageService;

  beforeEach(() => {
    service = new TokenStorageService();
    service.clear();
  });

  it('localStorage contains tokens when rememberMe=true, sessionStorage does not; and vice-versa', () => {
    fc.assert(
      fc.property(
        fc.tuple(
          fc.string({ minLength: 1 }),
          fc.string({ minLength: 1 }),
          fc.boolean()
        ),
        ([accessToken, refreshToken, rememberMe]) => {
          // Arrange: clear state and set storage preference
          service.clear();
          service.setStorageType(rememberMe);

          // Act: save tokens
          service.saveTokens(accessToken, refreshToken);

          // Assert: verify storage placement based on rememberMe
          if (rememberMe) {
            // localStorage should contain both tokens
            expect(localStorage.getItem('rf_access_token')).toBe(accessToken);
            expect(localStorage.getItem('rf_refresh_token')).toBe(refreshToken);
            // sessionStorage should NOT contain tokens
            expect(sessionStorage.getItem('rf_access_token')).toBeNull();
            expect(sessionStorage.getItem('rf_refresh_token')).toBeNull();
          } else {
            // sessionStorage should contain both tokens
            expect(sessionStorage.getItem('rf_access_token')).toBe(accessToken);
            expect(sessionStorage.getItem('rf_refresh_token')).toBe(refreshToken);
            // localStorage should NOT contain tokens (storage type key is allowed)
            expect(localStorage.getItem('rf_access_token')).toBeNull();
            expect(localStorage.getItem('rf_refresh_token')).toBeNull();
          }
        }
      ),
      { numRuns: 100 }
    );
  });
});
