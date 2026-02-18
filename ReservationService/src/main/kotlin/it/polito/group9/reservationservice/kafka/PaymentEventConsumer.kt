package it.polito.group9.reservationservice.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.group9.reservationservice.dtos.CreateNotificationRequest
import it.polito.group9.reservationservice.entities.NotificationType
import it.polito.group9.reservationservice.entities.ReservationStatus
import it.polito.group9.reservationservice.services.NotificationService
import it.polito.group9.reservationservice.services.ReservationService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class PaymentEventConsumer(
    private val reservationService: ReservationService,
    private val notificationService: NotificationService
) {
  private val logger = LoggerFactory.getLogger(javaClass)
  private val objectMapper = ObjectMapper()

  @KafkaListener(
      topics = ["paypal.public.paypal_outbox_events"],
      groupId = "reservation-service",
      containerFactory = "paymentListenerContainerFactory")
  fun listen(record: OutboxRecord<String>, ack: Acknowledgment) {
    try {
      val event = objectMapper.readValue(record.payload, PaymentCompletedEvent::class.java)
      logger.info(
          "Received payment event: reservationId={}, status={}", event.reservationId, event.status)

      val reservation = reservationService.getReservationById(event.reservationId)
      val userId = reservation.userId

      // Mappa PaymentStatus a ReservationStatus e aggiorna la prenotazione
      when (event.status) {
        PaymentStatus.COMPLETED -> {
          reservationService.updateReservationByStatus(event.reservationId, ReservationStatus.PAID)
          notificationService.create(
              buildNotificationRequest(
                  NotificationType.PAYMENT,
                  "Payment Completed",
                  "Your payment for reservation ${reservation.id} has been successfully processed.",
                  userId))
          logger.info("Reservation ${event.reservationId} marked as PAID")
        }
        PaymentStatus.CANCELLED,
        PaymentStatus.FAILED,
        PaymentStatus.DECLINED -> {
          reservationService.updateReservationByStatus(
              event.reservationId, ReservationStatus.CANCELLED)
          notificationService.create(
              buildNotificationRequest(
                  NotificationType.RESERVATION,
                  "Reservation Cancelled",
                  "Your reservation ${reservation.id} has been successfully cancelled.",
                  userId))
          logger.info(
              "Reservation ${event.reservationId} marked as CANCELLED due to payment failure")
        }
        PaymentStatus.REFUNDED -> {
          // Rimborso completo: aggiorna da CANCELLED_PENDING_REFUND a CANCELLED
          reservationService.updateReservationByStatus(
              event.reservationId, ReservationStatus.CANCELLED)
          notificationService.create(
              buildNotificationRequest(
                  NotificationType.PAYMENT,
                  "Payment refunded",
                  "Your payment for reservation ${reservation.id} has been successfully refunded",
                  userId))
          logger.info(
              "Reservation ${event.reservationId} marked as CANCELLED after successful refund")
        }
        PaymentStatus.PARTIALLY_REFUNDED -> {
          // Per rimborsi parziali, manteniamo lo stato PAID ma loggiamo l'evento
          logger.info(
              "Reservation ${event.reservationId} partially refunded but keeping PAID status")
        }
        PaymentStatus.REFUND_PENDING -> {
          logger.info(
              "Refund pending for reservation ${event.reservationId}, keeping CANCELLED_PENDING_REFUND status")
        }
        PaymentStatus.CREATED -> {
          logger.info(
              "Payment order created for reservation ${event.reservationId}, no status change needed")
        }
        else -> logger.warn("Unhandled payment status: ${event.status}")
      }
      ack.acknowledge()
    } catch (e: Exception) {
      logger.error("Failed to process payment event", e)
    }
  }

  private fun buildNotificationRequest(
      type: NotificationType,
      title: String,
      body: String,
      userId: Long
  ): CreateNotificationRequest {
    return CreateNotificationRequest(userId = userId, type = type, title = title, body = body)
  }
}
