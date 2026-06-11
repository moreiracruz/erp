import { Injectable } from '@angular/core';

const ACCESS_TOKEN_KEY = 'rf_access_token';
const REFRESH_TOKEN_KEY = 'rf_refresh_token';
const USER_ROLE_KEY = 'rf_user_role';

/**
 * Service responsible for securely storing authentication tokens.
 * Uses sessionStorage by default (cleared on tab close).
 */
@Injectable({ providedIn: 'root' })
export class TokenStorageService {

  saveTokens(accessToken: string, refreshToken: string): void {
    sessionStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
    sessionStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
  }

  getAccessToken(): string | null {
    return sessionStorage.getItem(ACCESS_TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return sessionStorage.getItem(REFRESH_TOKEN_KEY);
  }

  saveUserRole(role: string): void {
    sessionStorage.setItem(USER_ROLE_KEY, role);
  }

  getUserRole(): string | null {
    return sessionStorage.getItem(USER_ROLE_KEY);
  }

  clear(): void {
    sessionStorage.removeItem(ACCESS_TOKEN_KEY);
    sessionStorage.removeItem(REFRESH_TOKEN_KEY);
    sessionStorage.removeItem(USER_ROLE_KEY);
  }

  isAuthenticated(): boolean {
    return !!this.getAccessToken();
  }
}
