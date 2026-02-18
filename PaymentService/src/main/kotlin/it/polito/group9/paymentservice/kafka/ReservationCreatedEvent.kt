package it.polito.group9.paymentservice.kafka

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

/** Event published when a new reservation is created. */
data class ReservationCreatedEvent
@JsonCreator
constructor(
    @JsonProperty("eventType") val eventType: ReservationStatus,
    @JsonProperty("reservationId") val reservationId: Long,
    @JsonProperty("userId") val userId: Long,
    @JsonProperty("amount") val amount: Double?
) {
  constructor() : this(ReservationStatus.PENDING, 0, 0, null)
}
