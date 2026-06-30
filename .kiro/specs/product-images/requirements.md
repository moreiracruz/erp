# Requirements Document

## Introduction

This document defines the requirements for the Product Image Management feature in the ERP system. The feature enables managers to upload, organize, and manage product images through the admin panel, while the storefront displays those images to customers in the catalog and product detail pages. Images are stored locally on the server filesystem with automatic multi-size generation (thumbnail, card, full) and served via a static endpoint. The implementation spans the backend (Spring Boot, hexagonal architecture), admin frontend (Angular), and storefront frontend (Angular).

## Glossary

- **Image_Service**: The backend application service responsible for orchestrating image upload, processing, storage, retrieval, deletion, and reorder operations
- **Image_Processor**: The backend component responsible for resizing uploaded images into multiple size variants while maintaining aspect ratio
- **Image_Storage**: The local filesystem directory structure at `/uploads/products/{produto_uuid}/` where processed images are persisted
- **Image_API**: The set of REST endpoints under `/api/v1/products/{uuid}/images` for managing product images
- **Upload_Zone**: The frontend admin UI component providing drag-and-drop and click-to-select file upload functionality
- **Image_Gallery**: The storefront PDP component displaying product images as a navigable carousel with thumbnail strip
- **Product_Card**: The storefront catalog component displaying a product summary with its main image
- **Main_Image**: The single image per product designated as the primary/cover image displayed in listings and cards
- **Image_Record**: A database row in the `produto_imagens` table representing metadata for one uploaded image
- **Magic_Bytes**: The first bytes of a file used to verify the actual file type regardless of extension or Content-Type header
- **Sort_Order**: The integer field determining the display sequence of images for a product

## Requirements

### Requirement 1: Image Upload

**User Story:** As a manager, I want to upload images for a product, so that customers can see visual representations of products in the store.

#### Acceptance Criteria

1. WHEN a manager sends a multipart/form-data POST request to `/api/v1/products/{uuid}/images` with one or more image files, THE Image_API SHALL accept the upload and return the created Image_Record metadata with HTTP 201
2. THE Image_API SHALL accept files with content types image/jpeg, image/png, and image/webp exclusively
3. THE Image_API SHALL reject any single file larger than 5MB with an HTTP 400 response containing a descriptive error message
4. THE Image_API SHALL reject an upload that would cause the product to exceed 10 total images with an HTTP 400 response containing a descriptive error message
5. THE Image_API SHALL reject an upload that would cause the total storage for a product to exceed 50MB with an HTTP 400 response containing a descriptive error message
6. THE Image_API SHALL validate the file type by inspecting Magic_Bytes in addition to the Content-Type header
7. IF the Magic_Bytes do not match an accepted image format, THEN THE Image_API SHALL reject the file with HTTP 400 regardless of the declared Content-Type
8. THE Image_API SHALL sanitize uploaded filenames by removing path separators, special characters, and null bytes to prevent path traversal attacks
9. WHEN the product has no existing images, THE Image_Service SHALL mark the first uploaded image as the Main_Image automatically
10. THE Image_API SHALL assign Sort_Order values to newly uploaded images sequentially after the current maximum Sort_Order for that product

### Requirement 2: Image Processing and Storage

**User Story:** As a manager, I want uploaded images to be automatically resized into multiple dimensions, so that the storefront can serve appropriately sized images for different contexts.

#### Acceptance Criteria

1. WHEN an image is successfully uploaded, THE Image_Processor SHALL generate three size variants: thumbnail (200px wide), card (400px wide), and full (800px wide)
2. THE Image_Processor SHALL maintain the original aspect ratio when resizing images
3. THE Image_Processor SHALL output JPEG variants at 85% quality for images originally in JPEG format
4. THE Image_Processor SHALL preserve the original format (PNG, WebP) for non-JPEG uploads during resizing
5. THE Image_Storage SHALL persist all size variants in the directory `/uploads/products/{produto_uuid}/` using a naming convention that includes the size suffix (e.g., `{name}_thumb.jpg`, `{name}_card.jpg`, `{name}_full.jpg`)
6. THE Image_Service SHALL store the filename, original_name, content_type, file_size, sort_order, and is_main fields in the Image_Record

### Requirement 3: Image Listing

**User Story:** As a frontend client, I want to retrieve all images for a product in display order, so that I can render them correctly on the page.

#### Acceptance Criteria

1. WHEN a GET request is sent to `/api/v1/products/{uuid}/images`, THE Image_API SHALL return a JSON array of Image_Records sorted by Sort_Order ascending
2. THE Image_API response SHALL include for each image: id, filename, original_name, content_type, file_size, sort_order, is_main, created_at, and URLs for each size variant (thumbnail, card, full)
3. IF the product UUID does not exist, THEN THE Image_API SHALL return HTTP 404 with a descriptive error message

### Requirement 4: Image Deletion

**User Story:** As a manager, I want to delete individual product images, so that I can remove outdated or incorrect images.

#### Acceptance Criteria

1. WHEN a manager sends a DELETE request to `/api/v1/products/{uuid}/images/{imageId}`, THE Image_API SHALL remove the Image_Record from the database and delete all associated files from Image_Storage
2. IF the deleted image was the Main_Image and other images remain, THEN THE Image_Service SHALL promote the image with the lowest Sort_Order as the new Main_Image
3. IF the image ID does not exist for the given product, THEN THE Image_API SHALL return HTTP 404
4. THE Image_API SHALL return HTTP 204 on successful deletion

### Requirement 5: Image Reordering

**User Story:** As a manager, I want to reorder product images, so that I can control which images appear first in the gallery.

#### Acceptance Criteria

1. WHEN a manager sends a PUT request to `/api/v1/products/{uuid}/images/reorder` with a JSON body containing an ordered array of image IDs, THE Image_API SHALL update Sort_Order values to match the provided sequence
2. IF the provided array does not contain exactly all image IDs belonging to that product, THEN THE Image_API SHALL return HTTP 400 with a descriptive error message
3. THE Image_API SHALL return the updated list of Image_Records sorted by the new Sort_Order on success

### Requirement 6: Set Main Image

**User Story:** As a manager, I want to designate one image as the main/cover image, so that it appears as the product thumbnail in catalog listings.

#### Acceptance Criteria

1. WHEN a manager sends a PUT request to `/api/v1/products/{uuid}/images/{imageId}/main`, THE Image_Service SHALL set the specified image as is_main=true and set all other images for that product as is_main=false
2. IF the image ID does not exist for the given product, THEN THE Image_API SHALL return HTTP 404
3. THE Image_API SHALL return the updated Image_Record on success

### Requirement 7: Image Serving

**User Story:** As a customer, I want product images to load efficiently from the server, so that I can view products without slow page load times.

#### Acceptance Criteria

1. THE application SHALL serve static files from the `/uploads/products/` directory path via HTTP GET requests
2. THE application SHALL set appropriate Cache-Control headers on image responses to enable browser caching
3. THE application SHALL set the correct Content-Type header matching the served image format

### Requirement 8: Authorization and Security

**User Story:** As a system administrator, I want image management endpoints to be restricted to authorized managers, so that unauthorized users cannot modify product images.

#### Acceptance Criteria

1. THE Image_API upload, delete, reorder, and set-main endpoints SHALL require the authenticated user to have ROLE_MANAGER
2. IF a request to a protected Image_API endpoint lacks valid authentication or the ROLE_MANAGER role, THEN THE Image_API SHALL return HTTP 403
3. THE Image_API SHALL reject filenames containing path traversal sequences (e.g., `../`, `..\\`, null bytes) before any file operation
4. THE Image_API listing and image serving endpoints SHALL be accessible without authentication for storefront consumption

### Requirement 9: Admin Frontend Image Upload UI

**User Story:** As a manager, I want a visual interface for uploading and managing product images in the admin panel, so that I can manage images without using API tools directly.

#### Acceptance Criteria

1. THE admin product form SHALL display an "Imagens" section below the product details fields
2. THE Upload_Zone SHALL accept files via drag-and-drop and via click-to-select file dialog
3. THE Upload_Zone SHALL validate file type (JPEG, PNG, WebP) and file size (maximum 5MB) on the client before initiating upload
4. IF a file fails client-side validation, THEN THE Upload_Zone SHALL display an inline error message describing the violation without sending the request
5. WHILE an upload is in progress, THE Upload_Zone SHALL display a progress indicator showing upload percentage for each file
6. WHEN an upload completes successfully, THE Upload_Zone SHALL display a thumbnail preview of the uploaded image
7. THE admin image section SHALL allow a manager to reorder images by dragging thumbnails to new positions
8. THE admin image section SHALL allow a manager to mark an image as Main_Image by clicking a star/crown icon on the thumbnail
9. THE admin image section SHALL allow a manager to delete an image by clicking a delete button on the thumbnail, with a confirmation prompt before deletion
10. IF an upload or management operation fails, THEN THE admin image section SHALL display a toast notification with the error message

### Requirement 10: Storefront Product Card Image Display

**User Story:** As a customer, I want to see product images in catalog cards, so that I can visually identify products while browsing.

#### Acceptance Criteria

1. THE Product_Card SHALL display the Main_Image of the product using the card-size variant (400px wide)
2. IF a product has no images, THEN THE Product_Card SHALL display the branded placeholder image from `assets/images/product-placeholder.webp`
3. THE Product_Card SHALL use native lazy loading (loading="lazy") for images below the initial viewport
4. WHILE the product image is loading, THE Product_Card SHALL display a shimmer placeholder matching the image dimensions

### Requirement 11: Storefront PDP Image Gallery

**User Story:** As a customer, I want to browse all product images in a gallery on the product detail page, so that I can examine the product from different angles before purchasing.

#### Acceptance Criteria

1. WHEN a customer navigates to the Product_Detail_Page, THE Image_Gallery SHALL display the Main_Image as the large primary image using the full-size variant (800px wide)
2. THE Image_Gallery SHALL display a horizontal thumbnail strip below the primary image showing all product images using thumbnail variants (200px wide)
3. WHEN a customer clicks a thumbnail, THE Image_Gallery SHALL update the primary image to the selected image within 200ms
4. WHILE viewing on a viewport below 768px, THE Image_Gallery SHALL support swipe gestures to navigate between images
5. IF a product has no images, THEN THE Image_Gallery SHALL display the branded placeholder image
6. THE Image_Gallery SHALL preload the adjacent images (previous and next) relative to the currently displayed image

### Requirement 12: Image Persistence Schema

**User Story:** As a developer, I want a well-structured database schema for image metadata, so that image data is queryable and maintains referential integrity with products.

#### Acceptance Criteria

1. THE `produto_imagens` table SHALL reference `produtos.uuid` with a foreign key constraint and ON DELETE CASCADE behavior
2. THE `produto_imagens` table SHALL have an index on `produto_uuid` for efficient lookups by product
3. THE `produto_imagens` table SHALL have a partial index on `(produto_uuid, is_main)` filtering for `is_main = TRUE` to enforce efficient main image lookup
4. THE Image_Service SHALL ensure at most one image per product has `is_main = TRUE` at any time
