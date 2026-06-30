# Implementation Plan: Frontend Auth — Testing

## Overview

The authentication feature is fully implemented. This plan focuses exclusively on installing the property-based testing library (fast-check), writing property-based tests for the 10 correctness properties defined in the design, and writing unit/integration tests for the auth components and services using Vitest.

## Tasks

- [x] 1. Set up testing infrastructure
  - [x] 1.1 Install fast-check as a dev dependency
    - Run `npm install --save-dev fast-check` in the `frontend/` directory
    - Verify the package is added to `package.json` devDependencies
    - _Requirements: 10.1, 10.2, 10.3_

- [x] 2. Property-based tests for AuthService error mapping and token logic
  - [x] 2.1 Write property test for error mapping preserving dynamic parameters
    - **Property 1: Error mapping preserves dynamic parameters**
    - Create `features/auth/services/auth.service.pbt.spec.ts`
    - Use `fc.nat()` to generate random positive integers for minutes
    - Assert `mapLoginError` output includes the exact numeric value for HTTP 423 and 429 responses
    - **Validates: Requirements 1.4, 7.3**

  - [x] 2.2 Write property test for JWT decode round trip
    - **Property 8: JWT decode round trip**
    - Generate random User objects with `fc.record({ uuid: fc.uuid(), username: fc.string(), role: fc.constantFrom(...UserRoles), active: fc.boolean() })`
    - Encode as a fake JWT payload (base64), pass through `decodeToken`, verify field equality
    - **Validates: Requirements 5.2**

  - [x] 2.3 Write property test for token refresh scheduled at 80% of expiry
    - **Property 9: Token refresh scheduled at 80% of expiry**
    - Use `fc.integer({ min: 1, max: 86400 })` for expiresIn values
    - Spy on `setTimeout` and assert delay equals `expiresIn * 0.8 * 1000`
    - **Validates: Requirements 5.3**

- [x] 3. Property-based tests for TokenStorageService
  - [x] 3.1 Write property test for token storage strategy respecting remember-me preference
    - **Property 2: Token storage strategy respects remember-me preference**
    - Create `infrastructure/storage/token-storage.service.pbt.spec.ts`
    - Use `fc.tuple(fc.string({ minLength: 1 }), fc.string({ minLength: 1 }), fc.boolean())` for (accessToken, refreshToken, rememberMe)
    - Assert localStorage contains tokens when rememberMe=true and sessionStorage does not, and vice-versa
    - **Validates: Requirements 1.6**

- [x] 4. Property-based tests for form validators
  - [x] 4.1 Write property test for email validation rejecting invalid formats
    - **Property 3: Email validation rejects invalid formats**
    - Create `features/auth/validators/form-validators.pbt.spec.ts`
    - Generate invalid emails (missing @, empty, no domain) and valid emails using `fc.emailAddress()`
    - Apply Angular Validators.email and Validators.required; assert invalid/valid accordingly
    - **Validates: Requirements 2.1, 4.2**

  - [x] 4.2 Write property test for password length validation boundary
    - **Property 4: Password length validation boundary**
    - Generate strings with length < 8 using `fc.string({ maxLength: 7 })` and strings with length ≥ 8 using `fc.string({ minLength: 8, maxLength: 50 })`
    - Apply Validators.minLength(8); assert invalid for short, valid for long
    - **Validates: Requirements 2.2, 3.2**

  - [x] 4.3 Write property test for passwords match validation
    - **Property 5: Passwords match validation**
    - Use `fc.tuple(fc.string({ minLength: 1 }), fc.string({ minLength: 1 }))` to generate pairs
    - Assert `passwordsMatchValidator` returns error when not equal, null when equal
    - **Validates: Requirements 3.3**

  - [x] 4.4 Write property test for password strength classification completeness
    - **Property 6: Password strength classification completeness**
    - Generate arbitrary non-empty strings with `fc.string({ minLength: 1 })`
    - Assert `evaluateStrength` always returns one of 'weak', 'medium', or 'strong'
    - Additionally generate weak-band strings (short, lowercase only) and strong-band strings (≥12, mixed case + digits + special)
    - **Validates: Requirements 3.4**

- [x] 5. Property-based tests for recovery flow and JWT interceptor
  - [x] 5.1 Write property test for recovery flow never revealing email existence
    - **Property 7: Recovery flow never reveals email existence**
    - Create `features/auth/recovery/recovery.component.pbt.spec.ts`
    - Use `fc.emailAddress()` to generate emails
    - Mock backend to return success or error; assert component always transitions to `submitted = true` and displays the same generic message
    - **Validates: Requirements 4.3**

  - [x] 5.2 Write property test for JWT interceptor excluding auth endpoints
    - **Property 10: JWT interceptor excludes auth endpoints**
    - Create `infrastructure/auth/jwt.interceptor.pbt.spec.ts`
    - Generate URLs with `fc.oneof(fc.constant('/auth/login'), fc.constant('/auth/refresh'), fc.string())` combined with base URL
    - Assert no Authorization header on auth URLs, and Bearer header present on non-auth URLs when token exists
    - **Validates: Requirements 7.5**

- [x] 6. Checkpoint — Ensure all property-based tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 7. Unit tests for AuthService
  - [x] 7.1 Write unit tests for AuthService login and signal state transitions
    - Create `features/auth/services/auth.service.spec.ts`
    - Test login success → currentUser signal populated, isAuthenticated = true, userRole set
    - Test login failure → signals unchanged, error message returned
    - Test logout → signals cleared, tokens removed, navigated to home
    - _Requirements: 10.1, 5.1, 5.5_

  - [x] 7.2 Write unit tests for AuthService token refresh success and failure
    - Test refresh success → new tokens saved, signals updated
    - Test refresh failure → session cleared, navigated to login with session_expired
    - Test initFromStorage with valid token → signals populated
    - Test initFromStorage with invalid/malformed token → session cleared
    - _Requirements: 10.1, 5.3, 5.4_

- [x] 8. Unit tests for LoginComponent
  - [x] 8.1 Write unit tests for LoginComponent form validation and error display
    - Create `features/auth/login/login.component.spec.ts`
    - Test form invalid when email empty or invalid format
    - Test form invalid when password < 8 chars
    - Test submit button disabled when form invalid
    - Test loading state set during request
    - Test error messages displayed for 401, 423, 429, and network errors
    - Test session_expired message displayed from query param
    - _Requirements: 10.2, 2.1, 2.2, 2.3, 2.4, 1.3, 1.4, 1.5_

  - [x] 8.2 Write unit tests for LoginComponent navigation and remember-me
    - Test navigation to returnUrl on success
    - Test navigation to default role route when no returnUrl
    - Test rememberMe flag passed to auth service
    - _Requirements: 10.2, 1.1, 1.6, 6.2, 6.3_

- [x] 9. Unit tests for RegisterComponent
  - [x] 9.1 Write unit tests for RegisterComponent password strength and validation
    - Create `features/auth/register/register.component.spec.ts`
    - Test password strength indicator updates on input (weak, medium, strong examples)
    - Test confirmPassword mismatch shows error
    - Test terms checkbox required for submission
    - _Requirements: 10.3, 3.4, 3.3, 3.5_

  - [x] 9.2 Write unit tests for RegisterComponent submission and error handling
    - Test successful registration navigates to '/'
    - Test duplicate email error (409) displays correct message
    - Test network error displays connection message
    - _Requirements: 10.3, 3.7, 3.6_

- [x] 10. Integration tests for complete auth flows
  - [x] 10.1 Write integration tests for full login flow
    - Create `features/auth/integration/auth-flow.integration.spec.ts`
    - Test form submit → AuthService → token stored → signals updated → navigation
    - Test remember-me persistence (localStorage vs sessionStorage)
    - _Requirements: 10.4, 1.1, 1.6_

  - [x] 10.2 Write integration tests for token refresh cycle
    - Simulate timer expiry → refresh call → new tokens stored → signals updated
    - Test refresh failure → session cleared → redirect to login
    - _Requirements: 10.4, 5.3, 5.4_

- [x] 11. Final checkpoint — Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Property-based tests (tasks 2–5) are the core deliverable; unit/integration tests complement them
- All test files use Vitest as the test runner (configured via `@angular/build:unit-test`)
- fast-check is the PBT library; minimum 100 iterations per property
- The `evaluateStrength` and `passwordsMatchValidator` methods are private; tests should either test them through the component's public API or export them as standalone functions
- The `mapLoginError`, `decodeToken`, and `scheduleRefresh` methods in AuthService are private; tests may need to access them via controlled inputs (e.g., passing specific HttpErrorResponse objects through `login()`)

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1"] },
    { "id": 1, "tasks": ["2.1", "2.2", "2.3", "3.1", "4.1", "4.2", "4.3", "4.4"] },
    { "id": 2, "tasks": ["5.1", "5.2"] },
    { "id": 3, "tasks": ["7.1", "7.2", "8.1", "8.2", "9.1", "9.2"] },
    { "id": 4, "tasks": ["10.1", "10.2"] }
  ]
}
```
