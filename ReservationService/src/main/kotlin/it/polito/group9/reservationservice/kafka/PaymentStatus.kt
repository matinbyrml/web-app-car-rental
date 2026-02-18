package it.polito.group9.reservationservice.kafka

enum class PaymentStatus {
  CREATED,
  COMPLETED,
  CANCELLED,
  REFUNDED,
  PARTIALLY_REFUNDED,
  REFUND_PENDING,
  FAILED,
  DECLINED
}
