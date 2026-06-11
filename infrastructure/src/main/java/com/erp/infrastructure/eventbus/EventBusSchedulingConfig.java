package com.erp.infrastructure.eventbus;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables scheduling for the event retry mechanism.
 */
@Configuration
@EnableScheduling
public class EventBusSchedulingConfig {
}
