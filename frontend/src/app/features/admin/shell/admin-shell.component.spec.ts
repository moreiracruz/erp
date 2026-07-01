import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { AuthService } from '../../auth/services/auth.service';
import { AdminShellComponent } from './admin-shell.component';

describe('AdminShellComponent', () => {
  let fixture: ComponentFixture<AdminShellComponent>;
  let authServiceMock: {
    currentUser: ReturnType<typeof signal>;
    logout: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    authServiceMock = {
      currentUser: signal({
        uuid: 'admin-uuid',
        username: 'admin@example.com',
        role: 'ROLE_SUPER_ADMIN',
        active: true,
      }),
      logout: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [AdminShellComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authServiceMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AdminShellComponent);
    fixture.detectChanges();
  });

  it('should render the administrative navigation', () => {
    const text = fixture.nativeElement.textContent;

    expect(text).toContain('Administracao');
    expect(text).toContain('Dashboard');
    expect(text).toContain('Produtos');
    expect(text).toContain('Usuarios');
    expect(text).toContain('Estoque');
    expect(text).toContain('PDV');
    expect(text).toContain('Consignacao');
  });

  it('should delegate logout to AuthService', () => {
    const button = fixture.nativeElement.querySelector('.admin-user__logout') as HTMLButtonElement;

    button.click();

    expect(authServiceMock.logout).toHaveBeenCalledOnce();
  });
});
