# Implementation Plan: Product Images

## Overview

This plan implements the Product Image Management feature following the existing hexagonal architecture in the `product` module. Tasks are ordered to build domain → ports → adapters → frontend, ensuring each step integrates with previous ones. The migration (V10) is already in place, so we start from domain model and ports.

## Tasks

- [x] 1. Domain model, ports, and configuration
  - [x] 1.1 Create `ProdutoImagem` domain model and `ImageValidator` utility
    - Create `ProdutoImagem.java` in `modules/product/src/main/java/br/com/moreiracruz/erp/modules/product/domain/model/` with `create()` and `restore()` factory methods
    - Create `ImageValidator.java` in same package with `validateMagicBytes(byte[])`, `validateFileSize(long)`, `validateFilename(String)` methods
    - Implement magic bytes detection for JPEG (0xFF 0xD8 0xFF), PNG (0x89 0x50 0x4E 0x47), WebP (RIFF...WEBP)
    - _Requirements: 1.2, 1.3, 1.6, 1.7, 1.8, 8.3_

  - [x] 1.2 Create inbound port interfaces and DTOs
    - Create `UploadImageUseCase`, `DeleteImageUseCase`, `ReorderImagesUseCase`, `SetMainImageUseCase`, `ListImagesUseCase` interfaces in `domain/port/in/`
    - Create `UploadImageCommand` record and `ImageResponse` record in `domain/port/in/`
    - _Requirements: 1.1, 3.2, 4.1, 5.1, 6.1_

  - [x] 1.3 Create outbound port interfaces
    - Create `ProdutoImagemRepository`, `ImageStoragePort`, `ImageProcessorPort` interfaces in `domain/port/out/`
    - _Requirements: 2.1, 2.5, 2.6_

  - [x] 1.4 Add Thumbnailator dependency and image configuration properties
    - Add `net.coobird:thumbnailator` dependency to `modules/product/pom.xml`
    - Add `app.images.*` configuration block to `bootstrap/src/main/resources/application.yml`
    - Create `ImageProperties.java` configuration class bound to `app.images` prefix
    - _Requirements: 2.1, 2.2, 2.3, 2.4_

- [x] 2. Backend use case implementations
  - [x] 2.1 Implement `UploadImageUseCaseImpl`
    - Create use case in `application/usecase/` following existing patterns (e.g., `RegisterProductUseCaseImpl`)
    - Validate file type (content-type + magic bytes), file size, image count limit, storage limit
    - Generate sanitized UUID-based filename, delegate to `ImageProcessorPort` for resize, store via `ImageStoragePort`
    - Save `ProdutoImagem` to repository with correct sort_order and auto-set main if first image
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 1.10, 2.6_

  - [x] 2.2 Write property tests for upload validation (ImageValidatorProperties)
    - **Property 1: File type validation rejects non-image content**
    - **Property 2: File size validation enforces 5MB limit**
    - **Property 3: Image count limit enforces maximum of 10**
    - **Property 4: Storage limit enforces 50MB total per product**
    - **Property 5: Filename sanitization removes all dangerous sequences**
    - **Validates: Requirements 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 8.3**

  - [x] 2.3 Implement `DeleteImageUseCaseImpl`
    - Delete all file variants from storage, remove DB record
    - If deleted image was main and others remain, promote lowest sort_order image to main
    - _Requirements: 4.1, 4.2, 4.3, 4.4_

  - [x] 2.4 Implement `ReorderImagesUseCaseImpl`
    - Validate provided IDs are exact permutation of product's image IDs
    - Update sort_order based on array position (index 0 → sortOrder 0, etc.)
    - _Requirements: 5.1, 5.2, 5.3_

  - [x] 2.5 Implement `SetMainImageUseCaseImpl`
    - Clear is_main on all product images, set is_main on target image
    - _Requirements: 6.1, 6.2, 6.3, 12.4_

  - [x] 2.6 Implement `ListImagesUseCaseImpl`
    - Query images by product UUID ordered by sort_order
    - Map to `ImageResponse` with resolved URLs for each size variant
    - _Requirements: 3.1, 3.2, 3.3_

  - [x] 2.7 Write property tests for service logic (ImageServiceProperties)
    - **Property 6: Sort order assignment is sequential after current maximum**
    - **Property 10: Image listing is always sorted by sort_order ascending**
    - **Property 11: Main image promotion selects lowest sort_order**
    - **Property 12: Reorder maps array positions to sort_order values**
    - **Property 13: Reorder rejects incomplete or excessive ID lists**
    - **Property 14: At most one main image per product (uniqueness invariant)**
    - **Validates: Requirements 1.10, 3.1, 4.2, 5.1, 5.2, 6.1, 12.4**

- [x] 3. Infrastructure adapters (persistence and storage)
  - [x] 3.1 Implement `ProdutoImagemJpaEntity` and `ProdutoImagemJpaRepository`
    - Create JPA entity in `infrastructure/src/main/java/br/com/moreiracruz/erp/infrastructure/persistence/product/`
    - Create Spring Data JPA repository interface with required query methods
    - _Requirements: 12.1, 12.2, 12.3_

  - [x] 3.2 Implement `ProdutoImagemJpaRepositoryAdapter`
    - Create adapter implementing `ProdutoImagemRepository` port
    - Map between domain model and JPA entity
    - Implement `clearMainByProdutoUuid`, `sumFileSizeByProdutoUuid`, `findMaxSortOrderByProdutoUuid`
    - _Requirements: 2.6, 12.1, 12.2, 12.3, 12.4_

  - [x] 3.3 Implement `ThumbnailatorImageProcessorAdapter`
    - Create adapter implementing `ImageProcessorPort`
    - Resize to 200px, 400px, 800px widths maintaining aspect ratio
    - JPEG at 85% quality, PNG/WebP preserved as-is
    - _Requirements: 2.1, 2.2, 2.3, 2.4_

  - [x] 3.4 Write property tests for image processor (ImageProcessorProperties)
    - **Property 7: Image resize preserves aspect ratio and produces correct widths**
    - **Property 8: Non-JPEG format is preserved through resize**
    - **Validates: Requirements 2.1, 2.2, 2.3, 2.4**

  - [x] 3.5 Implement `LocalImageStorageAdapter`
    - Create adapter implementing `ImageStoragePort`
    - Store files at `{base-path}/{produto_uuid}/{filename}_{size}.{ext}`
    - Create directories lazily, implement delete/deleteAll with file cleanup
    - Resolve URL paths for serving
    - _Requirements: 2.5, 7.1_

  - [x] 3.6 Write property test for storage path construction (ImageStoragePathProperties)
    - **Property 9: Storage path construction follows naming convention**
    - **Validates: Requirements 2.5**

- [x] 4. Checkpoint - Ensure all backend tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 5. Web adapter and security configuration
  - [x] 5.1 Implement `ProductImageController`
    - Create REST controller at `/api/v1/products/{uuid}/images` in `adapter/in/web/`
    - Implement POST (upload), GET (list), DELETE, PUT /reorder, PUT /{imageId}/main
    - Add `@PreAuthorize("hasRole('ROLE_MANAGER')")` on mutating endpoints
    - _Requirements: 1.1, 3.1, 4.1, 4.4, 5.1, 5.3, 6.1, 6.3, 8.1, 8.2_

  - [x] 5.2 Configure static resource serving and security rules
    - Add static resource handler for `/uploads/products/**` in a WebMvcConfigurer
    - Add Cache-Control headers for image responses
    - Update `SecurityConfig` to permit GET requests to `/uploads/products/**` without authentication
    - _Requirements: 7.1, 7.2, 7.3, 8.4_

  - [x] 5.3 Write unit tests for `ProductImageController`
    - Test request/response mapping, validation error responses
    - Test authorization enforcement (403 without ROLE_MANAGER)
    - _Requirements: 1.1, 8.1, 8.2_

- [x] 6. Frontend core layer (model, port, adapter)
  - [x] 6.1 Create `ProductImage` model and `ImagePort` abstract class
    - Create `product-image.model.ts` in `frontend/src/app/core/models/`
    - Create `image.port.ts` in `frontend/src/app/core/ports/`
    - _Requirements: 3.2, 9.1_

  - [x] 6.2 Implement `ImageHttpAdapter`
    - Create `image-http.adapter.ts` in `frontend/src/app/infrastructure/http/`
    - Implement all `ImagePort` methods using HttpClient with multipart upload support
    - Register adapter as provider for `ImagePort` in app config
    - _Requirements: 1.1, 4.1, 5.1, 6.1_

- [x] 7. Admin frontend - Image management UI
  - [x] 7.1 Create `UploadZoneComponent`
    - Standalone component with drag-and-drop zone and click-to-select
    - Client-side validation: file type (JPEG, PNG, WebP), size (≤5MB)
    - Show inline error messages for validation failures
    - Display progress indicator during upload
    - _Requirements: 9.2, 9.3, 9.4, 9.5_

  - [x] 7.2 Create `ImageGridComponent`
    - Standalone component displaying thumbnail grid of uploaded images
    - CDK DragDrop for reorder, star icon to set main, delete button with confirmation dialog
    - Emit events for reorder, setMain, delete actions
    - _Requirements: 9.6, 9.7, 9.8, 9.9_

  - [x] 7.3 Create `ProductImageSectionComponent` and integrate into admin product form
    - Container component orchestrating UploadZone and ImageGrid
    - Wire to `ImagePort` for all CRUD operations
    - Display toast notifications on errors
    - Add "Imagens" section to the existing admin products component
    - _Requirements: 9.1, 9.10_

  - [x] 7.4 Write unit tests for admin image components
    - Test UploadZoneComponent: file validation, drag events, progress display
    - Test ImageGridComponent: reorder drag-drop, main toggle, delete confirmation
    - Test ProductImageSectionComponent: orchestration and error handling
    - _Requirements: 9.2, 9.3, 9.4, 9.7, 9.8, 9.9_

- [x] 8. Storefront frontend - Image display integration
  - [x] 8.1 Update `ProductCardComponent` to display product images
    - Bind main image card-size URL from product data
    - Fallback to `assets/images/product-placeholder.webp` when no images
    - Add `loading="lazy"` attribute for images below fold
    - Add shimmer placeholder while loading
    - _Requirements: 10.1, 10.2, 10.3, 10.4_

  - [x] 8.2 Update `ImageGalleryComponent` to use ProductImage data
    - Display main image as large primary using full-size variant
    - Render horizontal thumbnail strip with all images
    - Implement thumbnail click to switch primary image (within 200ms)
    - Support swipe gestures below 768px viewport
    - Preload adjacent images
    - Fallback to placeholder when no images
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 11.6_

  - [x] 8.3 Write unit tests for storefront image components
    - Test ProductCardComponent: placeholder fallback, lazy loading attribute
    - Test ImageGalleryComponent: thumbnail click, swipe navigation, preload links
    - _Requirements: 10.1, 10.2, 10.3, 11.1, 11.3, 11.4, 11.6_

- [x] 9. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties from the design document
- The V10 migration already exists — no schema creation task needed
- The `ImageGalleryComponent` and `ProductCardComponent` (via product-grid) already exist — tasks 8.1 and 8.2 modify them rather than create from scratch
- Thumbnailator dependency needs to be added to `modules/product/pom.xml`
- Static file serving configuration belongs in the `infrastructure` module

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1", "1.2", "1.3", "1.4"] },
    { "id": 1, "tasks": ["2.1", "2.3", "2.4", "2.5", "2.6", "3.1", "3.3", "3.5"] },
    { "id": 2, "tasks": ["2.2", "2.7", "3.2", "3.4", "3.6"] },
    { "id": 3, "tasks": ["5.1", "5.2"] },
    { "id": 4, "tasks": ["5.3", "6.1"] },
    { "id": 5, "tasks": ["6.2"] },
    { "id": 6, "tasks": ["7.1", "7.2", "8.1", "8.2"] },
    { "id": 7, "tasks": ["7.3", "7.4", "8.3"] }
  ]
}
```
