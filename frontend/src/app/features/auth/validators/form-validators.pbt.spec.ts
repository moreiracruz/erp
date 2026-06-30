import { describe, it, expect } from 'vitest';
import { AbstractControl, FormControl, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import * as fc from 'fast-check';

/**
 * Property-Based Tests for email validation.
 *
 * **Validates: Requirements 2.1, 4.2**
 */
describe('Form Validators — Property 3: Email validation rejects invalid formats', () => {
  function createEmailControl(value: string): FormControl {
    const control = new FormControl(value, [Validators.required, Validators.email]);
    control.markAsTouched();
    return control;
  }

  it('should mark valid emails as valid', () => {
    fc.assert(
      fc.property(fc.emailAddress(), (email) => {
        const control = createEmailControl(email);
        expect(control.valid).toBe(true);
        expect(control.errors).toBeNull();
      }),
      { numRuns: 100 },
    );
  });

  it('should mark empty string as invalid (required fails)', () => {
    fc.assert(
      fc.property(fc.constant(''), (emptyValue) => {
        const control = createEmailControl(emptyValue);
        expect(control.valid).toBe(false);
        expect(control.hasError('required')).toBe(true);
      }),
      { numRuns: 100 },
    );
  });

  it('should mark strings missing @ as invalid', () => {
    fc.assert(
      fc.property(
        fc.string({ minLength: 1 }).filter((s) => !s.includes('@')),
        (invalidEmail) => {
          const control = createEmailControl(invalidEmail);
          expect(control.valid).toBe(false);
          expect(control.hasError('email')).toBe(true);
        },
      ),
      { numRuns: 100 },
    );
  });

  it('should mark strings with @ but no domain as invalid', () => {
    fc.assert(
      fc.property(
        fc.string({ minLength: 1 }).map((local) => `${local.replace('@', '')}@`),
        (noDomainEmail) => {
          const control = createEmailControl(noDomainEmail);
          expect(control.valid).toBe(false);
        },
      ),
      { numRuns: 100 },
    );
  });
});

/**
 * Property-Based Tests for password length validation boundary.
 *
 * **Validates: Requirements 2.2, 3.2**
 */
describe('Form Validators — Property 4: Password length validation boundary', () => {
  function createPasswordControl(value: string): FormControl {
    const control = new FormControl(value, [Validators.required, Validators.minLength(8)]);
    control.markAsTouched();
    return control;
  }

  it('should mark passwords with length < 8 (non-empty) as invalid', () => {
    fc.assert(
      fc.property(
        fc.string({ minLength: 1, maxLength: 7 }),
        (shortPassword) => {
          const control = createPasswordControl(shortPassword);
          expect(control.valid).toBe(false);
          expect(control.hasError('minlength')).toBe(true);
        },
      ),
      { numRuns: 100 },
    );
  });

  it('should mark passwords with length >= 8 as valid', () => {
    fc.assert(
      fc.property(
        fc.string({ minLength: 8, maxLength: 50 }),
        (longPassword) => {
          const control = createPasswordControl(longPassword);
          expect(control.valid).toBe(true);
          expect(control.errors).toBeNull();
        },
      ),
      { numRuns: 100 },
    );
  });

  it('should mark empty string as invalid (required fails)', () => {
    fc.assert(
      fc.property(fc.constant(''), (emptyValue) => {
        const control = createPasswordControl(emptyValue);
        expect(control.valid).toBe(false);
        expect(control.hasError('required')).toBe(true);
      }),
      { numRuns: 100 },
    );
  });
});

/**
 * Property-Based Tests for passwords match validation.
 *
 * **Validates: Requirements 3.3**
 */
describe('Form Validators — Property 5: Passwords match validation', () => {
  function passwordsMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password')?.value;
    const confirm = control.get('confirmPassword')?.value;
    if (password && confirm && password !== confirm) {
      control.get('confirmPassword')?.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    }
    return null;
  }

  function createFormGroup(password: string, confirmPassword: string): FormGroup {
    return new FormGroup(
      {
        password: new FormControl(password),
        confirmPassword: new FormControl(confirmPassword),
      },
      { validators: [passwordsMatchValidator] },
    );
  }

  it('should return passwordMismatch error when passwords are not equal', () => {
    fc.assert(
      fc.property(
        fc.tuple(fc.string({ minLength: 1 }), fc.string({ minLength: 1 })).filter(
          ([a, b]) => a !== b,
        ),
        ([password, confirmPassword]) => {
          const group = createFormGroup(password, confirmPassword);
          expect(group.hasError('passwordMismatch')).toBe(true);
          expect(group.get('confirmPassword')?.hasError('passwordMismatch')).toBe(true);
        },
      ),
      { numRuns: 100 },
    );
  });

  it('should return null (valid) when passwords are equal and non-empty', () => {
    fc.assert(
      fc.property(fc.string({ minLength: 1 }), (password) => {
        const group = createFormGroup(password, password);
        expect(group.hasError('passwordMismatch')).toBe(false);
        expect(group.get('confirmPassword')?.hasError('passwordMismatch')).toBeFalsy();
      }),
      { numRuns: 100 },
    );
  });
});

type PasswordStrength = 'weak' | 'medium' | 'strong';

/**
 * Standalone implementation of evaluateStrength for testing the algorithm.
 * Mirrors the private method in RegisterComponent.
 */
function evaluateStrength(password: string): PasswordStrength | null {
  if (!password) return null;

  let score = 0;
  if (password.length >= 8) score++;
  if (password.length >= 12) score++;
  if (/[a-z]/.test(password) && /[A-Z]/.test(password)) score++;
  if (/\d/.test(password)) score++;
  if (/[^a-zA-Z0-9]/.test(password)) score++;

  if (score <= 2) return 'weak';
  if (score <= 3) return 'medium';
  return 'strong';
}

/**
 * Property-Based Tests for password strength classification completeness.
 *
 * **Validates: Requirements 3.4**
 */
describe('Form Validators — Property 6: Password strength classification completeness', () => {
  const validStrengths: PasswordStrength[] = ['weak', 'medium', 'strong'];

  it('should always return weak, medium, or strong for any non-empty string', () => {
    fc.assert(
      fc.property(fc.string({ minLength: 1 }), (password) => {
        const result = evaluateStrength(password);
        expect(result).not.toBeNull();
        expect(validStrengths).toContain(result);
      }),
      { numRuns: 100 },
    );
  });

  it('should classify short lowercase-only strings as weak', () => {
    const lowercaseShort = fc
      .array(fc.integer({ min: 97, max: 122 }).map((code) => String.fromCharCode(code)), { minLength: 1, maxLength: 7 })
      .map((chars) => chars.join(''));

    fc.assert(
      fc.property(lowercaseShort, (password: string) => {
        const result = evaluateStrength(password);
        expect(result).toBe('weak');
      }),
      { numRuns: 100 },
    );
  });

  it('should classify strings with length >= 12, mixed case, digits, and special chars as strong', () => {
    const strongPassword = fc
      .tuple(
        fc.array(fc.integer({ min: 97, max: 122 }).map((code) => String.fromCharCode(code)), { minLength: 2, maxLength: 4 }).map((a) => a.join('')),
        fc.array(fc.integer({ min: 65, max: 90 }).map((code) => String.fromCharCode(code)), { minLength: 2, maxLength: 4 }).map((a) => a.join('')),
        fc.array(fc.integer({ min: 48, max: 57 }).map((code) => String.fromCharCode(code)), { minLength: 2, maxLength: 4 }).map((a) => a.join('')),
        fc.constantFrom('!', '@', '#', '$', '%', '^', '&', '*'),
        fc.constantFrom('!', '@', '#', '$', '%', '^', '&', '*'),
      )
      .map(([lower, upper, digits, special1, special2]) => lower + upper + digits + special1 + special2)
      .filter((s) => s.length >= 12);

    fc.assert(
      fc.property(strongPassword, (password: string) => {
        const result = evaluateStrength(password);
        expect(result).toBe('strong');
      }),
      { numRuns: 100 },
    );
  });
});
