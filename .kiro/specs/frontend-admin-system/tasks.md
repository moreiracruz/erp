# Implementation Plan: Frontend Administracao do Sistema

## Overview

This plan implements the protected ERP administration frontend and closes the most important frontend/backend integration gaps for physical-store operation. The plan is organized in waves so that each slice is usable and testable before moving to broader modules.

Priority path:

1. Admin shell + RBAC navigation.
2. Product/variant/image administration.
3. Inventory administration.
4. Consignment administration and stock-origin visibility.
5. Physical sales / PDV with stock-origin awareness.
6. Customers, finance, pricing, dashboard, users.
7. Integration tests and E2E smoke coverage.

## Tasks

---

## Wave 1: Admin Shell, Routing, and Shared Infrastructure

- [ ] 1. Create admin shell and route structure
  - [ ] 1.1 Create `AdminShellComponent`
    - Add sidebar, topbar, breadcrumb/title region, content outlet, logout action.
    - Use responsive collapsed navigation below 768px.
    - _Requirements: 1.2, 1.5_

  - [ ] 1.2 Consolidate protected admin routes
    - Create/adjust route groups for dashboard, products, inventory, consignments, POS, customers, finance, pricing, and users.
    - Apply existing `authGuard` and role metadata to protected admin routes.
    - Preserve redirects from existing route paths when practical.
    - _Requirements: 1.1, 1.4_

  - [ ] 1.3 Implement `AdminMenuService`
    - Define menu item model with label, route, icon, allowed roles, and feature flag/status.
    - Filter menu by authenticated user role.
    - Write property test: visible menu items always include current role.
    - _Requirements: 1.3, 14.3_

  - [ ] 1.4 Add shared admin UI primitives
    - Create reusable loading, empty, error, confirmation dialog, form-error, page-header, and data-toolbar components where not already available.
    - Keep components standalone and OnPush.
    - _Requirements: 12.4_

- [ ] 2. Define shared admin ports, error models, and adapter conventions
  - [ ] 2.1 Create `AdminApiError` and mapper utilities
    - Normalize HTTP 401, 403, 404, 422, and network failures into UI-friendly errors.
    - _Requirements: 12.4_

  - [ ] 2.2 Create frontend ports
    - Add `ProductAdminPort`, `InventoryPort`, `ConsignmentPort`, `SalesPort`, `CustomerPort`, `FinancePort`, `PricingPort`, `DashboardPort`, and `UserAdminPort`.
    - Keep DTOs in `core/models` or feature-specific model folders, following existing patterns.
    - _Requirements: 12.1, 12.2_

  - [ ] 2.3 Register HTTP adapters in `app.config.ts`
    - Ensure adapters are injectable through ports.
    - Ensure JWT interceptor applies to protected calls.
    - _Requirements: 12.2, 12.5_

---

## Wave 2: Backend Contract Alignment for Admin

- [ ] 3. Close Product API read-model gaps
  - [ ] 3.1 Add admin product detail read model on backend
    - Include product fields, active status, variants, and image metadata or image summary when needed.
    - Preserve Clean Architecture boundaries.
    - _Requirements: 3.1, 3.2, 3.4_

  - [ ] 3.2 Add storefront/admin product summary read model
    - Include `minPrice`, `maxPrice`, variant count, and main image card URL when available.
    - Keep public GET endpoints safe for storefront.
    - _Requirements: 3.1, 4.1_

  - [ ] 3.3 Add backend tests for product read models
    - Cover active products, products with no variants, inactive variants, and products with/without images.
    - _Requirements: 14.1_

- [ ] 4. Identify and document remaining backend contract gaps
  - [ ] 4.1 Validate Sales API supports POS quantity edit/remove needs
    - If missing, create backend tasks for remove item/update quantity or define cancel/re-add behavior.
    - _Requirements: 6.2, 6.8_

  - [ ] 4.2 Validate Pricing API list/update coupon needs
    - Add backend tasks if the admin must list, deactivate, or edit coupons.
    - _Requirements: 9.1, 9.4_

  - [ ] 4.3 Validate UserAdmin backend endpoints
    - Add backend requirements/tasks before frontend user administration implementation.
    - _Requirements: 11.1, 11.4_

  - [ ] 4.4 Validate Dashboard backend data source
    - Decide between dedicated dashboard endpoint or composed reads.
    - Document the decision in the spec or an ADR if meaningful.
    - _Requirements: 2.1, 2.5_

  - [ ] 4.5 Validate Consignment API readiness
    - Confirm endpoints, DTOs, role rules, and stock-origin fields from `.kiro/specs/consignment-management`.
    - Block consignment frontend screens until lifecycle semantics and settlement decisions are available.
    - _Requirements: 10.1, 10.12_

---

## Wave 3: Product, Variant, and Image Administration

- [ ] 5. Implement ProductAdmin HTTP adapter and facade
  - [ ] 5.1 Implement `ProductAdminHttpAdapter`
    - Map list, get, create, update, deactivate, and create variant calls to backend Product API.
    - Add adapter tests for URL, method, body, and error mapping.
    - _Requirements: 3.1, 3.3, 12.2, 14.1_

  - [ ] 5.2 Implement `ProductAdminService`
    - Signals: `products`, `selectedProduct`, `loading`, `saving`, `error`, `filters`.
    - Actions: load, search/filter, create, update, deactivate, createVariant, refreshSelected.
    - _Requirements: 3.1, 3.2, 12.3_

- [ ] 6. Replace mock admin products UI
  - [ ] 6.1 Refactor existing `ProductsComponent` into product list page or replace it with `ProductListPageComponent`
    - Remove hardcoded products.
    - Load from `ProductAdminService`.
    - _Requirements: 3.1_

  - [ ] 6.2 Implement product create/edit form
    - Use reactive forms.
    - Preserve user input after HTTP 422.
    - Add unit and property tests.
    - _Requirements: 3.3, 3.7, 14.3_

  - [ ] 6.3 Implement variant management UI
    - Variant table plus create form for SKU, barcode, size, color, sale price, cost, active status.
    - Display duplicate SKU/barcode backend errors inline.
    - _Requirements: 3.4, 3.5_

  - [ ] 6.4 Integrate product image section with persisted UUIDs
    - Reuse existing upload/grid components.
    - Ensure image section is disabled until product has a backend UUID.
    - _Requirements: 4.1, 4.2, 4.4_

---

## Wave 4: Inventory and Consignment Administration

- [ ] 7. Implement Inventory adapter and facade
  - [ ] 7.1 Implement `InventoryHttpAdapter`
    - Map variant search, stock lookup, entry, withdrawal, and movement history endpoints.
    - Add HTTP adapter tests.
    - _Requirements: 5.1, 5.2, 5.3, 5.6, 14.1_

  - [ ] 7.2 Implement `InventoryService`
    - Signals: selected variant, stock summary, movement history, loading, error.
    - Actions: searchBySku, searchByBarcode, loadStock, registerEntry, registerWithdrawal, loadMovements.
    - _Requirements: 5.1, 5.2, 5.3_

- [ ] 8. Build Inventory page
  - [ ] 8.1 Create variant search and stock summary UI
    - Support SKU/barcode lookup.
    - Show physical, reserved, available, owned, consigned-in, consigned-out, and origin-specific reserved stock when available.
    - _Requirements: 5.1, 5.2, 5.7_

  - [ ] 8.2 Create entry and withdrawal forms
    - Use reactive forms.
    - Validate quantity range [1, 100000].
    - Show insufficient stock backend responses.
    - _Requirements: 5.3, 5.4, 5.5_

  - [ ] 8.3 Create movement history table
    - Show operation type, quantity, timestamp, actor, and reference UUID when available.
    - _Requirements: 5.6_

- [ ] 8.4 Implement Consignment adapter and facade
  - [ ] 8.4.1 Implement `ConsignmentHttpAdapter`
    - Map list, detail, open received, open sent, returns, definitive purchase, sent-sale confirmation, and settlement actions to `/api/v1/consignments`.
    - Add adapter tests for URL, method, body mapping, role-sensitive errors, and HTTP 422 pending-quantity errors.
    - _Requirements: 10.1, 10.5, 10.9, 10.10, 12.2, 14.1_

  - [ ] 8.4.2 Implement `ConsignmentService`
    - Signals: consignments, selectedConsignment, filters, loading, saving, error, reports.
    - Actions: search, loadDetail, openReceived, openSent, returnReceived, returnSent, purchaseReceived, confirmSentSale, markSettlementPaid, markSettlementReceived.
    - _Requirements: 10.5, 10.6, 10.7, 10.8, 10.10, 12.3_

- [ ] 8.5 Build Consignacoes admin pages
  - [ ] 8.5.1 Add Consignacoes route and menu item
    - Visible to ROLE_MANAGER, ROLE_STOCK, and ROLE_FINANCE with action-level filtering.
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 14.3_

  - [ ] 8.5.2 Build consignment list and detail pages
    - Filters by type, status, party, date range, due date, and variant.
    - Detail shows original, sold, purchased, returned, pending quantities, and settlement status.
    - _Requirements: 10.5, 10.6_

  - [ ] 8.5.3 Build received and sent consignment forms
    - Collect party, due date, notes, variant items, quantities, and settlement values when required.
    - Preserve form data on backend validation errors.
    - _Requirements: 10.7, 10.8, 10.9_

  - [ ] 8.5.4 Build return, purchase, sent-sale confirmation, and settlement panels
    - Role-aware actions; no frontend-only quantity mutation.
    - Keep fiscal/NF-e behavior out of scope.
    - _Requirements: 10.2, 10.3, 10.4, 10.9, 10.10, 10.11_

---

## Wave 5: Physical Sales / PDV

- [ ] 9. Implement Sales adapter and POS facade
  - [ ] 9.1 Implement `SalesHttpAdapter`
    - Map open sale, add item, finalize, cancel, and get sale endpoints.
    - Add HTTP adapter tests.
    - _Requirements: 6.1, 6.2, 6.6, 6.8, 6.10, 14.1_

  - [ ] 9.2 Implement `PosService`
    - Signals: current sale, selected item, loading, error, payment state.
    - Actions: openSale, addItemByCode, finalize, cancel, reset.
    - Ensure displayed totals and stock-origin decisions come from backend sale state.
    - _Requirements: 6.1, 6.2, 6.5, 6.7, 6.10, 6.11_

  - [ ] 9.3 Add payment method mapping utility
    - Map frontend values to backend enum values.
    - Property test mapping is total and exact.
    - _Requirements: 6.6, 14.3_

  - [ ] 9.4 Add stock-origin handling utility
    - Render owned vs consigned-in origin labels from backend sale items.
    - If backend allocation policy is manual, expose origin selection before adding the item.
    - _Requirements: 6.10, 6.11, 10.12_

- [ ] 10. Replace mock POS terminal
  - [ ] 10.1 Refactor `TerminalComponent` into integrated `PosTerminalPageComponent`
    - Remove random/mock product generation.
    - Call backend via `PosService`.
    - _Requirements: 6.1, 6.2_

  - [ ] 10.2 Implement product scan/search input
    - SKU/barcode input with keyboard-friendly flow.
    - Show product not found and insufficient stock errors.
    - _Requirements: 6.2, 6.3, 6.4, 6.9, 6.10_

  - [ ] 10.3 Implement sale item table and backend totals display
    - Show items, quantities, prices, stock origin when allowed, subtotal, discount, tax, and total from backend response.
    - _Requirements: 6.5, 6.11_

  - [ ] 10.4 Implement payment panel and finalization flow
    - Support DINHEIRO, DEBITO, CREDITO, PIX.
    - For cash, collect amount paid and display backend change.
    - _Requirements: 6.6, 6.7_

  - [ ] 10.5 Implement cancellation dialog
    - Require reason.
    - Call backend cancel endpoint and clear sale state on success.
    - _Requirements: 6.8_

---

## Wave 6: Customers, Finance, and Pricing

- [ ] 11. Implement Customer administration
  - [ ] 11.1 Implement `CustomerHttpAdapter` and `CustomerService`
    - Register, get, update, deactivate, and search customers.
    - Add adapter and facade tests.
    - _Requirements: 7.1, 7.2, 7.4, 14.1, 14.2_

  - [ ] 11.2 Build customer search/list and form pages
    - Search by CPF, partial name, or UUID.
    - Display CPF duplicate and invalid messages from backend.
    - _Requirements: 7.1, 7.2, 7.3_

  - [ ] 11.3 Integrate customer association into POS
    - Optional customer lookup before or during sale.
    - Handle inactive customer backend rejection.
    - _Requirements: 7.5_

- [ ] 12. Implement Finance administration
  - [ ] 12.1 Implement `FinanceHttpAdapter` and `FinanceService`
    - Cash-flow query, expense creation, entry detail.
    - Add tests for date range and error mapping.
    - _Requirements: 8.1, 8.2, 8.3, 14.1_

  - [ ] 12.2 Build cash-flow report page
    - Date filters, totals, daily entries, retry/error states.
    - _Requirements: 8.1, 8.5_

  - [ ] 12.3 Build expense form
    - Amount, description, category, competence date.
    - Display all backend validation failures.
    - _Requirements: 8.3, 8.4_

- [ ] 13. Implement Pricing administration
  - [ ] 13.1 Implement `PricingHttpAdapter` and `PricingService`
    - Campaign list/create/deactivate and coupon create.
    - Add tests for conflict and validation errors.
    - _Requirements: 9.1, 9.2, 9.3, 9.4_

  - [ ] 13.2 Build campaign and coupon management pages
    - Use reactive forms.
    - Keep backend as source of truth for conflicts and discount calculation.
    - _Requirements: 9.2, 9.4, 9.5_

---

## Wave 7: Dashboard and User Administration

- [ ] 14. Implement operational dashboard
  - [ ] 14.1 Implement `DashboardPort` and selected backend strategy
    - Use dedicated endpoint if implemented, or documented composed reads.
    - _Requirements: 2.1, 2.5_

  - [ ] 14.2 Build role-aware dashboard widgets
    - Manager, finance, cashier, and stock views differ by role.
    - Include loading, empty, and retryable error states.
    - _Requirements: 2.1, 2.2, 2.3_

- [ ] 15. Implement user administration after backend contract exists
  - [ ] 15.1 Create backend user-management endpoints if selected for MVP
    - List, create, update, role change, status change, optional password reset.
    - Add backend RBAC tests.
    - _Requirements: 11.1, 11.2, 11.3, 11.4_

  - [ ] 15.2 Implement `UserAdminHttpAdapter` and `UserAdminService`
    - Add adapter and facade tests.
    - _Requirements: 11.1, 11.3, 14.1_

  - [ ] 15.3 Build user list and form pages
    - Never display stored passwords.
    - Support role and status management.
    - _Requirements: 11.1, 11.2, 11.5_

---

## Wave 8: Security, Verification, and Documentation

- [ ] 16. Security verification
  - [ ] 16.1 Add route-role tests
    - Verify forbidden admin routes display unauthorized state.
    - _Requirements: 1.4, 13.1, 13.2_

  - [ ] 16.2 Add sensitive-data logging checks
    - Review admin code for console logging of JWT, refresh token, password, CPF, or sensitive customer data.
    - _Requirements: 13.4_

  - [ ] 16.3 Verify mutating operations require explicit intent
    - Confirm destructive/deactivation/cancellation flows use confirmation dialogs or explicit forms.
    - _Requirements: 13.3_

- [ ] 17. Automated test coverage
  - [ ] 17.1 Run frontend unit/property tests
    - `npm test` from `frontend/`.
    - _Requirements: 14.1, 14.2, 14.3_

  - [ ] 17.2 Run backend tests for changed endpoints
    - `./mvnw test` or scoped Maven commands.
    - _Requirements: 14.5_

  - [ ] 17.3 Expand frontend-backend E2E smoke script
    - Validate login, product listing, stock lookup, consignment list access, and protected API behavior through Docker Compose.
    - _Requirements: 14.4_

  - [ ] 17.4 Run Docker Compose smoke verification
    - `./scripts/e2e-frontend-backend.sh`
    - Report Docker/network limitations if unavailable.
    - _Requirements: 14.4, 14.5_

- [ ] 18. Documentation
  - [ ] 18.1 Document admin module setup and role access
    - Update README or `.github/docs` with admin routes, required roles, and test commands.
    - _Requirements: 14.5_

  - [ ] 18.2 Document backend contract gaps and decisions
    - Keep a clear checklist of endpoints needed before each admin feature can be considered complete.
    - Add ADR if dashboard aggregation, consignment stock allocation, or POS quantity semantics require an architecture decision.
    - _Requirements: 2.5, 6.2, 10.12, 11.4_

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 1, "tasks": ["1.1", "1.2", "1.3", "1.4", "2.1", "2.2", "2.3"] },
    { "id": 2, "tasks": ["3.1", "3.2", "3.3", "4.1", "4.2", "4.3", "4.4", "4.5"] },
    { "id": 3, "tasks": ["5.1", "5.2", "6.1", "6.2", "6.3", "6.4"] },
    { "id": 4, "tasks": ["7.1", "7.2", "8.1", "8.2", "8.3", "8.4.1", "8.4.2", "8.5.1", "8.5.2", "8.5.3", "8.5.4"] },
    { "id": 5, "tasks": ["9.1", "9.2", "9.3", "9.4", "10.1", "10.2", "10.3", "10.4", "10.5"] },
    { "id": 6, "tasks": ["11.1", "11.2", "11.3", "12.1", "12.2", "12.3", "13.1", "13.2"] },
    { "id": 7, "tasks": ["14.1", "14.2", "15.1", "15.2", "15.3"] },
    { "id": 8, "tasks": ["16.1", "16.2", "16.3", "17.1", "17.2", "17.3", "17.4", "18.1", "18.2"] }
  ]
}
```

## Notes

- This feature intentionally focuses on internal ERP administration and physical-store workflows.
- Storefront checkout and customer self-service account remain separate specs unless pulled into admin scope.
- Consignment lifecycle and stock-origin semantics are defined by `.kiro/specs/consignment-management`; this spec only defines the admin frontend integration.
- Backend Contract Gap tasks should be resolved before implementing frontend screens that depend on missing endpoints.
- All critical business rules remain enforced in the backend.
