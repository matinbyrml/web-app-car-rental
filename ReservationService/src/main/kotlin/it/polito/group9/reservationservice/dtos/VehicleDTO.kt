package it.polito.group9.reservationservice.dtos

import io.swagger.v3.oas.annotations.media.Schema
import it.polito.group9.reservationservice.entities.AvailabilityStatus
import it.polito.group9.reservationservice.entities.Vehicle
import jakarta.validation.constraints.Min

/**
 * Data Transfer Object (DTO) representing a vehicle. This class is used to transfer vehicle data
 * between different layers of the application.
 *
 * @property id The unique identifier of the vehicle. Nullable for new vehicles.
 * @property licensePlate The license plate of the vehicle.
 * @property vin The Vehicle Identification Number (VIN) of the vehicle.
 * @property availability The availability status of the vehicle (e.g., AVAILABLE, UNAVAILABLE).
 * @property km The current mileage of the vehicle in kilometers.
 * @property pendingCleaning Indicates whether the vehicle is pending cleaning.
 * @property pendingRepair Indicates whether the vehicle is pending repair.
 * @property maintenanceRecordHistory A list of maintenance records associated with the vehicle.
 * @property vehicleModelId The ID of the vehicle model associated with this vehicle.
 */
data class VehicleDTO(
    @field:Schema(description = "Unique identifier of the vehicle", example = "1")
    val id: Long? = null,
    @field:Schema(description = "Vehicle's license plate", example = "AB123CD")
    val licensePlate: String,
    @field:Schema(description = "Vehicle's VIN", example = "1HGCM82633A123456") val vin: String,
    @field:Schema(description = "Vehicle's availability status", example = "AVAILABLE")
    val availability: String,
    @field:Min(value = 0)
    @field:Schema(description = "Vehicle's current mileage in kilometers", example = "15000")
    val km: Int,
    @field:Schema(description = "Pending cleaning status of the vehicle", example = "false")
    val pendingCleaning: Boolean,
    @field:Schema(description = "Pending repair status of the vehicle", example = "false")
    val pendingRepair: Boolean,
    @field:Schema(description = "Maintenance record history", example = "[]")
    val maintenanceRecordHistory: List<MaintenanceRecordDTO> = emptyList(),
    @field:Schema(description = "Notes associated with the vehicle", example = "[]")
    val vehicleModelId: Long,
) {
  fun toEntity(): Vehicle =
      Vehicle(
          id = id,
          licensePlate = licensePlate,
          vin = vin,
          availability = AvailabilityStatus.valueOf(availability),
          km = km,
          pendingCleaning = pendingCleaning,
          pendingRepair = pendingRepair,
          maintenanceRecordHistory = emptyList(),
          vehicleModel = null,
      )
}
