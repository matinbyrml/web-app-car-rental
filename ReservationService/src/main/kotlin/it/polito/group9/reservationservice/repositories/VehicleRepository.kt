package it.polito.group9.reservationservice.repositories

import it.polito.group9.reservationservice.entities.ReservationStatus
import it.polito.group9.reservationservice.entities.Vehicle
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 * Repository interface for managing Vehicle entities. This interface provides methods for
 * performing CRUD operations and custom queries on the Vehicle entity using Spring Data JPA.
 */
interface VehicleRepository : JpaRepository<Vehicle, Long>, JpaSpecificationExecutor<Vehicle> {

  /**
   * Finds a Vehicle entity by its license plate.
   *
   * @param licensePlate The license plate of the vehicle to search for.
   * @return The Vehicle entity if found, or null if no vehicle with the given license plate exists.
   */
  fun findByLicensePlate(licensePlate: String): Vehicle?

  fun findByVin(vin: String): Vehicle?

  @Query(
      """
    select * 
from Vehicle v
where v.id = :id and not exists (
    select 1 
    from Reservation r 
    where r.vehicle_id = v.id and r.start_date <= :endDate and r.end_date >= :startDate and r.status != :cancelled and r.status != :cancelled_pending_refund or v.pending_cleaning or v.pending_repair
)
  """,
      nativeQuery = true)
  fun findAvailableVehicle(
      @Param("id") id: Long,
      @Param("startDate") startDate: String,
      @Param("endDate") endDate: String,
      @Param("cancelled") cancelled: String = ReservationStatus.CANCELLED.ordinal.toString(),
      @Param("cancelled_pending_refund")
      cancelled_pending_refund: String =
          ReservationStatus.CANCELLED_PENDING_REFUND.ordinal.toString()
  ): Vehicle?

  @Query(
      """
    select * 
from Vehicle v
where not exists (
    select 1 
    from Reservation r 
    where r.vehicle_id = v.id and r.start_date <= :endDate and r.end_date >= :startDate and r.status != :cancelled and r.status != :cancelled_pending_refund or v.pending_cleaning or v.pending_repair
)
  """,
      nativeQuery = true)
  fun findAvailableVehicles(
      @Param("startDate") startDate: String,
      @Param("endDate") endDate: String,
      @Param("cancelled") cancelled: String = ReservationStatus.CANCELLED.ordinal.toString(),
      @Param("cancelled_pending_refund")
      cancelled_pending_refund: String =
          ReservationStatus.CANCELLED_PENDING_REFUND.ordinal.toString()
  ): List<Vehicle>

  @Query(
      """
    select v 
    from Vehicle v
    where v.id not in (
        select r.vehicle.id 
        from Reservation r 
        where r.vehicle = v and r.startDate <= :endDate and r.endDate >= :startDate
    ) and v.pendingCleaning = false and v.pendingRepair = false
    order by v.km
  """)
  fun getAssignableVehiclesOptimized(
      @Param("startDate") startDate: String,
      @Param("endDate") endDate: String
  ): List<Vehicle>
}
