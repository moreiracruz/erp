# Requirements Document: Consignment Management

## Introduction

This document defines the requirements for consignment management in the clothing retail ERP. The business needs to handle both:

1. **Consignacao recebida**: products physically received from a consignor/supplier for temporary sale or evaluation. They enter physical stock while the consignment remains open, but are not owned stock until purchased definitively.
2. **Consignacao enviada**: products physically sent to another party for temporary sale or evaluation. They leave store physical stock while the consignment remains open, but remain economically controlled by the store until sold, purchased, or returned.

Consignment must be modeled as a first-class module because it has its own lifecycle, party, due date, pending quantities, settlement, and integration with inventory, sales, finance, and the admin frontend.

This specification intentionally does not define tax, invoice, legal ownership, or statutory accounting rules. Those must be validated separately before fiscal implementation.

## Glossary

- **Consignment**: Temporary commercial arrangement involving products that may later be sold, purchased definitively, or returned.
- **Consignor**: Party that owns or provides goods in a received consignment.
- **Consignee**: Party that receives goods in a sent consignment.
- **Consignacao recebida**: Goods received into the store from another party.
- **Consignacao enviada**: Goods sent out from the store to another party.
- **Physical stock**: Quantity physically present in the store.
- **Owned stock**: Quantity definitively owned by the store.
- **Consigned-in stock**: Quantity physically in the store from received consignments.
- **Consigned-out stock**: Quantity sent out under consignment and not physically in the store.
- **Settlement**: Operational financial record that resolves sold/purchased consigned items.
- **Definitive purchase**: Conversion of received consignment into owned stock.
- **Definitive sale**: Sale or confirmation that removes consigned-out goods from store ownership/control.

## Requirements

### Requirement 1: Create Received Consignment

**User Story:** As a manager or stock operator, I want to register products received in consignment, so that they enter physical stock while remaining identifiable as consigned goods.

#### Acceptance Criteria

1. WHEN a ROLE_MANAGER or ROLE_STOCK creates a received consignment, THE System SHALL store party identifier, opened date, optional due date, notes, and items.
2. Each consignment item SHALL reference an existing variant UUID and specify quantity between 1 and 100000.
3. WHEN a received consignment is opened, THE Inventory_Service SHALL increase physical stock and consigned-in stock for each item atomically.
4. THE System SHALL record an inventory movement with operation type `CONSIGNACAO_RECEBIDA` for each item.
5. IF any item references an inactive or nonexistent variant, THEN the creation SHALL fail with HTTP 422 and no stock movement SHALL be committed.
6. IF any item quantity is invalid, THEN the creation SHALL fail with HTTP 422 and no stock movement SHALL be committed.

### Requirement 2: Create Sent Consignment

**User Story:** As a manager or stock operator, I want to send products in consignment to another party, so that they leave physical stock but remain tracked until settlement or return.

#### Acceptance Criteria

1. WHEN a ROLE_MANAGER or ROLE_STOCK creates a sent consignment, THE System SHALL store party identifier, opened date, optional due date, notes, and items.
2. WHEN a sent consignment is opened, THE Inventory_Service SHALL decrease physical stock and owned available stock for each item atomically and increase consigned-out stock.
3. IF available owned stock is insufficient for any item, THEN the creation SHALL fail with HTTP 422 returning the current available owned stock, and no item movement SHALL be committed.
4. THE System SHALL record an inventory movement with operation type `CONSIGNACAO_ENVIADA` for each item.
5. Sent consignment items SHALL NOT be available for local PDV sale while they are outside the store.

### Requirement 3: Consignment Lifecycle

**User Story:** As a manager, I want consignments to have explicit statuses, so that pending, settled, returned, and cancelled quantities are auditable.

#### Acceptance Criteria

1. A consignment SHALL have one of the statuses: `ABERTA`, `PARCIALMENTE_ACERTADA`, `FINALIZADA`, or `CANCELADA`.
2. A consignment item SHALL track original quantity, sold quantity, purchased quantity, returned quantity, and pending quantity.
3. THE pending quantity SHALL always equal `quantity - soldQuantity - purchasedQuantity - returnedQuantity`.
4. IF a quantity update would make pending quantity negative, THEN THE System SHALL reject the operation with HTTP 422.
5. A consignment SHALL move to `PARCIALMENTE_ACERTADA` when at least one item has sold, purchased, or returned quantity and at least one item still has pending quantity.
6. A consignment SHALL move to `FINALIZADA` only when all items have pending quantity equal to zero.
7. A consignment SHALL be cancellable only while no item has been sold, purchased, or returned.

### Requirement 4: Return Received Consignment

**User Story:** As a stock operator, I want to return unsold received consignment goods, so that physical stock is corrected and pending quantities are reduced.

#### Acceptance Criteria

1. WHEN returning received consignment items, THE System SHALL allow return only up to each item's pending quantity.
2. WHEN a received consignment return is confirmed, THE Inventory_Service SHALL decrease physical stock and consigned-in stock atomically.
3. THE System SHALL record inventory movement type `DEVOLUCAO_CONSIGNACAO_RECEBIDA`.
4. IF physical consigned-in stock is insufficient, THEN THE System SHALL reject the return with HTTP 422 and leave all quantities unchanged.
5. A return SHALL NOT create financial revenue or expense by itself.

### Requirement 5: Return Sent Consignment

**User Story:** As a stock operator, I want to receive back unsold sent consignment goods, so that they re-enter physical stock.

#### Acceptance Criteria

1. WHEN returning sent consignment items, THE System SHALL allow return only up to each item's pending quantity.
2. WHEN a sent consignment return is confirmed, THE Inventory_Service SHALL increase physical stock and owned stock atomically and decrease consigned-out stock.
3. THE System SHALL record inventory movement type `RETORNO_CONSIGNACAO_ENVIADA`.
4. A return SHALL NOT create financial revenue or expense by itself.

### Requirement 6: Sell Received Consignment Through PDV

**User Story:** As a cashier, I want the PDV to sell items that may be owned or consigned, so that checkout remains fast while backend stock origin remains correct.

#### Acceptance Criteria

1. WHEN a sale item is added and both owned stock and received consigned stock exist for the same variant, THE backend SHALL decide stock origin according to a configured stock-allocation policy.
2. Supported allocation policies SHALL include at least: `OWNED_FIRST`, `CONSIGNED_IN_FIRST`, and `MANUAL`.
3. IF policy is `MANUAL`, THEN the PDV SHALL require the operator to select the origin before adding the item.
4. WHEN a received consignment item is sold, THE System SHALL decrease consigned-in pending quantity and consigned-in stock.
5. WHEN the sale is finalized, THE System SHALL create a settlement pending amount payable to the consignor, based on the consignment item's settlement value or configured commission rule.
6. IF a sale is cancelled before finalization, THE System SHALL restore consignment reservation/quantity state consistently.
7. THE PDV SHALL display whether an item was fulfilled from owned stock or consigned-in stock when the operator has ROLE_MANAGER or ROLE_STOCK; ROLE_CASHIER may see only operational messages unless configured otherwise.

### Requirement 7: Confirm Sale of Sent Consignment

**User Story:** As a manager, I want to confirm that goods sent in consignment were sold by the external party, so that stock and finance are settled.

#### Acceptance Criteria

1. WHEN confirming sold sent consignment items, THE System SHALL allow sale confirmation only up to each item's pending quantity.
2. WHEN sold sent consignment items are confirmed, THE System SHALL decrease consigned-out stock and increase sold quantity for each item.
3. THE System SHALL create an operational revenue or receivable record according to the configured settlement mode.
4. THE System SHALL record an inventory movement type `VENDA_CONSIGNACAO_ENVIADA`.
5. IF the same confirmation request is retried with the same idempotency key, THEN THE System SHALL NOT duplicate stock or financial effects.

### Requirement 8: Definitive Purchase of Received Consignment

**User Story:** As a manager, I want to buy received consignment goods definitively, so that they become owned stock.

#### Acceptance Criteria

1. WHEN purchasing received consignment items definitively, THE System SHALL allow purchase only up to each item's pending quantity.
2. WHEN purchase is confirmed, THE System SHALL decrease consigned-in stock and increase owned stock without changing physical stock.
3. THE System SHALL create a financial payable/expense record or purchase settlement according to the configured settlement mode.
4. THE System SHALL record inventory movement type `COMPRA_DEFINITIVA_CONSIGNACAO`.
5. Definitive purchase SHALL update purchased quantity and pending quantity for each item.

### Requirement 9: Reserve Consignment Items

**User Story:** As a manager or cashier, I want to reserve consigned or owned items for a sale/order, so that pending transactions do not oversell stock.

#### Acceptance Criteria

1. THE Inventory_Service SHALL support reservation by stock origin: owned, consigned-in, or allocation policy.
2. A reservation SHALL decrease available stock for its origin while preserving physical stock until finalization.
3. IF a reservation expires or the sale is cancelled, THE System SHALL release the reservation and restore origin-specific availability.
4. IF available stock for the selected origin is insufficient, THEN THE System SHALL reject the reservation with HTTP 422.
5. Existing sale reservation behavior SHALL remain compatible for variants without consigned stock.

### Requirement 10: Consignment Settlement

**User Story:** As a finance user, I want to settle consignment obligations, so that pending payments or receivables are tracked.

#### Acceptance Criteria

1. WHEN consigned-in goods are sold or purchased definitively, THE System SHALL create a settlement entry payable to the consignor unless the settlement mode is configured as manual-only.
2. WHEN consigned-out goods are sold by the external party, THE System SHALL create a settlement entry receivable or revenue according to the settlement mode.
3. A settlement SHALL reference consignment UUID, item UUIDs, party UUID, amount, status, and created timestamp.
4. Settlement status SHALL be one of `PENDENTE`, `PAGO`, `RECEBIDO`, or `CANCELADO`.
5. A settlement SHALL be idempotent for a given business event.
6. The System SHALL NOT define fiscal invoice behavior in this feature.

### Requirement 11: Consignment Search and Reporting

**User Story:** As a manager, I want to find consignments and see pending quantities and due dates, so that I can manage follow-up and settlement.

#### Acceptance Criteria

1. THE System SHALL list consignments by type, status, party, date range, due date, and variant.
2. THE System SHALL expose details for a consignment including items, original quantities, sold quantities, purchased quantities, returned quantities, pending quantities, and settlement status.
3. THE System SHALL expose a report of due or overdue open consignments.
4. THE System SHALL expose a report of pending settlement amounts grouped by party.
5. Query endpoints SHALL be paginated.

### Requirement 12: Admin Frontend for Consignments

**User Story:** As a store operator, I want an admin UI for consignments, so that I can manage received and sent consignments without direct database access.

#### Acceptance Criteria

1. THE Admin_System SHALL add a protected Consignacoes module visible to ROLE_MANAGER, ROLE_STOCK, and ROLE_FINANCE with role-specific actions.
2. ROLE_MANAGER SHALL access all consignment actions.
3. ROLE_STOCK SHALL create received/sent consignments and register returns but SHALL NOT mark settlements as paid/received.
4. ROLE_FINANCE SHALL view consignments and manage settlements but SHALL NOT alter physical stock movements.
5. THE UI SHALL provide screens for list, detail, create received consignment, create sent consignment, return/settle actions, and due/pending reports.
6. All mutating frontend actions SHALL call backend APIs; no consignment quantity update SHALL be frontend-only.

### Requirement 13: Authorization and Security

**User Story:** As a technical lead, I want consignment actions protected by role, so that stock and financial consequences are controlled.

#### Acceptance Criteria

1. All consignment API endpoints SHALL require authentication.
2. ROLE_MANAGER SHALL have read/write access to all consignment endpoints.
3. ROLE_STOCK SHALL have access to stock-impacting consignment creation and returns, but not financial settlement payment/receipt confirmation.
4. ROLE_FINANCE SHALL have read access and settlement payment/receipt access, but not physical stock movement actions.
5. ROLE_CASHIER SHALL only interact with consigned stock through the Sales/PDV flow.
6. The System SHALL not log sensitive party data, settlement details beyond operational IDs, JWTs, or payment credentials.

### Requirement 14: Database Integrity and Concurrency

**User Story:** As a developer, I want database constraints and transactional behavior, so that consignment stock cannot become inconsistent.

#### Acceptance Criteria

1. Consignment item quantities SHALL have database constraints ensuring non-negative original, sold, purchased, returned, and pending quantities.
2. Settlement records SHALL have idempotency keys for event-based or request-based creation.
3. Stock-origin counters SHALL be updated in the same transaction as consignment item quantity updates.
4. Critical stock-origin updates SHALL use pessimistic locking or equivalent atomic updates to prevent overselling.
5. The schema SHALL index consignment type/status, party UUID, due date, variant UUID, and settlement status.

### Requirement 15: Observability and Audit

**User Story:** As a manager and technical lead, I want consignment actions to be auditable, so that stock and settlement changes can be investigated.

#### Acceptance Criteria

1. Every consignment lifecycle action SHALL record actor UUID, timestamp, action type, consignment UUID, and affected item UUIDs.
2. The System SHALL emit structured logs for consignment creation, return, sale confirmation, definitive purchase, settlement creation, and close/cancel.
3. The System SHALL emit domain events for major lifecycle actions.
4. Metrics SHALL include counters for opened consignments, finalized consignments, returned items, settlement created, and settlement paid/received.

### Requirement 16: Testing and Verification

**User Story:** As a developer, I want automated tests for consignment invariants, so that changes do not break stock or settlement correctness.

#### Acceptance Criteria

1. Domain tests SHALL verify pending quantity invariants.
2. Persistence/integration tests SHALL verify transactional stock effects for received and sent consignments.
3. Sales integration tests SHALL verify selling received consignment creates settlement without duplicating effects.
4. Finance integration tests SHALL verify settlement idempotency.
5. Frontend tests SHALL verify role-based menu/action visibility and form validation.
6. E2E smoke tests SHALL cover at least: create received consignment, view stock impact, return item, and verify pending quantity.

## Assumptions

1. Party identity can initially be represented by a UUID and display name, even if supplier/partner management becomes a separate module later.
2. Consignment settlement is operational finance, not fiscal invoicing.
3. Existing inventory stock counters can be extended rather than replaced, but origin-specific counters must remain consistent.
4. The first implementation supports variant-level consignments, not product-only consignments.

## Open Questions

1. For PDV allocation, should the default be `OWNED_FIRST`, `CONSIGNED_IN_FIRST`, or manual selection?
2. For received consignment sales, is the payable amount a fixed cost per item, a percentage commission, or negotiated per consignment item?
3. For sent consignments, does confirmation create a sale to the consignee, a receivable from the consignee, or only an operational settlement?
4. Should due-date expiration automatically block sale/return actions, or only flag the consignment as overdue?
5. Should supplier/partner cadastro be implemented before consignment, or can party UUID/name be stored directly for MVP?
