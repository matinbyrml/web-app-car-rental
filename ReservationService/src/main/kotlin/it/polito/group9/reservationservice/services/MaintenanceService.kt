package it.polito.group9.reservationservice.services

import it.polito.group9.reservationservice.dtos.MaintenanceRecordDTO
import it.polito.group9.reservationservice.entities.Vehicle
import org.springframework.data.domain.Page

/**
 * Service interface for managing maintenance records.
 *
 * This interface defines the contract for operations related to maintenance records, including
 * filtering, retrieving, updating, and deleting records.
 */
interface MaintenanceService {

  /**
   * Retrieves a paginated list of maintenance records filtered by various criteria.
   *
   * @param vehicleId The ID of the vehicle to filter maintenance records by.
   * @param startDate The start date for filtering maintenance records (optional).
   * @param endDate The end date for filtering maintenance records (optional).
   * @param startUpcomingServiceNeeds The start date for upcoming service needs (optional).
   * @param endUpcomingServiceNeeds The end date for upcoming service needs (optional).
   * @param page The page number for pagination.
   * @param size The number of records per page.
   * @return A paginated list of MaintenanceRecordDTO objects matching the filters.
   */
  fun getMaintenancesWithFilters(
      vehicleId: Long,
      startDate: String?,
      endDate: String?,
      startUpcomingServiceNeeds: String?,
      endUpcomingServiceNeeds: String?,
      page: Int,
      size: Int
  ): Page<MaintenanceRecordDTO>

  /**
   * Retrieves a maintenance record by its ID.
   *
   * @param id The ID of the maintenance record to retrieve.
   * @return The MaintenanceRecordDTO corresponding to the given ID.
   */
  fun getMaintenanceById(id: Long): MaintenanceRecordDTO

  /**
   * Updates an existing maintenance record with new data.
   *
   * @param dto The MaintenanceRecordDTO containing updated data.
   * @param vehicle The Vehicle entity associated with the maintenance record.
   * @return The updated MaintenanceRecordDTO.
   */
  fun updateMaintenanceRecord(dto: MaintenanceRecordDTO, vehicle: Vehicle): MaintenanceRecordDTO

  /**
   * Deletes a maintenance record by its ID.
   *
   * @param id The ID of the maintenance record to delete.
   */
  fun deleteMaintenanceRecord(id: Long)

  fun createMaintenanceRecord(dto: MaintenanceRecordDTO, vehicle: Vehicle): MaintenanceRecordDTO
}
