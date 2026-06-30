import { ChangeDetectionStrategy, Component, OnInit, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AdminUser, SystemAdminHttpAdapter } from '../../../infrastructure/http/system-admin-http.adapter';
import { UserRole } from '../../../core/models/user.model';

@Component({
  selector: 'app-system-admin',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './system-admin.component.html',
  styleUrl: './system-admin.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SystemAdminComponent implements OnInit {
  readonly roles: UserRole[] = ['ROLE_USER', 'ROLE_SUPER_ADMIN', 'ROLE_MANAGER', 'ROLE_CASHIER', 'ROLE_STOCK', 'ROLE_FINANCE'];
  readonly users = signal<AdminUser[]>([]);
  readonly searchQuery = signal('');
  readonly loading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly showCreateForm = signal(false);
  readonly selectedUser = signal<AdminUser | null>(null);
  readonly passwordReset = signal('');

  readonly createForm = signal<{ username: string; password: string; role: UserRole }>({
    username: '',
    password: '',
    role: 'ROLE_CASHIER',
  });

  readonly filteredUsers = computed(() => {
    const query = this.searchQuery().trim().toLowerCase();
    if (!query) return this.users();
    return this.users().filter((user) =>
      user.username.toLowerCase().includes(query) ||
      user.role.toLowerCase().includes(query),
    );
  });

  constructor(private readonly systemAdmin: SystemAdminHttpAdapter) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.systemAdmin.listUsers().subscribe({
      next: (users) => {
        this.users.set(users);
        this.loading.set(false);
      },
      error: () => this.handleError('Não foi possível carregar usuários.'),
    });
  }

  openCreateForm(): void {
    this.createForm.set({ username: '', password: '', role: 'ROLE_CASHIER' });
    this.showCreateForm.set(true);
  }

  closeCreateForm(): void {
    this.showCreateForm.set(false);
  }

  createUser(): void {
    const form = this.createForm();
    if (!form.username.trim() || form.password.length < 8) return;
    this.loading.set(true);
    this.errorMessage.set(null);
    this.systemAdmin.createUser({
      username: form.username.trim(),
      password: form.password,
      role: form.role,
    }).subscribe({
      next: (created) => {
        this.users.set([...this.users(), created].sort((a, b) => a.username.localeCompare(b.username)));
        this.loading.set(false);
        this.closeCreateForm();
      },
      error: () => this.handleError('Não foi possível criar usuário.'),
    });
  }

  selectUser(user: AdminUser): void {
    this.selectedUser.set(user);
    this.passwordReset.set('');
  }

  closeDetails(): void {
    this.selectedUser.set(null);
    this.passwordReset.set('');
  }

  updateRole(user: AdminUser, role: UserRole): void {
    if (user.role === role) return;
    this.loading.set(true);
    this.errorMessage.set(null);
    this.systemAdmin.updateRole(user.uuid, role).subscribe({
      next: (updated) => this.applyUserUpdate(updated),
      error: () => this.handleError('Não foi possível alterar perfil.'),
    });
  }

  toggleActive(user: AdminUser): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    const request = user.active ? this.systemAdmin.deactivate(user.uuid) : this.systemAdmin.activate(user.uuid);
    request.subscribe({
      next: (updated) => this.applyUserUpdate(updated),
      error: () => this.handleError('Não foi possível alterar status do usuário.'),
    });
  }

  unlock(user: AdminUser): void {
    this.loading.set(true);
    this.errorMessage.set(null);
    this.systemAdmin.unlock(user.uuid).subscribe({
      next: (updated) => this.applyUserUpdate(updated),
      error: () => this.handleError('Não foi possível desbloquear usuário.'),
    });
  }

  resetPassword(user: AdminUser): void {
    const password = this.passwordReset();
    if (password.length < 8) return;
    this.loading.set(true);
    this.errorMessage.set(null);
    this.systemAdmin.resetPassword(user.uuid, password).subscribe({
      next: (updated) => {
        this.passwordReset.set('');
        this.applyUserUpdate(updated);
      },
      error: () => this.handleError('Não foi possível redefinir senha.'),
    });
  }

  isLocked(user: AdminUser): boolean {
    return !!user.lockedUntil && new Date(user.lockedUntil).getTime() > Date.now();
  }

  roleLabel(role: UserRole): string {
    return role.replace('ROLE_', '');
  }

  private applyUserUpdate(updated: AdminUser): void {
    this.users.set(this.users().map((user) => user.uuid === updated.uuid ? updated : user));
    if (this.selectedUser()?.uuid === updated.uuid) {
      this.selectedUser.set(updated);
    }
    this.loading.set(false);
  }

  private handleError(message: string): void {
    this.errorMessage.set(message);
    this.loading.set(false);
  }
}
