-- Create reservation outbox table for Debezium
DROP TABLE IF EXISTS reservation_outbox CASCADE;

CREATE TABLE IF NOT EXISTS reservation_outbox
(
    id             UUID        NOT NULL,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id   BIGINT      NOT NULL,
    event_type     VARCHAR(50) NOT NULL,
    payload        TEXT        NOT NULL,
    occurred_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_reservation_outbox PRIMARY KEY (id)
);

