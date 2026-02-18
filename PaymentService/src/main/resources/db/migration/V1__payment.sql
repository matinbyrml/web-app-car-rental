DROP TABLE IF EXISTS payment_order;
DROP TABLE IF EXISTS paypal_outbox_events;

CREATE TABLE payment_order
(
    id              UUID           NOT NULL,
    reservation_id  INTEGER        NOT NULL,
    amount          DECIMAL(12, 2) NOT NULL,
    currency        VARCHAR(3)     NOT NULL,
    status          VARCHAR(20)    NOT NULL,
    paypal_order_id VARCHAR(100),
    tms_created     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    tms_updated     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    approval_url VARCHAR(500),
    CONSTRAINT pk_payment_order PRIMARY KEY (id)
);

CREATE TABLE paypal_outbox_events
(
    id             UUID        NOT NULL,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id   UUID        NOT NULL,
    event_type     VARCHAR(50) NOT NULL,
    payload        TEXT        NOT NULL,
    occurred_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_paypal_outbox_events PRIMARY KEY (id)
);