# Implementation Plan: Frontend Checkout

## Overview

The checkout feature (cart page, checkout wizard, order summary, CartService) is already implemented. This plan focuses on verifying existing code against requirements, installing the property-based testing library (fast-check), writing property-based tests for CartService correctness properties, writing unit tests for components, and fixing any gaps found during verification.

## Tasks

- [x] 1. Install fast-check and verify test infrastructure
  - [x] 1.1 Install fast-check as a dev dependency
    - Run `npm install --save-dev fast-check` in the `frontend/` directory
    - Verify the package is added to `package.json` devDependencies
    - Confirm Vitest can import fast-check by creating a minimal smoke test
    - _Requirements: Design Testing Strategy_

- [x] 2. CartService property-based tests
  - [x] 2.1 Write property test: Subtotal equals sum of line totals
    - Create `frontend/src/app/features/storefront/services/cart.service.spec.ts`
    - Generate arbitrary CartItem arrays with positive prices and quantities using fast-check
    - Assert that `subtotal()` equals `Σ(price × quantity)` for all generated inputs
    - Tag: `Feature: frontend-checkout, Property 1: Subtotal equals sum of line totals`
    - **Property 1: Subtotal equals sum of line totals**
    - **Validates: Requirements 1.3, 6.2**

  - [x] 2.2 Write property test: Shipping cost threshold
    - Generate arbitrary cart states where subtotal varies around the 299 threshold
    - Assert `shippingCost()` is `0` when `subtotal >= 299` and `15.90` when `subtotal < 299`
    - Tag: `Feature: frontend-checkout, Property 2: Shipping cost threshold`
    - **Property 2: Shipping cost threshold**
    - **Validates: Requirements 3.2, 6.2**

  - [x] 2.3 Write property test: Total is subtotal plus shipping
    - For any generated cart state, assert `total()` === `subtotal() + shippingCost()`
    - Tag: `Feature: frontend-checkout, Property 3: Total is subtotal plus shipping`
    - **Property 3: Total is subtotal plus shipping**
    - **Validates: Requirements 6.2**

  - [x] 2.4 Write property test: removeItem shrinks cart and excludes item
    - Generate a non-empty cart and pick a random existing variantUuid
    - Call `removeItem(variantUuid)` and assert: cart length decreased by 1, removed UUID is absent
    - Tag: `Feature: frontend-checkout, Property 4: Remove item decreases cart size and excludes item`
    - **Property 4: Remove item decreases cart size and excludes item**
    - **Validates: Requirements 1.2**

  - [x] 2.5 Write property test: updateQuantity changes only the target item
    - Generate a cart with multiple items, pick one variantUuid, generate a positive newQty
    - Call `updateQuantity(variantUuid, newQty)` and assert: target item has newQty, all other items unchanged
    - Tag: `Feature: frontend-checkout, Property 5: updateQuantity changes only the target item`
    - **Property 5: updateQuantity changes only the target item**
    - **Validates: Requirements 6.1**

- [x] 3. Checkpoint - Ensure all property tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 4. CartPageComponent unit tests
  - [x] 4.1 Write unit tests for CartPageComponent
    - Create `frontend/src/app/features/checkout/cart/cart-page.component.spec.ts`
    - Test: renders item list when cart has items (requirement 1.1)
    - Test: shows empty state with message when cart is empty (requirement 1.5)
    - Test: increment calls updateQuantity with qty + 1
    - Test: decrement calls updateQuantity with qty - 1, does not go below 1
    - Test: remove calls removeItem with correct variantUuid (requirement 1.2)
    - Test: goToCheckout navigates to `/checkout` (requirement 1.4)
    - _Requirements: 1.1, 1.2, 1.4, 1.5_

- [x] 5. CheckoutPageComponent unit tests
  - [x] 5.1 Write unit tests for CheckoutPageComponent
    - Create `frontend/src/app/features/checkout/checkout-page/checkout-page.component.spec.ts`
    - Test: initializes at step 1 (requirement 2.1)
    - Test: nextStep advances from step 1 to 2 to 3 (requirement 2.1)
    - Test: prevStep goes back from step 3 to 2 to 1 (requirement 2.1)
    - Test: isStep1Valid requires name, email, phone (requirement 2.3)
    - Test: isStep2Valid requires cep, street, number, neighborhood, city, state (requirement 2.4)
    - Test: isStep3Valid requires a payment method selection (requirement 2.5)
    - Test: pre-fills email from AuthService when user is authenticated (requirement 2.3)
    - Test: confirmOrder clears cart and navigates to home (requirement 2.1)
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

- [x] 6. OrderSummaryComponent unit tests
  - [x] 6.1 Write unit tests for OrderSummaryComponent
    - Create `frontend/src/app/features/checkout/components/order-summary/order-summary.component.spec.ts`
    - Test: renders items, subtotal, shippingCost, and total from inputs (requirement 3.1)
    - Test: computes itemCount as sum of quantities
    - Test: toggleCollapse toggles the collapsed signal (requirement 3.3)
    - _Requirements: 3.1, 3.3_

- [x] 7. Verify guest checkout and routing
  - [x] 7.1 Write smoke test verifying checkout routes have no auth guard
    - Verify `CHECKOUT_ROUTES` configuration loads without guards
    - Confirm both `/checkout/cart` and `/checkout` routes are accessible (requirement 4.1)
    - _Requirements: 4.1_

- [x] 8. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Property-based tests (tasks 2.1–2.5) are the primary deliverables — they validate CartService correctness properties from the design
- The implementation already exists; no new feature code is needed unless a gap is discovered during testing
- fast-check should generate at least 100 iterations per property
- All tests use Vitest as the test runner (already configured via `@angular/build:unit-test`)
- CartService tests require Angular's `TestBed` for signal reactivity or direct instantiation since it has no Angular-specific deps beyond `@Injectable`

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1"] },
    { "id": 1, "tasks": ["2.1", "2.2", "2.3", "2.4", "2.5"] },
    { "id": 2, "tasks": ["4.1", "5.1", "6.1", "7.1"] }
  ]
}
```
