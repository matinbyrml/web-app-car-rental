package it.polito.group9.reservationservice.services.impl

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.group9.reservationservice.entities.Reservation
import it.polito.group9.reservationservice.entities.ReservationOutbox
import it.polito.group9.reservationservice.entities.ReservationStatus
import it.polito.group9.reservationservice.kafka.ReservationCreatedEvent
import it.polito.group9.reservationservice.repositories.ReservationOutboxRepository
import it.polito.group9.reservationservice.services.OutboxService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class OutboxServiceImpl(
    private val reservationOutboxRepository: ReservationOutboxRepository,
    private val objectMapper: ObjectMapper
) : OutboxService {

  private val logger = LoggerFactory.getLogger(OutboxServiceImpl::class.java)

  override fun publishReservationEvent(
      reservation: Reservation,
      eventType: ReservationStatus,
      amount: Long?
  ) {
    val payloadJson =
        when (eventType) {
          ReservationStatus.PENDING -> {
            val finalAmount = amount ?: 0L
            val event =
                ReservationCreatedEvent(
                    eventType = eventType,
                    reservationId = reservation.id!!,
                    userId = reservation.userId,
                    amount = finalAmount)
            objectMapper.writeValueAsString(event)
          }
          else -> {
            val event =
                ReservationCreatedEvent(
                    eventType = eventType,
                    reservationId = reservation.id!!,
                    userId = reservation.userId,
                    amount = null)
            objectMapper.writeValueAsString(event)
          }
        }

    reservationOutboxRepository.save(
        ReservationOutbox(
            aggregateType = "Reservation",
            aggregateId = reservation.id!!,
            eventType = eventType,
            payload = payloadJson))
    logger.info(
        "Reservation outbox event published: reservationId={}, eventType={}",
        reservation.id,
        eventType)
  }
}
