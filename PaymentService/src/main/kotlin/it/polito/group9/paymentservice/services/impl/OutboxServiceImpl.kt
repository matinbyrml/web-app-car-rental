package it.polito.group9.paymentservice.services.impl

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.group9.paymentservice.kafka.PaymentCompletedEvent
import it.polito.group9.paymentservice.kafka.PaymentStatus
import it.polito.group9.paymentservice.model.PaymentOrder
import it.polito.group9.paymentservice.model.PaypalOutboxEvent
import it.polito.group9.paymentservice.repositories.PaypalOutBoxEventsRepository
import it.polito.group9.paymentservice.services.OutboxService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/** Implementation of OutboxService for publishing payment events. */
@Service
class OutboxServiceImpl(
    private val outboxRepo: PaypalOutBoxEventsRepository,
    private val objectMapper: ObjectMapper
) : OutboxService {

  private val logger = LoggerFactory.getLogger(OutboxServiceImpl::class.java)

  override fun publishPaymentEvent(order: PaymentOrder, status: PaymentStatus) {
    logger.info("Publishing payment event to outbox: orderId={}, status={}", order.id, status)

    try {
      // Crea sempre un evento tipizzato PaymentCompletedEvent
      val event =
          PaymentCompletedEvent(
              reservationId = order.reservationId,
              paypalOrderId = order.paypalOrderId ?: "",
              paymentOrderId = order.id,
              status = status)

      val payload = objectMapper.writeValueAsString(event)

      outboxRepo.save(
          PaypalOutboxEvent(
              aggregateType = "PaymentOrder",
              aggregateId = order.id,
              eventType = status.name,
              payload = payload))

      logger.info("Payment event published successfully: orderId={}, status={}", order.id, status)
    } catch (e: Exception) {
      logger.error("Failed to publish payment event: orderId={}, status={}", order.id, status, e)
      throw e
    }
  }
}
