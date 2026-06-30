import { describe, it, expect, vi, beforeEach } from 'vitest';
import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideRouter, Router, ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';

import { RegisterComponent } from './register.component';
import { AuthService } from '../services/auth.service';

describe('RegisterComponent — Password Strength and Validation', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let authServiceMock: {
    register: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    authServiceMock = {
      register: vi.fn().mockReturnValue(of(void 0)),
    };

    await TestBed.configureTestingModule({
      imports: [RegisterComponent],
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        provideRouter([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  describe('Password strength indicator', () => {
    it('should classify "abc" as weak (score=0, no criteria met)', () => {
      component.form.get('password')?.setValue('abc');
      component.onPasswordInput();

      expect(component.passwordStrength()).toBe('weak');
    });

    it('should classify "abcdefgh" as weak (score=1, only length>=8)', () => {
      component.form.get('password')?.setValue('abcdefgh');
      component.onPasswordInput();

      expect(component.passwordStrength()).toBe('weak');
    });

    it('should classify "Abcdefgh1" as medium (score=3: length>=8, mixed case, digit)', () => {
      component.form.get('password')?.setValue('Abcdefgh1');
      component.onPasswordInput();

      expect(component.passwordStrength()).toBe('medium');
    });

    it('should classify "Abcdefghij1!" as strong (score=5: length>=8, length>=12, mixed case, digit, special)', () => {
      component.form.get('password')?.setValue('Abcdefghij1!');
      component.onPasswordInput();

      expect(component.passwordStrength()).toBe('strong');
    });

    it('should return null for empty password', () => {
      component.form.get('password')?.setValue('');
      component.onPasswordInput();

      expect(component.passwordStrength()).toBeNull();
    });
  });

  describe('Confirm password mismatch', () => {
    it('should set passwordMismatch error when passwords do not match', () => {
      component.form.get('password')?.setValue('test1234');
      component.form.get('confirmPassword')?.setValue('different');
      // Trigger the group validator
      component.form.updateValueAndValidity();

      expect(component.form.hasError('passwordMismatch')).toBe(true);
      expect(component.form.get('confirmPassword')?.hasError('passwordMismatch')).toBe(true);
    });

    it('should not have passwordMismatch error when passwords match', () => {
      component.form.get('password')?.setValue('test1234');
      component.form.get('confirmPassword')?.setValue('test1234');
      component.form.updateValueAndValidity();

      expect(component.form.hasError('passwordMismatch')).toBe(false);
    });
  });

  describe('Terms checkbox required for submission', () => {
    it('should mark form as invalid when termsAccepted is false', () => {
      // Fill all required fields with valid values
      component.form.get('fullName')?.setValue('Test User');
      component.form.get('email')?.setValue('test@example.com');
      component.form.get('password')?.setValue('password123');
      component.form.get('confirmPassword')?.setValue('password123');
      component.form.get('phone')?.setValue('11999999999');
      component.form.get('termsAccepted')?.setValue(false);
      component.form.updateValueAndValidity();

      expect(component.form.invalid).toBe(true);
    });

    it('should mark form as valid when termsAccepted is true and all fields are valid', () => {
      component.form.get('fullName')?.setValue('Test User');
      component.form.get('email')?.setValue('test@example.com');
      component.form.get('password')?.setValue('password123');
      component.form.get('confirmPassword')?.setValue('password123');
      component.form.get('phone')?.setValue('11999999999');
      component.form.get('termsAccepted')?.setValue(true);
      component.form.updateValueAndValidity();

      expect(component.form.valid).toBe(true);
    });

    it('should not call register when form is invalid due to unchecked terms', () => {
      component.form.get('fullName')?.setValue('Test User');
      component.form.get('email')?.setValue('test@example.com');
      component.form.get('password')?.setValue('password123');
      component.form.get('confirmPassword')?.setValue('password123');
      component.form.get('phone')?.setValue('11999999999');
      component.form.get('termsAccepted')?.setValue(false);
      component.form.updateValueAndValidity();

      component.onSubmit();

      expect(authServiceMock.register).not.toHaveBeenCalled();
    });
  });
});


describe('RegisterComponent — Submission and error handling', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let authServiceMock: { register: ReturnType<typeof vi.fn> };
  let routerMock: { navigate: ReturnType<typeof vi.fn> };

  function fillValidForm(): void {
    component.form.get('fullName')?.setValue('Test User');
    component.form.get('email')?.setValue('test@example.com');
    component.form.get('password')?.setValue('Password1!');
    component.form.get('confirmPassword')?.setValue('Password1!');
    component.form.get('phone')?.setValue('11999999999');
    component.form.get('termsAccepted')?.setValue(true);
    component.form.updateValueAndValidity();
  }

  beforeEach(async () => {
    authServiceMock = {
      register: vi.fn().mockReturnValue(of(void 0)),
    };

    routerMock = {
      navigate: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [RegisterComponent],
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        { provide: Router, useValue: routerMock },
        { provide: ActivatedRoute, useValue: { snapshot: { queryParamMap: { get: () => null } } } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should navigate to "/" on successful registration', () => {
    authServiceMock.register.mockReturnValue(of(void 0));
    fillValidForm();

    component.onSubmit();

    expect(authServiceMock.register).toHaveBeenCalledWith({
      fullName: 'Test User',
      email: 'test@example.com',
      password: 'Password1!',
      phone: '11999999999',
      cpf: undefined,
    });
    expect(routerMock.navigate).toHaveBeenCalledWith(['/']);
    expect(component.loading()).toBe(false);
  });

  it('should display duplicate email error message on 409 conflict', () => {
    authServiceMock.register.mockReturnValue(
      throwError(() => 'Este e-mail já está cadastrado.')
    );
    fillValidForm();

    component.onSubmit();

    expect(component.errorMessage()).toBe('Este e-mail já está cadastrado.');
    expect(component.loading()).toBe(false);
  });

  it('should display connection error message on network failure', () => {
    authServiceMock.register.mockReturnValue(
      throwError(() => 'Erro de conexão. Verifique sua internet e tente novamente.')
    );
    fillValidForm();

    component.onSubmit();

    expect(component.errorMessage()).toBe('Erro de conexão. Verifique sua internet e tente novamente.');
    expect(component.loading()).toBe(false);
  });
});
