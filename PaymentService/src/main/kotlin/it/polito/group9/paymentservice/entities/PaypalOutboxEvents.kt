package it.polito.group9.paymentservice.model

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "paypal_outbox_events")
data class PaypalOutboxEvent(
    @Id val id: UUID = UUID.randomUUID(),
    @Column(name = "aggregate_type", nullable = false, length = 50) val aggregateType: String,
    @Column(name = "aggregate_id", nullable = false) val aggregateId: UUID,
    @Column(name = "event_type", nullable = false, length = 50) val eventType: String,
    @Column(nullable = false, columnDefinition = "TEXT") val payload: String,
    @Column(name = "occurred_at", nullable = false)
    val occurredAt: OffsetDateTime = OffsetDateTime.now()
)
