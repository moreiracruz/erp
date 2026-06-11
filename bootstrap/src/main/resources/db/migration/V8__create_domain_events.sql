CREATE TABLE domain_events (
    id              BIGSERIAL       PRIMARY KEY,
    event_id        UUID            NOT NULL UNIQUE,
    event_type      VARCHAR(50)     NOT NULL,
    payload         JSONB           NOT NULL,
    status          VARCHAR(10)     NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING','DELIVERED','FAILED','DLQ')),
    retry_count     INT             NOT NULL DEFAULT 0,
    next_retry_at   TIMESTAMPTZ,
    last_error      TEXT,
    occurred_at     TIMESTAMPTZ     NOT NULL,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_domain_events_status ON domain_events(status, next_retry_at)
    WHERE status IN ('PENDING','FAILED');
