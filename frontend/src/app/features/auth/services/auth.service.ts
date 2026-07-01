import { computed, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, Observable, tap, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthPort } from '../../../core/ports';
import { RegisterData, TokenPair, User, UserRole } from '../../../core/models';
import { TokenStorageService } from '../../../infrastructure/storage/token-storage.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  readonly currentUser = signal<User | null>(null);
  readonly isAuthenticated = computed(() => !!this.currentUser());
  readonly userRole = computed(() => this.currentUser()?.role ?? null);

  private refreshTimerId: ReturnType<typeof setTimeout> | null = null;

  constructor(
    private authPort: AuthPort,
    private tokenStorage: TokenStorageService,
    private router: Router,
  ) {}

  login(email: string, password: string, rememberMe: boolean): Observable<void> {
    this.tokenStorage.setStorageType(rememberMe);

    return this.authPort.login({ username: email, password }).pipe(
      tap(tokens => this.handleTokens(tokens)),
      tap(() => void 0),
      catchError((err: HttpErrorResponse) => throwError(() => this.mapLoginError(err))),
      // Map TokenPair to void
      tap(() => {}),
    ) as unknown as Observable<void>;
  }

  register(data: RegisterData): Observable<void> {
    return this.authPort.register(data).pipe(
      tap(tokens => {
        this.tokenStorage.setStorageType(false);
        this.handleTokens(tokens);
      }),
      catchError((err: HttpErrorResponse) => throwError(() => this.mapRegisterError(err))),
    ) as unknown as Observable<void>;
  }

  logout(): void {
    this.authPort.logout().subscribe({ error: () => {} });
    this.clearSession();
    this.router.navigate(['/']);
  }

  recoverPassword(email: string): Observable<void> {
    return this.authPort.recoverPassword(email);
  }

  activate(token: string, password: string): Observable<void> {
    return this.authPort.activate(token, password).pipe(
      catchError((err: HttpErrorResponse) => throwError(() => this.mapActivationError(err))),
    );
  }

  initFromStorage(): void {
    const token = this.tokenStorage.getAccessToken();
    if (token) {
      const user = this.decodeToken(token);
      if (user) {
        this.currentUser.set(user);
        this.scheduleRefresh(this.getTokenRemainingTime(token));
      } else {
        this.clearSession();
      }
    }
  }

  getDefaultRouteForRole(role: UserRole | null): string {
    switch (role) {
      case 'ROLE_USER':
        return '/';
      case 'ROLE_SUPER_ADMIN':
        return '/admin/dashboard';
      case 'ROLE_MANAGER':
        return '/admin/dashboard';
      case 'ROLE_CASHIER':
        return '/pos';
      case 'ROLE_STOCK':
        return '/inventory';
      case 'ROLE_FINANCE':
        return '/dashboard';
      default:
        return '/';
    }
  }

  private handleTokens(tokens: TokenPair): void {
    this.tokenStorage.saveTokens(tokens.accessToken, tokens.refreshToken);
    const user = this.decodeToken(tokens.accessToken);
    if (user) {
      this.tokenStorage.saveUserRole(user.role);
      this.currentUser.set(user);
      this.scheduleRefresh(tokens.expiresIn);
    }
  }

  private scheduleRefresh(expiresInSeconds: number): void {
    if (this.refreshTimerId) {
      clearTimeout(this.refreshTimerId);
    }
    // Refresh at 80% of expiry time
    const refreshAt = expiresInSeconds * 0.8 * 1000;
    this.refreshTimerId = setTimeout(() => this.refreshToken(), refreshAt);
  }

  private refreshToken(): void {
    const refreshToken = this.tokenStorage.getRefreshToken();
    if (!refreshToken) {
      this.handleRefreshFailure();
      return;
    }

    this.authPort.refresh(refreshToken).subscribe({
      next: tokens => this.handleTokens(tokens),
      error: () => this.handleRefreshFailure(),
    });
  }

  private handleRefreshFailure(): void {
    this.clearSession();
    this.router.navigate(['/auth/login'], {
      queryParams: { message: 'session_expired' },
    });
  }

  private clearSession(): void {
    if (this.refreshTimerId) {
      clearTimeout(this.refreshTimerId);
      this.refreshTimerId = null;
    }
    this.tokenStorage.clear();
    this.currentUser.set(null);
  }

  private decodeToken(token: string): User | null {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return {
        uuid: payload.sub ?? payload.uuid ?? '',
        username: payload.username ?? payload.email ?? '',
        role: payload.role ?? 'ROLE_USER',
        active: payload.active ?? true,
      };
    } catch {
      return null;
    }
  }

  private getTokenRemainingTime(token: string): number {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const exp = payload.exp;
      if (!exp) return 300; // default 5 minutes
      const now = Math.floor(Date.now() / 1000);
      return Math.max(exp - now, 0);
    } catch {
      return 300;
    }
  }

  private mapLoginError(err: HttpErrorResponse): string {
    if (err.status === 0) {
      return 'Erro de conexão. Verifique sua internet e tente novamente.';
    }
    if (err.status === 401) {
      return 'E-mail ou senha inválidos';
    }
    if (err.status === 423) {
      const minutes = err.error?.remainingMinutes ?? 5;
      return `Conta bloqueada. Tente novamente em ${minutes} minutos`;
    }
    if (err.status === 429) {
      const minutes = err.error?.retryAfterMinutes ?? 5;
      return `Muitas tentativas. Tente novamente em ${minutes} minutos`;
    }
    return 'Ocorreu um erro inesperado. Tente novamente.';
  }

  private mapRegisterError(err: HttpErrorResponse): string {
    if (err.status === 0) {
      return 'Erro de conexão. Verifique sua internet e tente novamente.';
    }
    const backendMessage = String(err.error?.message ?? '').toLowerCase();
    if (err.status === 409 || (err.status === 422 && backendMessage.includes('cadastrado'))) {
      return 'Este e-mail já está cadastrado.';
    }
    return 'Ocorreu um erro inesperado. Tente novamente.';
  }

  private mapActivationError(err: HttpErrorResponse): string {
    if (err.status === 0) {
      return 'Erro de conexão. Verifique sua internet e tente novamente.';
    }
    if (err.status === 401 || err.status === 422) {
      return 'Token inválido, expirado ou senha inválida.';
    }
    return 'Ocorreu um erro inesperado. Tente novamente.';
  }
}
