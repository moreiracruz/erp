package com.erp.modules.product.adapter.in.web;

import java.util.List;

/**
 * Request body for the image reorder endpoint.
 *
 * @param imageIds ordered list of all image IDs for the product
 */
public record ReorderRequest(List<Long> imageIds) {}
