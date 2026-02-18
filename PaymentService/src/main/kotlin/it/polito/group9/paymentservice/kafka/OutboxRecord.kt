package it.polito.group9.paymentservice.kafka

import java.time.OffsetDateTime

/** Generic wrapper for outbox records emitted via Debezium. */
data class OutboxRecord<T>(
    val aggregate_type: String,
    val aggregate_id: Any,
    val event_type: ReservationStatus,
    val payload: T,
    val occurred_at: OffsetDateTime
)
