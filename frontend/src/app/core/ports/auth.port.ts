import { Observable } from 'rxjs';
import { LoginCredentials, RegisterData, TokenPair, User } from '../models';

/**
 * Port defining authentication operations.
 * Infrastructure layer provides the concrete implementation.
 */
export abstract class AuthPort {
  abstract login(credentials: LoginCredentials): Observable<TokenPair>;
  abstract register(data: RegisterData): Observable<TokenPair>;
  abstract activate(token: string, password: string): Observable<void>;
  abstract refresh(refreshToken: string): Observable<TokenPair>;
  abstract logout(): Observable<void>;
  abstract getCurrentUser(): Observable<User | null>;
  abstract recoverPassword(email: string): Observable<void>;
}
