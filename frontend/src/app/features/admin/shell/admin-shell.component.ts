import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { AuthService } from '../../auth/services/auth.service';
import { UserRole } from '../../../core/models';

type AdminNavItem = {
  label: string;
  description: string;
  route: string;
  externalSection?: boolean;
};

@Component({
  selector: 'app-admin-shell',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './admin-shell.component.html',
  styleUrl: './admin-shell.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminShellComponent {
  private readonly authService = inject(AuthService);

  readonly user = this.authService.currentUser;
  readonly userDisplayName = computed(() => this.user()?.username || 'Usuario administrativo');
  readonly userRoleLabel = computed(() => this.getRoleLabel(this.user()?.role ?? null));

  readonly primaryNavItems: AdminNavItem[] = [
    { label: 'Dashboard', description: 'Indicadores e alertas', route: '/admin/dashboard' },
    { label: 'Produtos', description: 'Catalogo e imagens', route: '/admin/products' },
    { label: 'Usuarios', description: 'Equipe e acessos', route: '/admin/system' },
  ];

  readonly operationNavItems: AdminNavItem[] = [
    { label: 'Estoque', description: 'Movimentacoes e saldos', route: '/inventory', externalSection: true },
    { label: 'PDV', description: 'Venda presencial', route: '/pos', externalSection: true },
    { label: 'Consignacao', description: 'Romaneios e acertos', route: '/consignments', externalSection: true },
  ];

  logout(): void {
    this.authService.logout();
  }

  private getRoleLabel(role: UserRole | null): string {
    switch (role) {
      case 'ROLE_SUPER_ADMIN':
        return 'Super administrador';
      case 'ROLE_MANAGER':
        return 'Gerente';
      case 'ROLE_FINANCE':
        return 'Financeiro';
      case 'ROLE_STOCK':
        return 'Estoque';
      case 'ROLE_CASHIER':
        return 'Caixa';
      case 'ROLE_USER':
        return 'Cliente';
      default:
        return 'Perfil administrativo';
    }
  }
}
