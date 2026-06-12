import { Component, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-account-layout',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './account-layout.component.html',
  styleUrls: ['./account-layout.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AccountLayoutComponent {
  readonly navItems = [
    { label: 'Meu Perfil', path: 'profile', icon: '👤' },
    { label: 'Meus Pedidos', path: 'orders', icon: '📦' },
    { label: 'Endereços', path: 'addresses', icon: '📍' },
    { label: 'Alterar Senha', path: 'password', icon: '🔒' },
  ];
}
