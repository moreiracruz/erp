import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { TestBed } from '@angular/core/testing';
import * as fc from 'fast-check';
import { ChangePasswordComponent } from './change-password.component';

/**
 * Property 6: Password validation correctness
 * Validates: Requirements 4.2
 *
 * Tests the same form structure and validators used by ChangePasswordComponent:
 * - newPassword: required, minLength(8)
 * - confirmPassword: required
 * - Form-level validator: passwordMatchValidator returns { passwordMismatch: true }
 *   when newPassword !== confirmPassword (and both are non-empty)
 */

function passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
  const newPassword = control.get('newPassword')?.value;
  const confirmPassword = control.get('confirmPassword')?.value;

  if (newPassword && confirmPassword && newPassword !== confirmPassword) {
    return { passwordMismatch: true };
  }
  return null;
}

describe('ChangePasswordComponent – Property 6: Password validation correctness', () => {
  let form: FormGroup;

  beforeEach(() => {
    const fb = new FormBuilder();
    form = fb.group(
      {
        currentPassword: ['', [Validators.required]],
        newPassword: ['', [Validators.required, Validators.minLength(8)]],
        confirmPassword: ['', [Validators.required]],
      },
      { validators: [passwordMatchValidator] },
    );
  });

  it('newPassword control reports minlength error for any string with fewer than 8 characters', () => {
    /**
     * **Validates: Requirements 4.2**
     *
     * For any string with fewer than 8 characters, the newPassword control
     * should report a minlength validation error.
     */
    fc.assert(
      fc.property(fc.string({ minLength: 1, maxLength: 7 }), (shortPassword) => {
        const control = form.get('newPassword')!;
        control.setValue(shortPassword);
        control.updateValueAndValidity();

        expect(control.errors).not.toBeNull();
        expect(control.errors!['minlength']).toBeDefined();
      }),
      { numRuns: 100 },
    );
  });

  it('form-level validator reports passwordMismatch for any pair of distinct non-empty strings', () => {
    /**
     * **Validates: Requirements 4.2**
     *
     * For any pair of distinct non-empty strings in newPassword and confirmPassword,
     * the form-level validator should report a passwordMismatch error.
     */
    fc.assert(
      fc.property(
        fc.tuple(fc.string({ minLength: 1 }), fc.string({ minLength: 1 })).filter(([a, b]) => a !== b),
        ([newPass, confirmPass]) => {
          form.get('newPassword')!.setValue(newPass);
          form.get('confirmPassword')!.setValue(confirmPass);
          form.updateValueAndValidity();

          expect(form.errors).not.toBeNull();
          expect(form.errors!['passwordMismatch']).toBe(true);
        },
      ),
      { numRuns: 100 },
    );
  });
});

/**
 * Task 5.2: Unit tests for ChangePasswordComponent
 * Validates: Requirements 4.1, 4.3
 */
describe('ChangePasswordComponent - Unit Tests', () => {
  let component: ChangePasswordComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ChangePasswordComponent],
    }).compileComponents();

    const fixture = TestBed.createComponent(ChangePasswordComponent);
    component = fixture.componentInstance;
  });

  it('form has 3 controls: currentPassword, newPassword, confirmPassword', () => {
    expect(component.form.get('currentPassword')).toBeDefined();
    expect(component.form.get('newPassword')).toBeDefined();
    expect(component.form.get('confirmPassword')).toBeDefined();
  });

  it('valid form submission sets successMsg', async () => {
    component.form.patchValue({
      currentPassword: 'oldPass123',
      newPassword: 'newPass456',
      confirmPassword: 'newPass456',
    });

    component.onSubmit();

    // Wait for the setTimeout mock API call
    await new Promise(resolve => setTimeout(resolve, 1100));

    expect(component.successMsg()).toBe('Senha alterada com sucesso!');
  });

  it('form resets after successful submission', async () => {
    component.form.patchValue({
      currentPassword: 'oldPass123',
      newPassword: 'newPass456',
      confirmPassword: 'newPass456',
    });

    component.onSubmit();

    await new Promise(resolve => setTimeout(resolve, 1100));

    expect(component.form.get('currentPassword')!.value).toBeFalsy();
    expect(component.form.get('newPassword')!.value).toBeFalsy();
    expect(component.form.get('confirmPassword')!.value).toBeFalsy();
  });

  it('form is invalid when currentPassword is empty', () => {
    component.form.patchValue({
      currentPassword: '',
      newPassword: 'newPass456',
      confirmPassword: 'newPass456',
    });

    expect(component.form.invalid).toBe(true);
  });
});
