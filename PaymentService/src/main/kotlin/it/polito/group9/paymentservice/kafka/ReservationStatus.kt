package it.polito.group9.paymentservice.kafka

/** Enum representing the states of a reservation event */
enum class ReservationStatus {
  PENDING,
  PAID,
  PICKED_UP,
  RETURNED,
  CANCELLED,
  CANCELLED_PENDING_REFUND,
}
