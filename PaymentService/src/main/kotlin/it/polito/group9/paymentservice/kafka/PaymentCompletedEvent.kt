package it.polito.group9.paymentservice.kafka

import java.util.UUID

/** Event published when a payment status changes. */
data class PaymentCompletedEvent(
    val reservationId: Long,
    val paypalOrderId: String,
    val paymentOrderId: UUID,
    val status: PaymentStatus
)
