import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { UserRole } from '../../core/models/user.model';

export interface AdminUser {
  uuid: string;
  username: string;
  role: UserRole;
  status: string;
  active: boolean;
  failedAttempts: number;
  lockedUntil: string | null;
  createdAt: string;
}

export interface CreateAdminUserCommand {
  username: string;
  password: string;
  role: UserRole;
}

@Injectable({ providedIn: 'root' })
export class SystemAdminHttpAdapter {
  private readonly baseUrl = `${environment.apiUrl}/api/v1/system/users`;

  constructor(private readonly http: HttpClient) {}

  listUsers(): Observable<AdminUser[]> {
    return this.http.get<AdminUser[]>(this.baseUrl);
  }

  createUser(command: CreateAdminUserCommand): Observable<AdminUser> {
    return this.http.post<AdminUser>(this.baseUrl, command);
  }

  updateRole(uuid: string, role: UserRole): Observable<AdminUser> {
    return this.http.put<AdminUser>(`${this.baseUrl}/${uuid}/role`, { role });
  }

  resetPassword(uuid: string, password: string): Observable<AdminUser> {
    return this.http.put<AdminUser>(`${this.baseUrl}/${uuid}/password`, { password });
  }

  activate(uuid: string): Observable<AdminUser> {
    return this.http.post<AdminUser>(`${this.baseUrl}/${uuid}/activate`, {});
  }

  deactivate(uuid: string): Observable<AdminUser> {
    return this.http.post<AdminUser>(`${this.baseUrl}/${uuid}/deactivate`, {});
  }

  unlock(uuid: string): Observable<AdminUser> {
    return this.http.post<AdminUser>(`${this.baseUrl}/${uuid}/unlock`, {});
  }
}
