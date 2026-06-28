package br.com.moreiracruz.erp.modules.product.adapter.in.web;

import br.com.moreiracruz.erp.modules.product.domain.port.in.DeleteImageUseCase;
import br.com.moreiracruz.erp.modules.product.domain.port.in.ImageResponse;
import br.com.moreiracruz.erp.modules.product.domain.port.in.ListImagesUseCase;
import br.com.moreiracruz.erp.modules.product.domain.port.in.ReorderImagesUseCase;
import br.com.moreiracruz.erp.modules.product.domain.port.in.SetMainImageUseCase;
import br.com.moreiracruz.erp.modules.product.domain.port.in.UploadImageCommand;
import br.com.moreiracruz.erp.modules.product.domain.port.in.UploadImageUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * REST adapter exposing product image management operations under
 * {@code /api/v1/products/{uuid}/images}.
 *
 * <p>Mutating endpoints (upload, delete, reorder, set-main) require ROLE_MANAGER.
 * The listing endpoint is publicly accessible for storefront consumption.
 */
@RestController
@RequestMapping("/api/v1/products/{uuid}/images")
public class ProductImageController {

    private final UploadImageUseCase uploadImageUseCase;
    private final ListImagesUseCase listImagesUseCase;
    private final DeleteImageUseCase deleteImageUseCase;
    private final ReorderImagesUseCase reorderImagesUseCase;
    private final SetMainImageUseCase setMainImageUseCase;

    public ProductImageController(UploadImageUseCase uploadImageUseCase,
                                  ListImagesUseCase listImagesUseCase,
                                  DeleteImageUseCase deleteImageUseCase,
                                  ReorderImagesUseCase reorderImagesUseCase,
                                  SetMainImageUseCase setMainImageUseCase) {
        this.uploadImageUseCase = uploadImageUseCase;
        this.listImagesUseCase = listImagesUseCase;
        this.deleteImageUseCase = deleteImageUseCase;
        this.reorderImagesUseCase = reorderImagesUseCase;
        this.setMainImageUseCase = setMainImageUseCase;
    }

    /**
     * Uploads one or more images for a product.
     *
     * @param uuid  the product's UUID
     * @param files one or more image files (multipart/form-data)
     * @return the created image metadata
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public List<ImageResponse> upload(@PathVariable UUID uuid,
                                      @RequestParam("files") List<MultipartFile> files) {
        List<UploadImageCommand> commands = files.stream()
                .map(this::toCommand)
                .toList();
        return uploadImageUseCase.upload(uuid, commands);
    }

    /**
     * Lists all images for a product sorted by display order.
     * This endpoint is publicly accessible for storefront consumption.
     *
     * @param uuid the product's UUID
     * @return images sorted by sort_order ascending
     */
    @GetMapping
    public List<ImageResponse> list(@PathVariable UUID uuid) {
        return listImagesUseCase.listByProduct(uuid);
    }

    /**
     * Deletes a single image from a product.
     *
     * @param uuid    the product's UUID
     * @param imageId the image ID to delete
     */
    @DeleteMapping("/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public void delete(@PathVariable UUID uuid, @PathVariable Long imageId) {
        deleteImageUseCase.delete(uuid, imageId);
    }

    /**
     * Reorders images for a product based on the provided ID sequence.
     *
     * @param uuid    the product's UUID
     * @param request body containing the ordered list of all image IDs
     * @return the updated list of images sorted by the new order
     */
    @PutMapping("/reorder")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public List<ImageResponse> reorder(@PathVariable UUID uuid,
                                       @RequestBody ReorderRequest request) {
        return reorderImagesUseCase.reorder(uuid, request.imageIds());
    }

    /**
     * Designates an image as the main/cover image for the product.
     *
     * @param uuid    the product's UUID
     * @param imageId the image ID to set as main
     * @return the updated image metadata
     */
    @PutMapping("/{imageId}/main")
    @PreAuthorize("hasRole('ROLE_MANAGER')")
    public ImageResponse setMain(@PathVariable UUID uuid, @PathVariable Long imageId) {
        return setMainImageUseCase.setMain(uuid, imageId);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private UploadImageCommand toCommand(MultipartFile file) {
        try {
            return new UploadImageCommand(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    file.getBytes()
            );
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler arquivo de upload", e);
        }
    }
}
