package it.polito.group9.paymentservice.services

import it.polito.group9.paymentservice.kafka.PaymentStatus
import it.polito.group9.paymentservice.model.PaymentOrder

/** Service interface for publishing outbox events. */
interface OutboxService {
  /**
   * Publishes a payment event to the outbox.
   *
   * @param order The payment order associated with the event
   * @param status The payment status to publish
   */
  fun publishPaymentEvent(order: PaymentOrder, status: PaymentStatus)
}
