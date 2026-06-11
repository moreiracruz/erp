import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthPort } from '../../core/ports';
import { LoginCredentials, TokenPair, User } from '../../core/models';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthHttpAdapter extends AuthPort {
  private readonly baseUrl = `${environment.apiUrl}/api/v1/auth`;

  constructor(private http: HttpClient) {
    super();
  }

  login(credentials: LoginCredentials): Observable<TokenPair> {
    return this.http.post<TokenPair>(`${this.baseUrl}/login`, credentials);
  }

  refresh(refreshToken: string): Observable<TokenPair> {
    return this.http.post<TokenPair>(`${this.baseUrl}/refresh`, { refreshToken });
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/logout`, {});
  }

  getCurrentUser(): Observable<User | null> {
    return this.http.get<User | null>(`${this.baseUrl}/me`);
  }
}
