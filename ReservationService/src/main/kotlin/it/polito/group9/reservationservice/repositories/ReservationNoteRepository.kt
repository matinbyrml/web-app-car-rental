package it.polito.group9.reservationservice.repositories

import it.polito.group9.reservationservice.entities.ReservationNote
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ReservationNoteRepository : JpaRepository<ReservationNote, Long> {
  fun findFirstByReservationIdOrderByIdDesc(reservationId: Long): ReservationNote?
}
