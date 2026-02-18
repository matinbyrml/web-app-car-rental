package it.polito.group9.reservationservice.services

import it.polito.group9.reservationservice.dtos.VehicleDTO
import it.polito.group9.reservationservice.entities.CarModel
import it.polito.group9.reservationservice.entities.Vehicle
import java.util.Date
import org.springframework.data.domain.Page

/**
 * Service interface for managing vehicles. This interface defines the contract for vehicle-related
 * operations, including CRUD operations and filtering vehicles based on various criteria.
 */
interface VehicleService {

  /**
   * Retrieves a vehicle by its ID.
   *
   * @param id The ID of the vehicle to retrieve.
   * @return The VehicleDTO representing the vehicle.
   */
  fun getVehicleById(id: Long): VehicleDTO

  /**
   * Retrieves a Vehicle entity by its ID.
   *
   * This method fetches the Vehicle entity from the database using the provided ID. It is typically
   * used internally within the service layer when the full entity is required for further
   * processing or operations.
   *
   * @param id The ID of the vehicle to retrieve.
   * @return The Vehicle entity corresponding to the given ID.
   */
  fun getVehicleEntityById(id: Long): Vehicle

  /**
   * Retrieves a paginated list of vehicles filtered by the provided criteria.
   *
   * @param licensePlate The license plate to filter by (optional).
   * @param vin The VIN to filter by (optional).
   * @param availability The availability status to filter by (optional).
   * @param kmMin The minimum kilometers to filter by (optional).
   * @param kmMax The maximum kilometers to filter by (optional).
   * @param pendingCleaning The cleaning status to filter by (optional).
   * @param pendingRepair The repair status to filter by (optional).
   * @param page The page number for pagination.
   * @param size The page size for pagination.
   * @return A paginated list of VehicleDTOs matching the filters.
   */
  fun getVehiclesWithFilters(
      licensePlate: String?,
      vin: String?,
      availability: String?,
      kmMin: Int?,
      kmMax: Int?,
      pendingCleaning: Boolean?,
      pendingRepair: Boolean?,
      page: Int,
      size: Int
  ): Page<VehicleDTO>

  /**
   * Creates a new vehicle.
   *
   * @param dto The VehicleDTO containing the details of the vehicle to create.
   * @param carModel The CarModel associated with the vehicle.
   * @return The created VehicleDTO.
   */
  fun createVehicle(dto: VehicleDTO, carModel: CarModel): VehicleDTO

  /**
   * Updates an existing vehicle.
   *
   * @param dto The VehicleDTO containing the updated details.
   * @param carModel The CarModel associated with the vehicle.
   * @return The updated VehicleDTO.
   */
  fun updateVehicle(dto: VehicleDTO, carModel: CarModel): VehicleDTO

  /**
   * Deletes a vehicle by its ID.
   *
   * @param id The ID of the vehicle to delete.
   */
  fun deleteVehicle(id: Long)

  /**
   * Retrieves a list of vehicles that are available for reservation within a specified date range.
   *
   * @param startDate The start date of the reservation period.
   * @param endDate The end date of the reservation period.
   * @return A list of VehicleDTOs representing the available vehicles.
   */
  fun getAvailableVehicles(
      startDate: Date,
      endDate: Date,
      page: Int,
      size: Int,
      models: List<String>?
  ): Page<VehicleDTO>

  fun getAssignableVehiclesOptimized(
      startDate: Date,
      endDate: Date,
      page: Int,
      size: Int
  ): Page<VehicleDTO>
}
