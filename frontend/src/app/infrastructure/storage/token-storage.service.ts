import { Injectable } from '@angular/core';

const ACCESS_TOKEN_KEY = 'rf_access_token';
const REFRESH_TOKEN_KEY = 'rf_refresh_token';
const USER_ROLE_KEY = 'rf_user_role';
const STORAGE_TYPE_KEY = 'rf_storage_type';

/**
 * Service responsible for securely storing authentication tokens.
 * Uses sessionStorage by default (cleared on tab close).
 * Supports localStorage when "remember me" is selected.
 */
@Injectable({ providedIn: 'root' })
export class TokenStorageService {

  private get storage(): Storage {
    return localStorage.getItem(STORAGE_TYPE_KEY) === 'local'
      ? localStorage
      : sessionStorage;
  }

  setStorageType(persistent: boolean): void {
    if (persistent) {
      localStorage.setItem(STORAGE_TYPE_KEY, 'local');
    } else {
      localStorage.removeItem(STORAGE_TYPE_KEY);
    }
  }

  saveTokens(accessToken: string, refreshToken: string): void {
    this.storage.setItem(ACCESS_TOKEN_KEY, accessToken);
    this.storage.setItem(REFRESH_TOKEN_KEY, refreshToken);
  }

  getAccessToken(): string | null {
    return this.storage.getItem(ACCESS_TOKEN_KEY)
      ?? localStorage.getItem(ACCESS_TOKEN_KEY)
      ?? sessionStorage.getItem(ACCESS_TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return this.storage.getItem(REFRESH_TOKEN_KEY)
      ?? localStorage.getItem(REFRESH_TOKEN_KEY)
      ?? sessionStorage.getItem(REFRESH_TOKEN_KEY);
  }

  saveUserRole(role: string): void {
    this.storage.setItem(USER_ROLE_KEY, role);
  }

  getUserRole(): string | null {
    return this.storage.getItem(USER_ROLE_KEY)
      ?? localStorage.getItem(USER_ROLE_KEY)
      ?? sessionStorage.getItem(USER_ROLE_KEY);
  }

  clear(): void {
    sessionStorage.removeItem(ACCESS_TOKEN_KEY);
    sessionStorage.removeItem(REFRESH_TOKEN_KEY);
    sessionStorage.removeItem(USER_ROLE_KEY);
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(USER_ROLE_KEY);
    localStorage.removeItem(STORAGE_TYPE_KEY);
  }

  isAuthenticated(): boolean {
    return !!this.getAccessToken();
  }
}
