package it.polito.group9.reservationservice.kafka

import java.time.OffsetDateTime

/** Generic wrapper for outbox records emitted via Debezium. */
data class OutboxRecord<T>(
    val aggregate_type: String,
    val aggregate_id: Any,
    val event_type: PaymentStatus,
    val payload: T,
    val occurred_at: OffsetDateTime
)
