# Implementation Plan: Consignment Management

## Overview

This plan adds a new consignment module and integrates it with inventory, sales, finance, domain events, and the admin frontend. Work is sequenced to protect stock invariants first, then add sales/finance integration, then admin UI and E2E verification.

---

## Wave 1: Specification Alignment and Architectural Decisions

- [ ] 1. Confirm business decisions before implementation
  - [ ] 1.1 Decide default PDV stock allocation policy
    - Choose `OWNED_FIRST`, `CONSIGNED_IN_FIRST`, or `MANUAL`.
    - Document decision in this spec or an ADR.
    - _Requirements: 6.1, 6.2_

  - [ ] 1.2 Decide settlement amount rule
    - Choose fixed settlement value per consignment item, percentage commission, manual amount, or combination.
    - Do not add fiscal/tax assumptions.
    - _Requirements: 6.5, 10.1, 10.2_

  - [ ] 1.3 Decide party model for MVP
    - Use direct party UUID/name snapshot or create Supplier/Partner module first.
    - _Requirements: 1.1, 2.1, 10.3_

  - [ ] 1.4 Decide due-date behavior
    - Informational alert only or operation-blocking after due date.
    - _Requirements: 11.3_

- [ ] 2. Add architecture documentation
  - [ ] 2.1 Create ADR for consignment as a separate module
    - Explain why it is not only an inventory movement type.
    - _Requirements: 3.1, 14.3_

  - [ ] 2.2 Update admin-system spec references
    - Add Consignacoes menu/module to frontend admin planning if needed.
    - _Requirements: 12.1_

---

## Wave 2: Database and Inventory Foundation

- [ ] 3. Create Flyway migrations for consignment schema
  - [ ] 3.1 Create `consignments` table
    - Include UUID, type, status, party UUID/name snapshot, opened date, due date, notes, created actor, timestamps.
    - Add indexes for type/status, party UUID, due date.
    - _Requirements: 1.1, 2.1, 11.1, 14.5_

  - [ ] 3.2 Create `consignment_items` table
    - Include variant UUID, SKU snapshot, original/sold/purchased/returned quantities, settlement value.
    - Add check constraint preventing sold+purchased+returned > quantity.
    - Add index on variant UUID.
    - _Requirements: 3.2, 3.3, 14.1_

  - [ ] 3.3 Create `consignment_settlements` table
    - Include direction, amount, status, party UUID, consignment UUID, idempotency key.
    - Add unique constraint on idempotency key.
    - _Requirements: 10.3, 10.4, 14.2_

  - [ ] 3.4 Create `consignment_audit_log` table
    - Store actor UUID, action type, payload JSONB, timestamp.
    - _Requirements: 15.1_

- [ ] 4. Extend inventory schema for stock origins
  - [ ] 4.1 Add origin-aware stock counters to `estoque_items`
    - `owned_stock`, `consigned_in_stock`, `consigned_out_stock`, `reserved_owned_stock`, `reserved_consigned_stock`.
    - Migrate existing physical/reserved stock into owned counters.
    - _Requirements: 9.1, 14.1_

  - [ ] 4.2 Add inventory movement operation types
    - `CONSIGNACAO_RECEBIDA`, `CONSIGNACAO_ENVIADA`, `DEVOLUCAO_CONSIGNACAO_RECEBIDA`, `RETORNO_CONSIGNACAO_ENVIADA`, `COMPRA_DEFINITIVA_CONSIGNACAO`, `VENDA_CONSIGNACAO_ENVIADA`.
    - _Requirements: 1.4, 2.4, 4.3, 5.3, 7.4, 8.4_

  - [ ] 4.3 Add database/integration tests for migration compatibility
    - Existing stock rows must keep equivalent physical and available stock after migration.
    - _Requirements: 14.1, 16.2_

---

## Wave 3: Backend Domain Model and Ports

- [ ] 5. Create `modules/consignment`
  - [ ] 5.1 Add Maven module and package structure
    - Domain, application, adapter in/out packages.
    - Wire into parent POM and bootstrap as needed.
    - _Requirements: 14.3_

  - [ ] 5.2 Implement domain enums and value objects
    - `ConsignmentType`, `ConsignmentStatus`, `SettlementStatus`, `SettlementDirection`, `StockAllocationPolicy`.
    - _Requirements: 3.1, 10.4_

  - [ ] 5.3 Implement `ConsignmentAgreement` and `ConsignmentItem`
    - Enforce pending quantity invariant in domain methods.
    - _Requirements: 3.2, 3.3, 3.4, 16.1_

  - [ ] 5.4 Implement `ConsignmentSettlement`
    - Enforce amount and status invariants.
    - _Requirements: 10.3, 10.4_

- [ ] 6. Define ports and commands
  - [ ] 6.1 Create inbound ports
    - Open received/sent, return received/sent, purchase received, confirm sent sale, settle, close, cancel, search.
    - _Requirements: 1-11_

  - [ ] 6.2 Create outbound ports
    - Repositories, inventory, product variant lookup, finance settlement, domain event publisher.
    - _Requirements: 1.5, 2.3, 10.1, 10.2_

  - [ ] 6.3 Create request/response records
    - Commands and read models for API and frontend consumption.
    - _Requirements: 11.2, 12.5_

---

## Wave 4: Inventory Integration

- [ ] 7. Implement inventory consignment port
  - [ ] 7.1 Add `InventoryConsignmentPort` adapter in inventory/infrastructure boundary
    - Methods for received open, sent open, received return, sent return, purchase conversion, sale confirmation, origin reservation.
    - Use row locking or atomic updates.
    - _Requirements: 1.3, 2.2, 4.2, 5.2, 8.2, 9.1, 14.4_

  - [ ] 7.2 Update inventory domain/model for origin-aware counters
    - Preserve existing `physicalStock`, `reservedStock`, and `availableStock` API semantics.
    - Add origin breakdown response fields.
    - _Requirements: 9.5_

  - [ ] 7.3 Add property tests for stock-origin invariants
    - Non-negative counters and derived physical/available consistency.
    - _Requirements: 14.1, 16.1_

  - [ ] 7.4 Add Testcontainers integration tests
    - Received open, sent open, returns, and purchase conversion with concurrent attempts.
    - _Requirements: 14.3, 14.4, 16.2_

---

## Wave 5: Consignment Use Cases

- [ ] 8. Implement opening use cases
  - [ ] 8.1 Implement `OpenReceivedConsignmentUseCase`
    - Validate variants, persist agreement/items, update inventory in one transaction, write audit log, emit event.
    - _Requirements: 1.1-1.6, 15.1, 15.3_

  - [ ] 8.2 Implement `OpenSentConsignmentUseCase`
    - Validate variants and owned availability, update inventory in one transaction, write audit log, emit event.
    - _Requirements: 2.1-2.5, 15.1, 15.3_

- [ ] 9. Implement return and conversion use cases
  - [ ] 9.1 Implement `ReturnReceivedConsignmentUseCase`
    - Validate pending quantity, update consignment, update inventory, audit, emit event.
    - _Requirements: 4.1-4.5_

  - [ ] 9.2 Implement `ReturnSentConsignmentUseCase`
    - Validate pending quantity, update consignment, update inventory, audit, emit event.
    - _Requirements: 5.1-5.4_

  - [ ] 9.3 Implement `PurchaseReceivedConsignmentUseCase`
    - Convert consigned-in stock to owned stock without changing physical stock.
    - Create settlement according to selected settlement policy.
    - _Requirements: 8.1-8.5, 10.1_

  - [ ] 9.4 Implement `ConfirmSentConsignmentSaleUseCase`
    - Validate pending quantity, update consigned-out stock, create receivable/revenue settlement idempotently.
    - _Requirements: 7.1-7.5, 10.2, 10.5_

- [ ] 10. Implement lifecycle and settlement use cases
  - [ ] 10.1 Implement `SettleConsignmentUseCase`
    - Mark payable as paid or receivable as received.
    - Enforce role-specific access at controller/security level.
    - _Requirements: 10.4, 13.3, 13.4_

  - [ ] 10.2 Implement `CloseConsignmentUseCase`
    - Reject closing while any pending quantity remains.
    - _Requirements: 3.6, 16.1_

  - [ ] 10.3 Implement `CancelConsignmentUseCase`
    - Allow only when no item has sold/purchased/returned quantities.
    - Reverse initial stock movement if cancellation is accepted.
    - _Requirements: 3.7_

  - [ ] 10.4 Implement `SearchConsignmentsUseCase`
    - Support filters by type, status, party, date range, due date, variant, and pagination.
    - _Requirements: 11.1, 11.2, 11.3, 11.5_

---

## Wave 6: Persistence and REST API

- [ ] 11. Implement JPA persistence adapters
  - [ ] 11.1 Create JPA entities and Spring Data repositories
    - Consignments, items, settlements, audit log.
    - _Requirements: 14.1, 14.2_

  - [ ] 11.2 Implement repository adapters
    - Map domain to persistence and back.
    - Preserve domain invariants on restore.
    - _Requirements: 14.3_

  - [ ] 11.3 Add persistence tests
    - Quantity constraints, idempotency keys, search indexes/query behavior.
    - _Requirements: 14.1, 14.2, 16.2_

- [ ] 12. Implement REST controller
  - [ ] 12.1 Create `ConsignmentController`
    - Expose all endpoints under `/api/v1/consignments`.
    - Map commands/responses.
    - _Requirements: 11.1, 11.2, 12.6_

  - [ ] 12.2 Add method security
    - ROLE_MANAGER full access, ROLE_STOCK stock actions, ROLE_FINANCE settlement actions, ROLE_CASHIER only through Sales.
    - _Requirements: 13.1-13.5_

  - [ ] 12.3 Add controller tests
    - Request mapping, validation errors, authorization, response shapes.
    - _Requirements: 13.1-13.5, 16.2_

---

## Wave 7: Sales and Finance Integration

- [ ] 13. Integrate Sales/PDV with consigned-in stock
  - [ ] 13.1 Define `ConsignmentAllocationPort` for Sales
    - Allocate origin using configured policy.
    - Support manual origin when policy is `MANUAL`.
    - _Requirements: 6.1, 6.2, 6.3_

  - [ ] 13.2 Update Add Item / Finalize Sale flow
    - Reserve origin-specific stock and create settlement after finalization.
    - Restore state on cancellation or failed finalization.
    - _Requirements: 6.4, 6.5, 6.6, 9.1-9.5_

  - [ ] 13.3 Add Sales integration tests
    - Owned-only sale remains unchanged.
    - Consigned-in sale creates settlement.
    - Cancellation restores origin reservation.
    - _Requirements: 6.4-6.6, 16.3_

- [ ] 14. Integrate Finance settlements
  - [ ] 14.1 Implement `FinanceSettlementPort`
    - Create payable/receivable operational entries idempotently.
    - _Requirements: 10.1-10.5_

  - [ ] 14.2 Add finance settlement tests
    - Duplicate event/idempotency key does not duplicate entries.
    - _Requirements: 10.5, 16.4_

---

## Wave 8: Domain Events, Observability, and Audit

- [ ] 15. Implement consignment domain events
  - [ ] 15.1 Create event payload records
    - Opened, returned, purchased, sold, settlement created, finalized, cancelled.
    - _Requirements: 15.3_

  - [ ] 15.2 Publish events from use cases
    - Use existing event envelope/event bus pattern.
    - _Requirements: 15.3_

  - [ ] 15.3 Add event property tests
    - Event envelopes contain non-null eventId, eventType, occurredAt, and payload.
    - _Requirements: 15.3, 16.1_

- [ ] 16. Add observability and audit
  - [ ] 16.1 Write audit log entries for lifecycle actions
    - Actor, action type, timestamp, consignment UUID, affected items.
    - _Requirements: 15.1_

  - [ ] 16.2 Add structured logs and metrics
    - Counters for opened/finalized consignments, returns, settlements created, settlements paid/received.
    - _Requirements: 15.2, 15.4_

---

## Wave 9: Admin Frontend

- [ ] 17. Add Consignacoes admin module
  - [ ] 17.1 Add route and menu item
    - Visible to ROLE_MANAGER, ROLE_STOCK, ROLE_FINANCE.
    - Add route-role tests.
    - _Requirements: 12.1, 13.2-13.4, 16.5_

  - [ ] 17.2 Create frontend models and `ConsignmentPort`
    - Commands, summaries, details, settlement models, paginated results.
    - _Requirements: 12.5, 12.6_

  - [ ] 17.3 Implement `ConsignmentHttpAdapter`
    - Map all REST endpoints and error responses.
    - Add HTTP adapter tests.
    - _Requirements: 12.6, 16.5_

  - [ ] 17.4 Implement `ConsignmentService`
    - Signals for list, detail, loading, saving, errors, filters.
    - _Requirements: 12.5_

- [ ] 18. Build Consignment UI screens
  - [ ] 18.1 Build list/search page
    - Filters by type, status, party, date range, due date, variant.
    - _Requirements: 11.1, 11.5, 12.5_

  - [ ] 18.2 Build create received consignment form
    - Party, due date, notes, variant search, item list.
    - _Requirements: 1.1, 1.2, 12.5_

  - [ ] 18.3 Build create sent consignment form
    - Validate available owned stock through backend responses.
    - _Requirements: 2.1-2.3, 12.5_

  - [ ] 18.4 Build detail page and item table
    - Show original, sold, purchased, returned, pending quantities and lifecycle status.
    - _Requirements: 3.2, 11.2_

  - [ ] 18.5 Build return/purchase/sale-confirmation dialogs
    - Role-aware actions and backend validation display.
    - _Requirements: 4.1, 5.1, 7.1, 8.1, 12.2-12.4_

  - [ ] 18.6 Build settlement panel and reports
    - Pending settlements by party, due/overdue consignments.
    - _Requirements: 10.3, 10.4, 11.3, 11.4_

---

## Wave 10: Verification and Documentation

- [ ] 19. Complete backend test suite
  - [ ] 19.1 Run domain and property tests for quantity invariants
    - _Requirements: 16.1_

  - [ ] 19.2 Run persistence/integration tests with Testcontainers
    - _Requirements: 16.2_

  - [ ] 19.3 Run Sales and Finance integration tests
    - _Requirements: 16.3, 16.4_

- [ ] 20. Complete frontend tests
  - [ ] 20.1 Run Consignment frontend unit/property tests
    - Role visibility, form validation, error mapping.
    - _Requirements: 16.5_

  - [ ] 20.2 Add E2E smoke coverage
    - Create received consignment, verify stock impact, return item, verify pending quantity.
    - _Requirements: 16.6_

- [ ] 21. Documentation
  - [ ] 21.1 Update API docs/specs
    - Document endpoints, DTOs, roles, and error cases.
    - _Requirements: 11.1, 13.1_

  - [ ] 21.2 Update admin documentation
    - Explain received vs sent consignment, statuses, role permissions, and limitations.
    - _Requirements: 12.1-12.5_

  - [ ] 21.3 Document fiscal/accounting exclusions
    - Make clear that fiscal invoice/legal accounting behavior is out of scope until validated.
    - _Requirements: 10.6_

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 1, "tasks": ["1.1", "1.2", "1.3", "1.4", "2.1", "2.2"] },
    { "id": 2, "tasks": ["3.1", "3.2", "3.3", "3.4", "4.1", "4.2", "4.3"] },
    { "id": 3, "tasks": ["5.1", "5.2", "5.3", "5.4", "6.1", "6.2", "6.3"] },
    { "id": 4, "tasks": ["7.1", "7.2", "7.3", "7.4"] },
    { "id": 5, "tasks": ["8.1", "8.2", "9.1", "9.2", "9.3", "9.4", "10.1", "10.2", "10.3", "10.4"] },
    { "id": 6, "tasks": ["11.1", "11.2", "11.3", "12.1", "12.2", "12.3"] },
    { "id": 7, "tasks": ["13.1", "13.2", "13.3", "14.1", "14.2"] },
    { "id": 8, "tasks": ["15.1", "15.2", "15.3", "16.1", "16.2"] },
    { "id": 9, "tasks": ["17.1", "17.2", "17.3", "17.4", "18.1", "18.2", "18.3", "18.4", "18.5", "18.6"] },
    { "id": 10, "tasks": ["19.1", "19.2", "19.3", "20.1", "20.2", "21.1", "21.2", "21.3"] }
  ]
}
```

## Notes

- Do not implement fiscal invoice or statutory accounting behavior from this spec.
- Keep Consignment as its own module; Inventory records stock effects but does not own the consignment lifecycle.
- Resolve Wave 1 decisions before coding stock and finance effects.
