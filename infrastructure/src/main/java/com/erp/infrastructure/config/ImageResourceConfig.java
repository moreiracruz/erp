package com.erp.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

/**
 * Configures static resource serving for product images.
 *
 * <p>Maps the URL path {@code /uploads/products/**} to the filesystem directory
 * specified by {@code app.images.base-path}. Responses include a Cache-Control
 * header set to 7 days public caching for optimal browser performance.
 */
@Configuration
public class ImageResourceConfig implements WebMvcConfigurer {

    @Value("${app.images.base-path:./uploads/products}")
    private String imagesBasePath;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/products/**")
                .addResourceLocations("file:" + imagesBasePath + "/")
                .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic());
    }
}
