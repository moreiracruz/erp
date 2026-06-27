# Implementation Plan: Área do Cliente (Minha Conta) — Testing

## Overview

The account feature is fully implemented. This task list focuses exclusively on writing property-based tests (fast-check) and unit tests for the 6 correctness properties defined in the design, plus unit test coverage for key component behaviors. All tests use Vitest with Angular TestBed and fast-check for PBT.

## Tasks

- [x] 1. Profile component tests
  - [x] 1.1 Write property test for CPF masking (Property 1)
    - **Property 1: CPF masking preserves last two digits**
    - For any 11-digit string, the `maskedCpf` computed should produce `***.***.***-XX` where XX are the last two digits of the input
    - Extract the masking logic from `ProfileComponent` and test it with fast-check using `fc.stringOf(fc.constantFrom(...'0123456789'), { minLength: 11, maxLength: 11 })`
    - Verify that the result always starts with `***.***.***-` and ends with the last 2 digits of the input
    - Use `numRuns: 100` minimum
    - Create file: `frontend/src/app/features/account/profile/profile.component.spec.ts`
    - **Validates: Requirements 1.1**

  - [x] 1.2 Write unit tests for ProfileComponent
    - Test `toggleEdit()` flips the `editing` signal
    - Test that email field is disabled in the reactive form
    - Test `onSave()` updates the profile signal with form values
    - Test `maskedCpf` returns '—' when CPF has < 2 characters
    - _Requirements: 1.2, 1.3, 1.4_

- [x] 2. Orders component tests
  - [x] 2.1 Write property test for status label mapping (Property 2)
    - **Property 2: Order status label mapping is total (bijective)**
    - For any valid status from `fc.constantFrom('processing', 'shipped', 'delivered')`, `getStatusLabel` returns a non-empty string from the set {'Processando', 'Enviado', 'Entregue'}
    - Verify bijectivity: all 3 statuses map to 3 unique labels
    - Use `numRuns: 100` minimum
    - Create file: `frontend/src/app/features/account/orders/orders.component.spec.ts`
    - **Validates: Requirements 2.2**

  - [x] 2.2 Write property test for toggle expand idempotence (Property 3)
    - **Property 3: Toggle expand is its own inverse**
    - For any order ID string and any initial `expandedOrderId` state (null or arbitrary string), calling `toggleOrder(id)` twice returns `expandedOrderId` to its original value
    - Use `fc.option(fc.string())` for initial state and `fc.string()` for the toggled ID
    - Use `numRuns: 100` minimum
    - Add to file: `frontend/src/app/features/account/orders/orders.component.spec.ts`
    - **Validates: Requirements 2.3**

  - [x] 2.3 Write unit tests for OrdersComponent
    - Test `formatCurrency` produces pt-BR BRL format
    - Test `formatDate` produces pt-BR date string
    - Test `toggleOrder` with matching ID sets to null (collapse)
    - Test `toggleOrder` with different ID sets to new ID (expand)
    - _Requirements: 2.1, 2.3_

- [x] 3. Checkpoint - Ensure profile and orders tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 4. Addresses component tests
  - [x] 4.1 Write property test for address CRUD invariants (Property 4)
    - **Property 4: Address CRUD preserves collection invariants**
    - **Add**: For any valid address data, adding to a list of N addresses produces a list of N+1 addresses containing the new entry
    - **Delete**: For any existing address ID in a non-empty list, deleting produces a list of N-1 and the ID is no longer present
    - **Edit**: For any existing address and new field values, editing preserves list length and updates the targeted entry
    - Use `fc.record(...)` to generate address data and `fc.array(...)` for initial lists
    - Use `numRuns: 100` minimum
    - Create file: `frontend/src/app/features/account/addresses/addresses.component.spec.ts`
    - **Validates: Requirements 3.3**

  - [x] 4.2 Write property test for address form validation (Property 5)
    - **Property 5: Address form validation rejects incomplete data**
    - For any combination of address fields where at least one required field (street, number, neighborhood, city, state, cep) is empty string, the form should report as invalid
    - Use fast-check to generate objects with at least one required field blank
    - Use `numRuns: 100` minimum
    - Add to file: `frontend/src/app/features/account/addresses/addresses.component.spec.ts`
    - **Validates: Requirements 3.4**

  - [x] 4.3 Write unit tests for AddressesComponent
    - Test `openForm()` sets `showForm` to true and resets `editingId`
    - Test `editAddress()` populates form and sets `editingId`
    - Test `closeForm()` resets form and hides it
    - Test `deleteAddress()` removes the correct address from the list
    - _Requirements: 3.1, 3.3_

- [x] 5. Change password component tests
  - [x] 5.1 Write property test for password validation (Property 6)
    - **Property 6: Password validation correctness**
    - For any string with fewer than 8 characters, the `newPassword` control should report a `minlength` error
    - For any pair of distinct non-empty strings in `newPassword` and `confirmPassword`, the form-level validator should report `passwordMismatch`
    - Use `fc.string({ minLength: 1, maxLength: 7 })` for short passwords and `fc.tuple(fc.string({minLength:1}), fc.string({minLength:1})).filter(([a,b]) => a !== b)` for mismatched pairs
    - Use `numRuns: 100` minimum
    - Create file: `frontend/src/app/features/account/change-password/change-password.component.spec.ts`
    - **Validates: Requirements 4.2**

  - [x] 5.2 Write unit tests for ChangePasswordComponent
    - Test form has 3 controls (currentPassword, newPassword, confirmPassword)
    - Test valid form submission sets successMsg
    - Test form resets after successful submission
    - Test form invalid when currentPassword is empty
    - _Requirements: 4.1, 4.3_

- [x] 6. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each property test references specific correctness properties from the design document
- All tests use Vitest as the runner and fast-check for property-based testing
- Tests should import component logic directly or instantiate components via Angular TestBed
- The feature is already fully implemented — no production code changes are expected
- Property tests validate universal correctness properties across all valid inputs
- Unit tests validate specific examples, edge cases, and component integration

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "2.1", "4.1", "5.1"] },
    { "id": 1, "tasks": ["1.2", "2.2", "4.2", "5.2"] },
    { "id": 2, "tasks": ["2.3", "4.3"] }
  ]
}
```
