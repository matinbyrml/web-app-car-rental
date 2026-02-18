package it.polito.group9.reservationservice.repositories

import it.polito.group9.reservationservice.entities.Reservation
import it.polito.group9.reservationservice.entities.ReservationStatus
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Repository interface for managing `Reservation` entities. Extends `JpaRepository` to provide
 * basic CRUD operations and `JpaSpecificationExecutor` to support complex queries using
 * specifications.
 */
@Repository
interface ReservationRepository :
    JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {
  @Modifying
  @Transactional
  @Query("UPDATE Reservation r SET r.status = :status WHERE r.id = :id")
  fun updateStatus(@Param("id") id: Long, @Param("status") status: ReservationStatus): Int

  fun findFirstByVehicleIdAndStartDateAfterAndStatusInOrderByStartDateAsc(
      vehicleId: Long,
      startDate: String,
      statuses: List<ReservationStatus>
  ): Reservation?
}
