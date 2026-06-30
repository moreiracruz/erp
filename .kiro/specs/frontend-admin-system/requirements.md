# Requirements Document: Frontend Administracao do Sistema

## Introduction

This document defines the requirements for the administrative ERP frontend used by store managers, cashiers, stock operators, and finance users. The feature turns the existing backend modules (Auth, Product, Inventory, Consignment, Sales/POS, Customer, Finance, Pricing, and Product Images) into a coherent backoffice experience for physical-store operation.

The administration frontend is distinct from the public storefront. Storefront pages are public and customer-facing; admin pages are protected operational tools and must enforce role-based access in the backend while also hiding unavailable UI actions in the frontend.

## Glossary

- **Admin_System**: The Angular administrative area of the ERP.
- **Backoffice**: Internal interface for store operations.
- **Admin_Shell**: Protected layout with sidebar, topbar, breadcrumbs, and user/session controls.
- **Cadastro**: CRUD-style registration and maintenance screen.
- **PDV**: Physical-store point of sale.
- **Consignacao recebida**: Goods physically received from another party, available according to policy, but not owned until purchased definitively.
- **Consignacao enviada**: Goods physically sent to another party, outside store physical stock, but tracked until sold, purchased, or returned.
- **Acerto de consignacao**: Operational payable/receivable settlement created by sold or purchased consigned items.
- **RBAC**: Role-based access control using ROLE_MANAGER, ROLE_CASHIER, ROLE_STOCK, and ROLE_FINANCE.
- **HTTP Adapter**: Angular infrastructure service implementing a domain port through backend REST API calls.
- **Backend Contract Gap**: Missing or insufficient backend endpoint/DTO needed for frontend integration.

## Requirements

### Requirement 1: Admin Shell and Navigation

**User Story:** As an authenticated operator, I want a consistent administration layout, so that I can access the ERP modules allowed for my role.

#### Acceptance Criteria

1. WHEN an unauthenticated user navigates to any `/admin`, `/dashboard`, `/inventory`, `/consignments`, `/pos`, `/finance`, `/pricing`, `/customers`, or `/users` route, THE Admin_System SHALL redirect to the login page with a `returnUrl`.
2. WHEN an authenticated user navigates to the Admin_System, THE Admin_System SHALL render a protected layout containing sidebar navigation, topbar, current user summary, logout action, and breadcrumb/title region.
3. THE sidebar SHALL show only modules allowed for the authenticated user's role.
4. IF a user manually navigates to a route outside their role permissions, THEN the Admin_System SHALL show an unauthorized page and the backend SHALL still return HTTP 403 for forbidden API calls.
5. WHEN the viewport width is below 768px, THE Admin_Shell SHALL collapse navigation behind a menu button and preserve all actions with touch targets of at least 44x44px.

### Requirement 2: Operational Dashboard

**User Story:** As a manager, I want a dashboard with operational indicators, so that I can understand the store situation quickly.

#### Acceptance Criteria

1. WHEN a ROLE_MANAGER opens the dashboard, THE Admin_System SHALL display today's gross sales total, number of finalized sales, average ticket, low-stock count, due/overdue consignment count, pending settlement count, and pending failed domain events count when available.
2. WHEN a ROLE_FINANCE opens the dashboard, THE Admin_System SHALL display financial and pending-settlement indicators without product-management or inventory-write shortcuts.
3. WHEN the dashboard API is unavailable, THE Admin_System SHALL show a user-friendly error state with a retry action.
4. THE dashboard SHALL not calculate authoritative financial totals from frontend-only data; totals must come from backend APIs.
5. IF no dedicated dashboard endpoint exists, THEN the implementation SHALL create a backend query/read endpoint or explicitly compose existing read endpoints in a documented adapter.

### Requirement 3: Product and Variant Administration

**User Story:** As a manager, I want to manage products, variants, prices, and barcodes, so that the catalog and PDV operate with accurate data.

#### Acceptance Criteria

1. WHEN a ROLE_MANAGER opens product administration, THE Admin_System SHALL load products from the backend Product API, not from hardcoded local arrays.
2. THE product list SHALL support text search by name, brand, or category, and SHALL show active status, category, brand, and variant count when available.
3. WHEN creating or editing a product, THE Admin_System SHALL validate required frontend fields before submission and SHALL display backend validation errors returned as HTTP 422.
4. WHEN creating a variant, THE Admin_System SHALL collect SKU, barcode, size, color, sale price, cost, and active status.
5. IF the backend rejects duplicate SKU or barcode, THEN the Admin_System SHALL show the conflicting field message without losing user-entered form data.
6. WHEN a product is deactivated, THE Admin_System SHALL ask for confirmation and then call the backend deactivation endpoint.
7. THE Admin_System SHALL not enforce product uniqueness only in the frontend; backend validation remains authoritative.

### Requirement 4: Product Image Management

**User Story:** As a manager, I want to upload and organize product images, so that the storefront and admin screens show accurate product photos.

#### Acceptance Criteria

1. WHEN a ROLE_MANAGER opens a persisted product detail screen, THE Admin_System SHALL show image upload and image grid controls linked to the product UUID.
2. WHEN uploading images, THE Admin_System SHALL use multipart/form-data against `/api/v1/products/{uuid}/images`.
3. THE Admin_System SHALL validate client-side file type and size for fast feedback while preserving backend validation as authoritative.
4. WHEN reorder, set-main, or delete operations succeed, THE Admin_System SHALL refresh or update the image list to match backend state.
5. IF an image operation fails with HTTP 401 or 403, THEN the Admin_System SHALL show an authorization/session message and not retry automatically.

### Requirement 5: Inventory Administration

**User Story:** As a stock operator, I want to view stock and register entries or withdrawals, so that inventory remains accurate.

#### Acceptance Criteria

1. WHEN a ROLE_STOCK or ROLE_MANAGER opens inventory, THE Admin_System SHALL support searching variants by SKU or barcode through backend endpoints.
2. WHEN a variant is selected, THE Admin_System SHALL display physical stock, reserved stock, and available stock from backend inventory APIs.
3. WHEN registering stock entry or withdrawal, THE Admin_System SHALL submit quantity and actor context to backend inventory APIs.
4. IF quantity is outside the allowed range [1, 100000], THEN THE Admin_System SHALL show a validation error and backend SHALL still reject invalid submissions.
5. IF a withdrawal would make stock negative, THEN THE Admin_System SHALL show the current physical stock returned by the backend.
6. THE inventory movement history SHALL be readable by ROLE_STOCK and ROLE_MANAGER.
7. WHEN origin-aware stock is available, THE Admin_System SHALL display owned stock, consigned-in stock, consigned-out stock, and origin-specific reserved stock without replacing the existing physical/reserved/available summary.

### Requirement 6: Physical Sales / PDV

**User Story:** As a cashier, I want a fast PDV screen integrated with product, inventory, pricing, and sales APIs, so that physical sales are recorded correctly.

#### Acceptance Criteria

1. WHEN a ROLE_CASHIER or ROLE_MANAGER opens the PDV, THE Admin_System SHALL create or resume a backend sale in status `EM_ANDAMENTO`.
2. WHEN the cashier scans or types a SKU/barcode, THE Admin_System SHALL search the backend product catalog and add the item through the backend Sales API, allowing inventory reservation to happen server-side.
3. IF the product is not found, THEN THE Admin_System SHALL show "Produto nao encontrado".
4. IF stock is insufficient, THEN THE Admin_System SHALL show the available stock value returned by the backend.
5. THE Admin_System SHALL display subtotal, discount, tax, and total values returned or confirmed by the backend; it SHALL NOT trust frontend totals as authoritative.
6. WHEN finalizing a sale, THE Admin_System SHALL support DINHEIRO, DEBITO, CREDITO, and PIX and map labels consistently to backend payment-method values.
7. WHEN payment method is DINHEIRO, THE Admin_System SHALL collect amount paid and display backend-computed change.
8. WHEN cancelling a sale, THE Admin_System SHALL require a cancellation reason and call the backend cancellation endpoint to release reservations.
9. Keyboard shortcuts SHALL be available for common cashier actions: focus search, finalize, cancel, and increase/decrease selected item quantity.
10. WHEN a sale item can be fulfilled from owned stock or received consignment stock, THE Admin_System SHALL follow the backend allocation response; IF backend policy is manual, THEN the PDV SHALL require the operator to select the stock origin.
11. THE PDV SHALL display backend-provided stock origin for sale items when allowed by the authenticated user's role.

### Requirement 7: Customer Administration

**User Story:** As a manager or cashier, I want to register and find customers, so that sales can optionally be associated with a customer.

#### Acceptance Criteria

1. WHEN a ROLE_MANAGER or ROLE_CASHIER opens customers, THE Admin_System SHALL search customers by CPF, partial name, or UUID using backend Customer APIs.
2. WHEN registering a customer, THE Admin_System SHALL collect full name, CPF, optional email, optional phone, and optional birth date.
3. IF CPF is invalid or duplicated, THEN THE Admin_System SHALL show the backend validation message without exposing existing customer data.
4. WHEN a ROLE_MANAGER deactivates a customer, THE Admin_System SHALL ask for confirmation and call the backend deactivation endpoint.
5. THE PDV SHALL support optional customer association and SHALL reject inactive customers using backend responses.

### Requirement 8: Finance Administration

**User Story:** As a finance user, I want to view cash flow and register expenses, so that the store's finances can be monitored.

#### Acceptance Criteria

1. WHEN a ROLE_FINANCE or ROLE_MANAGER opens finance, THE Admin_System SHALL show cash-flow filters for date range and load backend cash-flow reports.
2. IF the start date is after the end date or the range exceeds 366 days, THEN THE Admin_System SHALL show validation feedback and backend SHALL still reject invalid queries.
3. WHEN registering an expense, THE Admin_System SHALL collect amount, description, category, competence date, and optional payment method when applicable.
4. IF backend validation fails, THEN THE Admin_System SHALL display every failed constraint returned by the backend.
5. THE Admin_System SHALL distinguish automatic sale revenue entries from manual expense entries in the UI.

### Requirement 9: Pricing, Campaigns, and Coupons

**User Story:** As a manager, I want to manage campaigns and coupons, so that discounts are configured centrally and applied by the backend.

#### Acceptance Criteria

1. WHEN a ROLE_MANAGER opens pricing, THE Admin_System SHALL display campaigns and coupons from backend Pricing APIs.
2. WHEN creating a campaign, THE Admin_System SHALL collect type, target, discount value, cashback percentage when applicable, start date, end date, and active status.
3. IF the backend detects campaign conflict, THEN THE Admin_System SHALL show "Conflito de campanha".
4. WHEN creating a coupon, THE Admin_System SHALL collect code, discount configuration, validity window, maximum usage, and active status.
5. THE Admin_System SHALL not calculate final sale discount authoritatively; discount calculation remains a backend responsibility.

### Requirement 10: Consignment Administration

**User Story:** As a manager, stock operator, or finance user, I want to manage received and sent consignments, so that temporary stock, returns, definitive purchases/sales, and settlements are controlled.

#### Acceptance Criteria

1. WHEN a ROLE_MANAGER, ROLE_STOCK, or ROLE_FINANCE opens Consignacoes, THE Admin_System SHALL show a role-filtered consignment module in the admin menu.
2. ROLE_MANAGER SHALL be able to create received consignments, create sent consignments, register returns, confirm sent-consignment sales, purchase received consignments definitively, close/cancel consignments, and manage settlements.
3. ROLE_STOCK SHALL be able to create received/sent consignments and register physical returns, but SHALL NOT mark settlements as paid or received.
4. ROLE_FINANCE SHALL be able to view consignments, reports, and settlement panels, and mark settlements as paid/received, but SHALL NOT perform physical stock movements.
5. THE consignment list SHALL support filters by type, status, party, date range, due date, and variant when supported by backend APIs.
6. THE consignment detail screen SHALL show original, sold, purchased, returned, and pending quantities for every item.
7. WHEN opening a received consignment, THE Admin_System SHALL collect party, optional due date, notes, and variant items with quantities and settlement values when required by the backend settlement policy.
8. WHEN opening a sent consignment, THE Admin_System SHALL collect party, optional due date, notes, and variant items with quantities, and SHALL display backend validation for insufficient owned stock.
9. WHEN registering return, purchase, or sent-sale confirmation actions, THE Admin_System SHALL preserve form data after HTTP 422 and display backend pending-quantity errors.
10. THE settlement panel SHALL show pending, paid, received, and cancelled settlements returned by the backend.
11. THE consignment UI SHALL not implement fiscal invoice behavior; fiscal/accounting legal behavior remains out of scope until a separate validated spec exists.
12. THE Admin_System SHALL reference the `consignment-management` spec for lifecycle and backend API semantics.

### Requirement 11: User and Role Administration

**User Story:** As a manager, I want to manage employees and their roles, so that system access is controlled.

#### Acceptance Criteria

1. WHEN a ROLE_MANAGER opens user administration, THE Admin_System SHALL list users with username, role, active status, failed-attempt state when available, and lock status when available.
2. WHEN creating a user, THE Admin_System SHALL collect username/email, temporary password, role, and active status.
3. WHEN changing a user's role or active status, THE Admin_System SHALL call backend user-management endpoints.
4. IF backend user-management endpoints do not exist, THEN this requirement SHALL be treated as a Backend Contract Gap before frontend implementation begins.
5. THE Admin_System SHALL never store or display user passwords after creation.

### Requirement 12: Cross-Cutting API Integration

**User Story:** As a developer, I want admin screens to use stable ports and adapters, so that frontend business flows remain testable and decoupled from HTTP details.

#### Acceptance Criteria

1. THE Admin_System SHALL define frontend ports for ProductAdmin, Inventory, Consignment, Sales, Customer, Finance, Pricing, Dashboard, and UserAdmin access.
2. THE HTTP adapters SHALL be the only layer that knows endpoint URLs and request/response DTO shapes.
3. Components SHALL use facade/application services rather than injecting `HttpClient` directly.
4. Every admin HTTP adapter SHALL map HTTP 401, 403, 404, 409/422, and network failures into user-friendly UI states.
5. The JWT interceptor SHALL attach Authorization headers to protected admin calls and SHALL continue excluding login and refresh endpoints.

### Requirement 13: Security and Auditability

**User Story:** As a technical lead, I want admin actions to be secure and auditable, so that operational data is protected.

#### Acceptance Criteria

1. THE Admin_System SHALL rely on backend RBAC for all protected operations.
2. THE Admin_System SHALL hide forbidden actions for better UX but SHALL NOT treat frontend hiding as authorization.
3. Mutating actions for products, inventory, consignments, sales cancellation, finance, pricing, and user management SHALL include explicit user intent through forms or confirmation dialogs.
4. The frontend SHALL not log JWTs, refresh tokens, passwords, CPF values, or sensitive customer data to the console.
5. Backend APIs SHALL receive authenticated actor context from JWT; the frontend SHALL NOT allow actor UUID spoofing in request bodies unless the backend contract explicitly requires and validates it.

### Requirement 14: Testing and Verification

**User Story:** As a developer, I want automated coverage for admin flows, so that integration regressions are caught early.

#### Acceptance Criteria

1. Each admin port adapter SHALL have focused HTTP tests verifying URL, method, body mapping, and error mapping.
2. Each facade service SHALL have unit tests for loading, success, validation, and error states.
3. Critical pure functions (role menu filtering, payment method mapping, totals display formatting, date range validation, consignment pending-quantity display, and consignment role-action filtering) SHALL have property-based tests.
4. At least one frontend-backend E2E smoke test SHALL validate login, product listing, stock lookup, consignment list access, and protected-route behavior through Docker Compose.
5. If Docker, network, or Testcontainers are unavailable, the verification report SHALL state which checks were skipped and why.

## Assumptions

1. The initial admin MVP targets physical-store operation before online order fulfillment.
2. Existing backend modules remain the source of truth for business rules.
3. Checkout storefront and customer self-service account are separate product areas and are not blockers for the admin MVP unless explicitly pulled into scope.
4. Existing Angular standalone components, signals, guards, and HTTP interceptor patterns remain the preferred frontend style.
5. Consignment lifecycle, stock-origin effects, and settlement rules are defined in `.kiro/specs/consignment-management`.

## Open Questions

1. Should user creation/password reset be implemented now, or should users be seeded/administered outside the UI for MVP?
2. Should dashboard data come from a new aggregated backend endpoint or from composing existing module endpoints?
3. Should the PDV allow quantity editing after reservation, or should it cancel/re-add reservations through backend calls?
4. Should financial reports include export to CSV/PDF in the MVP?
5. Should promotions/coupons be part of the first admin release or postponed until POS and inventory are stable?
6. Should Consignacoes be implemented before POS so manual stock-origin selection is available from the first integrated PDV release?
