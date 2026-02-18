package it.polito.group9.reservationservice.kafka

import it.polito.group9.reservationservice.entities.ReservationStatus

/** Event representing a newly created reservation, published to Kafka Outbox. */
data class ReservationCreatedEvent(
    val eventType: ReservationStatus,
    val reservationId: Long,
    val userId: Long,
    val amount: Long?
)
