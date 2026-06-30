package br.com.moreiracruz.erp.modules.product.domain.port.in;

/**
 * Command carrying the data for a single image upload.
 */
public record UploadImageCommand(String originalFilename, String contentType, long size, byte[] content) {}
