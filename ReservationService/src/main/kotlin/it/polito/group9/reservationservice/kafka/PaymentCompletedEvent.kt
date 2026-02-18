package it.polito.group9.reservationservice.kafka

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

data class PaymentCompletedEvent(
    @JsonProperty("reservationId") val reservationId: Long,
    @JsonProperty("paypalOrderId") val paypalOrderId: String,
    @JsonProperty("paymentOrderId") val paymentOrderId: UUID,
    @JsonProperty("status") val status: PaymentStatus
) {
  constructor() : this(0, "", UUID.randomUUID(), PaymentStatus.COMPLETED)
}
