import { Observable } from 'rxjs';
import { LoginCredentials, TokenPair, User } from '../models';

/**
 * Port defining authentication operations.
 * Infrastructure layer provides the concrete implementation.
 */
export abstract class AuthPort {
  abstract login(credentials: LoginCredentials): Observable<TokenPair>;
  abstract refresh(refreshToken: string): Observable<TokenPair>;
  abstract logout(): Observable<void>;
  abstract getCurrentUser(): Observable<User | null>;
}
