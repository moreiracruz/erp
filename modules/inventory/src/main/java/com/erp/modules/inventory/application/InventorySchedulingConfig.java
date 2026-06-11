package com.erp.modules.inventory.application;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables Spring's scheduled task execution for the inventory module.
 *
 * <p>Kept separate from the scheduler bean itself so that {@code @EnableScheduling}
 * sits on a {@code @Configuration} class, as recommended by Spring.
 */
@Configuration
@EnableScheduling
public class InventorySchedulingConfig {
}
