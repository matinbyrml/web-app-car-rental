package it.polito.group9.reservationservice.repositories

import it.polito.group9.reservationservice.entities.MaintenanceRecord
import it.polito.group9.reservationservice.entities.MaintenanceStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

/**
 * Repository interface for managing MaintenanceRecord entities.
 *
 * This interface provides CRUD operations and supports custom queries using JPA specifications for
 * the MaintenanceRecord entity.
 */
@Repository
interface MaintenanceRepository :
    JpaRepository<MaintenanceRecord, Long>, // Provides basic CRUD operations for MaintenanceRecord.
    JpaSpecificationExecutor<
        MaintenanceRecord> // Enables the use of JPA specifications for custom queries.
{
  /**
   * Find all maintenance records for a given vehicle ID with statuses in the provided list.
   *
   * @param vehicleId The ID of the vehicle.
   * @param statuses The list of maintenance statuses to filter by.
   * @return A list of matching MaintenanceRecord entities.
   */
  fun findByVehicleIdAndStatusIn(
      vehicleId: Long,
      statuses: List<MaintenanceStatus>
  ): List<MaintenanceRecord>
}
