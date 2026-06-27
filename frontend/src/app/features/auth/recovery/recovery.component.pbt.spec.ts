import { describe, it, expect, vi, beforeEach } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import * as fc from 'fast-check';

import { RecoveryComponent } from './recovery.component';
import { AuthService } from '../services/auth.service';

/**
 * Property-Based Tests for RecoveryComponent.
 *
 * Property 7: Recovery flow never reveals email existence
 * For any email string submitted to the recovery flow, the component SHALL transition
 * to submitted = true and display the same generic message, regardless of whether
 * the backend returns success or error.
 *
 * **Validates: Requirements 4.3**
 */
describe('RecoveryComponent — Property 7: Recovery flow never reveals email existence', () => {
  let component: RecoveryComponent;
  let authServiceMock: { recoverPassword: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    authServiceMock = {
      recoverPassword: vi.fn(),
    };

    TestBed.configureTestingModule({
      imports: [RecoveryComponent, ReactiveFormsModule],
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        provideRouter([]),
      ],
    });

    const fixture = TestBed.createComponent(RecoveryComponent);
    component = fixture.componentInstance;
    component.ngOnInit();
  });

  it('should always transition to submitted=true regardless of backend success or error', () => {
    fc.assert(
      fc.property(
        fc.emailAddress(),
        fc.boolean(),
        (email, backendSucceeds) => {
          // Reset submitted state for each iteration
          component.submitted.set(false);
          component.loading.set(false);

          // Configure mock based on whether backend succeeds or fails
          if (backendSucceeds) {
            authServiceMock.recoverPassword.mockReturnValue(of(void 0));
          } else {
            authServiceMock.recoverPassword.mockReturnValue(
              throwError(() => new Error('User not found')),
            );
          }

          // Set the email value on the form and mark as valid
          component.form.get('email')!.setValue(email);
          component.form.get('email')!.markAsTouched();

          // Submit the form
          component.onSubmit();

          // Assert: component always transitions to submitted = true
          expect(component.submitted()).toBe(true);
          // Assert: loading is always false after completion
          expect(component.loading()).toBe(false);
        },
      ),
      { numRuns: 100 },
    );
  });

  it('should call recoverPassword with the exact email provided regardless of backend outcome', () => {
    fc.assert(
      fc.property(
        fc.emailAddress(),
        fc.boolean(),
        (email, backendSucceeds) => {
          // Reset state
          component.submitted.set(false);
          component.loading.set(false);
          authServiceMock.recoverPassword.mockClear();

          if (backendSucceeds) {
            authServiceMock.recoverPassword.mockReturnValue(of(void 0));
          } else {
            authServiceMock.recoverPassword.mockReturnValue(
              throwError(() => new Error('Not found')),
            );
          }

          component.form.get('email')!.setValue(email);
          component.form.get('email')!.markAsTouched();

          component.onSubmit();

          // The service was called with the email
          expect(authServiceMock.recoverPassword).toHaveBeenCalledWith(email);
          // The final state is always the same: submitted = true
          expect(component.submitted()).toBe(true);
        },
      ),
      { numRuns: 100 },
    );
  });
});
