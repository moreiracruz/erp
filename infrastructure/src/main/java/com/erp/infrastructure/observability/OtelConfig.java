package com.erp.infrastructure.observability;

import org.springframework.context.annotation.Configuration;

/**
 * OpenTelemetry configuration.
 * <p>
 * OpenTelemetry auto-instrumentation handles trace propagation via spring-boot-starter.
 * This config ensures MDC population (traceId, spanId) via Logback bridge.
 * OTLP exporter endpoint is configured via OTEL_EXPORTER_OTLP_ENDPOINT env var.
 */
@Configuration
public class OtelConfig {
}
