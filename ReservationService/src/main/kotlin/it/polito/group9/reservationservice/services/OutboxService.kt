package it.polito.group9.reservationservice.services

import it.polito.group9.reservationservice.entities.Reservation
import it.polito.group9.reservationservice.entities.ReservationStatus

interface OutboxService {
  fun publishReservationEvent(
      reservation: Reservation,
      eventType: ReservationStatus,
      amount: Long? = null
  )
}
