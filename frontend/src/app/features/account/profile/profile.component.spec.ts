import { describe, it, expect, beforeEach, vi } from 'vitest';
import * as fc from 'fast-check';
import { TestBed } from '@angular/core/testing';
import { ProfileComponent } from './profile.component';

/**
 * Extracts the CPF masking logic from ProfileComponent for pure-function testing.
 * Mirrors the computed maskedCpf logic:
 *   - strips non-digit characters
 *   - returns ***.***.***-XX where XX = last 2 digits
 *   - returns em-dash when input has fewer than 2 characters
 */
function maskCpf(cpf: string): string {
  if (!cpf || cpf.length < 2) return '\u2014';
  const lastTwo = cpf.replace(/\D/g, '').slice(-2);
  return '***.***.***-' + lastTwo;
}

/**
 * Arbitrary that generates 11-digit numeric strings.
 * Equivalent to fc.stringOf(fc.constantFrom(...'0123456789'), { minLength: 11, maxLength: 11 })
 * implemented via fc.array + map since stringOf is not available in fast-check v4.
 */
const elevenDigitString = fc.array(fc.constantFrom(...'0123456789'), { minLength: 11, maxLength: 11 }).map(arr => arr.join(''));

describe('ProfileComponent - Property 1: CPF masking preserves last two digits', () => {
  /**
   * **Validates: Requirements 1.1**
   *
   * Property 1: For any 11-digit string, masking produces ***.***.***-XX
   * where XX are the last two digits of the input.
   */
  it('should always produce ***.***.***-XX where XX are the last 2 digits of any 11-digit input', () => {
    fc.assert(
      fc.property(elevenDigitString, (digits: string) => {
        const cpf = digits.slice(0, 3) + '.' + digits.slice(3, 6) + '.' + digits.slice(6, 9) + '-' + digits.slice(9, 11);
        const masked = maskCpf(cpf);
        const expectedLastTwo = digits.slice(9, 11);

        expect(masked).toMatch(/^\*\*\*\.\*\*\*\.\*\*\*-/);
        expect(masked).toBe('***.***.***-' + expectedLastTwo);
      }),
      { numRuns: 100 },
    );
  });

  /**
   * **Validates: Requirements 1.1**
   *
   * Validates via TestBed that the component maskedCpf computed signal
   * produces the correct output for arbitrary 11-digit CPFs.
   */
  it('should produce correct masked CPF via component computed signal', () => {
    fc.assert(
      fc.property(elevenDigitString, (digits: string) => {
        const fixture = TestBed.createComponent(ProfileComponent);
        const component = fixture.componentInstance;

        const cpf = digits.slice(0, 3) + '.' + digits.slice(3, 6) + '.' + digits.slice(6, 9) + '-' + digits.slice(9, 11);
        component.profile.set({
          fullName: 'Test',
          email: 'test@test.com',
          phone: '(00) 00000-0000',
          cpf,
        });

        const result = component.maskedCpf();
        const expectedLastTwo = digits.slice(9, 11);

        expect(result).toBe('***.***.***-' + expectedLastTwo);
      }),
      { numRuns: 100 },
    );
  });
});

/**
 * Task 1.2: Unit tests for ProfileComponent
 * Validates: Requirements 1.2, 1.3, 1.4
 */
describe('ProfileComponent - Unit Tests', () => {
  let component: ProfileComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProfileComponent],
    }).compileComponents();

    const fixture = TestBed.createComponent(ProfileComponent);
    component = fixture.componentInstance;
    component.ngOnInit();
  });

  it('toggleEdit() flips the editing signal', () => {
    expect(component.editing()).toBe(false);
    component.toggleEdit();
    expect(component.editing()).toBe(true);
    component.toggleEdit();
    expect(component.editing()).toBe(false);
  });

  it('email field is disabled in the reactive form', () => {
    const emailControl = component.form.get('email');
    expect(emailControl).toBeDefined();
    expect(emailControl!.disabled).toBe(true);
  });

  it('onSave() updates the profile signal with form values', () => {
    vi.useFakeTimers();

    component.toggleEdit();
    component.form.patchValue({
      fullName: 'João Santos',
      phone: '(21) 98888-1234',
    });

    component.onSave();

    // Advance past the 800ms setTimeout in the component's onSave
    vi.advanceTimersByTime(800);

    expect(component.profile().fullName).toBe('João Santos');
    expect(component.profile().phone).toBe('(21) 98888-1234');

    vi.useRealTimers();
  });

  it('maskedCpf returns em-dash when CPF has fewer than 2 characters', () => {
    component.profile.set({
      fullName: 'Test',
      email: 'test@test.com',
      phone: '(00) 00000-0000',
      cpf: 'X',
    });

    expect(component.maskedCpf()).toBe('\u2014');
  });
});
