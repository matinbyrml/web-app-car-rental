package it.polito.group9.paymentservice.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.group9.paymentservice.services.PaymentService
import java.math.BigDecimal
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class ReservationEventConsumer(
    private val paymentService: PaymentService,
) {
  private val logger = LoggerFactory.getLogger(javaClass)
  private val objectMapper = ObjectMapper()

  @KafkaListener(
      topics = ["reservation.public.reservation_outbox"],
      groupId = "payment-service",
      containerFactory = "reservationListenerContainerFactory")
  fun listen(record: OutboxRecord<String>, ack: Acknowledgment) {
    try {
      val event = objectMapper.readValue(record.payload, ReservationCreatedEvent::class.java)
      logger.info(
          "Received reservation event: type={}, id={}, user={}, amount={}",
          event.eventType,
          event.reservationId,
          event.userId,
          event.amount)

      when (event.eventType) {
        ReservationStatus.PENDING -> {
          val amount = event.amount
          if (amount == null) {
            logger.warn(
                "Skipping order creation for reservation {}: amount is null", event.reservationId)
          } else {
            logger.info("Creating payment order for reservation {}", event.reservationId)
            val paymentOrder =
                paymentService.createOrder(
                    clientId = event.userId,
                    reservationId = event.reservationId,
                    amount = BigDecimal.valueOf(amount))
            logger.info("Payment order created successfully: {}", paymentOrder.paymentOrderId)
          }
        }
        ReservationStatus.CANCELLED_PENDING_REFUND -> {
          try {
            logger.info("Auto-refund flow triggered for reservation {}", event.reservationId)
            val resp = paymentService.refundByReservation(event.reservationId, null)
            logger.info(
                "Auto-refund completed for reservation {}: refundId={}, status={} ",
                event.reservationId,
                resp.paypalRefundId,
                resp.status)
          } catch (e: Exception) {
            logger.error("Auto-refund failed for reservation {}", event.reservationId, e)
          }
        }
        else -> {
          // other reservation events are not handled here
        }
      }

      ack.acknowledge()
    } catch (e: Exception) {
      logger.error("Failed to process reservation event", e)
    }
  }
}
