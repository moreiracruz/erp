import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { AuthPort } from '../../core/ports';
import { LoginCredentials, RegisterData, TokenPair, User } from '../../core/models';
import { environment } from '../../../environments/environment';
import { TokenStorageService } from '../storage/token-storage.service';

@Injectable({ providedIn: 'root' })
export class AuthHttpAdapter extends AuthPort {
  private readonly baseUrl = `${environment.apiUrl}/api/v1/auth`;

  constructor(
    private http: HttpClient,
    private tokenStorage: TokenStorageService,
  ) {
    super();
  }

  login(credentials: LoginCredentials): Observable<TokenPair> {
    return this.http.post<TokenPair>(`${this.baseUrl}/login`, credentials);
  }

  register(data: RegisterData): Observable<TokenPair> {
    return this.http.post<TokenPair>(`${this.baseUrl}/register`, data);
  }

  refresh(refreshToken: string): Observable<TokenPair> {
    return this.http.post<TokenPair>(`${this.baseUrl}/refresh`, { refreshToken });
  }

  logout(): Observable<void> {
    const refreshToken = this.tokenStorage.getRefreshToken();
    if (!refreshToken) {
      return of(undefined);
    }
    return this.http.post<void>(`${this.baseUrl}/logout`, { refreshToken });
  }

  getCurrentUser(): Observable<User | null> {
    return this.http.get<User | null>(`${this.baseUrl}/me`);
  }

  recoverPassword(email: string): Observable<void> {
    // Mock: backend recover-password endpoint not ready yet
    return of(undefined);
  }
}
