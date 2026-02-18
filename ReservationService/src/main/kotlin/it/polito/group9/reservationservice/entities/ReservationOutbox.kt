package it.polito.group9.reservationservice.entities

import jakarta.persistence.*
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "reservation_outbox")
data class ReservationOutbox(
    @Id val id: UUID = UUID.randomUUID(),
    @Column(name = "aggregate_type", nullable = false, length = 50) val aggregateType: String,
    @Column(name = "aggregate_id", nullable = false) val aggregateId: Long,
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    val eventType: ReservationStatus,
    @Column(nullable = false, columnDefinition = "TEXT") val payload: String,
    @Column(name = "occurred_at", nullable = false)
    val occurredAt: OffsetDateTime = OffsetDateTime.now(),
)
