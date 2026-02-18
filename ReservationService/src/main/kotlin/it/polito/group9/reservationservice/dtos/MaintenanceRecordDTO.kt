package it.polito.group9.reservationservice.dtos

import io.swagger.v3.oas.annotations.media.Schema
import it.polito.group9.reservationservice.entities.MaintenanceRecord
import it.polito.group9.reservationservice.entities.MaintenanceStatus
import java.util.*

/**
 * Data Transfer Object (DTO) for MaintenanceRecord.
 *
 * This class is used to transfer data related to maintenance records between different layers of
 * the application. It includes information about past defects, completed maintenance, upcoming
 * service needs, and the associated vehicle.
 *
 * @property id The unique identifier of the maintenance record.
 * @property pastDefects A description of past defects for the vehicle.
 * @property completedMaintenance A description of completed maintenance tasks.
 * @property upcomingServiceNeeds The date of upcoming service needs for the vehicle.
 * @property date The date of the maintenance record.
 * @property vehicleId The ID of the vehicle associated with this maintenance record.
 */
data class MaintenanceRecordDTO(
    @field:Schema(description = "ID of the maintenance record", example = "1") val id: Long? = null,
    @field:Schema(description = "Description of past defects", example = "Brake issues")
    val pastDefects: String,
    @field:Schema(
        description = "Description of completed maintenance", example = "Brake pads replaced")
    val completedMaintenance: String,
    @field:Schema(description = "Date of upcoming service needs", example = "2023-12-01")
    val upcomingServiceNeeds: Date,
    @field:Schema(description = "Date of the maintenance record", example = "2023-10-01")
    val date: Date,
    @field:Schema(description = "Vehicle associated with this maintenance", example = "42")
    val vehicleId: Long,
    @field:Schema(
        description = "Types of maintenance performed",
        example = "[\"BRAKE_REPAIR\", \"OIL_CHANGE\"]")
    val maintenanceTypes: List<String> = emptyList(),
    @field:Schema(description = "Indicates if further maintenance is required", example = "true")
    val requiresFurtherMaintenance: Boolean = false,
    @field:Schema(description = "Indicates if further cleaning is required", example = "true")
    val requiresFurtherCleaning: Boolean = false,
    @field:Schema(description = "Status of the maintenance", example = "SCHEDULED")
    val status: String = "SCHEDULED",
) {
  /**
   * Converts this MaintenanceRecordDTO to a MaintenanceRecord entity.
   *
   * @return A MaintenanceRecord entity containing the data from this DTO.
   */
  fun toEntity(): MaintenanceRecord =
      MaintenanceRecord(
          id = id,
          pastDefects = pastDefects,
          completedMaintenance = completedMaintenance,
          upcomingServiceNeeds = upcomingServiceNeeds,
          date = date,
          vehicle = null, // Set to null as the vehicle is not needed in this context
          requiresFurtherMaintenance = requiresFurtherMaintenance,
          requiresFurtherCleaning = requiresFurtherCleaning,
          status = MaintenanceStatus.valueOf(status))
}
