import { describe, it, expect, vi, beforeEach } from 'vitest';
import { TestBed, ComponentFixture } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { signal } from '@angular/core';

import { LoginComponent } from './login.component';
import { AuthService } from '../services/auth.service';
import { UserRole } from '../../../core/models/user.model';

/**
 * Unit tests for LoginComponent navigation and remember-me behavior.
 *
 * Validates: Requirements 10.2, 1.1, 1.6, 6.2, 6.3
 */
describe('LoginComponent — Navigation and Remember-Me', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let authServiceMock: {
    login: ReturnType<typeof vi.fn>;
    getDefaultRouteForRole: ReturnType<typeof vi.fn>;
    userRole: ReturnType<typeof signal>;
  };
  let routerMock: { navigateByUrl: ReturnType<typeof vi.fn> };
  let activatedRouteMock: { snapshot: { queryParamMap: { get: ReturnType<typeof vi.fn> } } };

  function setupComponent(returnUrl: string | null = null) {
    authServiceMock = {
      login: vi.fn().mockReturnValue(of(void 0)),
      getDefaultRouteForRole: vi.fn().mockReturnValue('/dashboard'),
      userRole: signal<UserRole | null>('ROLE_MANAGER'),
    };

    routerMock = {
      navigateByUrl: vi.fn(),
    };

    activatedRouteMock = {
      snapshot: {
        queryParamMap: {
          get: vi.fn((key: string) => {
            if (key === 'returnUrl') return returnUrl;
            return null;
          }),
        },
      },
    };

    TestBed.configureTestingModule({
      imports: [LoginComponent, ReactiveFormsModule],
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        { provide: Router, useValue: routerMock },
        { provide: ActivatedRoute, useValue: activatedRouteMock },
      ],
    });

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  describe('navigation on successful login', () => {
    it('should navigate to returnUrl when returnUrl is present', () => {
      setupComponent('/dashboard/products');

      component.form.setValue({
        email: 'user@example.com',
        password: 'password123',
        rememberMe: false,
      });

      component.onSubmit();

      expect(routerMock.navigateByUrl).toHaveBeenCalledWith('/dashboard/products');
    });

    it('should navigate to default role route when no returnUrl is present', () => {
      setupComponent(null);
      authServiceMock.getDefaultRouteForRole.mockReturnValue('/dashboard');

      component.form.setValue({
        email: 'user@example.com',
        password: 'password123',
        rememberMe: false,
      });

      component.onSubmit();

      expect(authServiceMock.getDefaultRouteForRole).toHaveBeenCalledWith('ROLE_MANAGER');
      expect(routerMock.navigateByUrl).toHaveBeenCalledWith('/dashboard');
    });
  });

  describe('rememberMe flag passed to auth service', () => {
    it('should pass rememberMe=true to authService.login when checked', () => {
      setupComponent(null);

      component.form.setValue({
        email: 'user@example.com',
        password: 'password123',
        rememberMe: true,
      });

      component.onSubmit();

      expect(authServiceMock.login).toHaveBeenCalledWith(
        'user@example.com',
        'password123',
        true,
      );
    });

    it('should pass rememberMe=false to authService.login when unchecked', () => {
      setupComponent(null);

      component.form.setValue({
        email: 'user@example.com',
        password: 'password123',
        rememberMe: false,
      });

      component.onSubmit();

      expect(authServiceMock.login).toHaveBeenCalledWith(
        'user@example.com',
        'password123',
        false,
      );
    });
  });
});
