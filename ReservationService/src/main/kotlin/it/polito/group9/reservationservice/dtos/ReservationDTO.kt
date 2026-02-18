package it.polito.group9.reservationservice.dtos

import io.swagger.v3.oas.annotations.media.Schema
import it.polito.group9.reservationservice.entities.Reservation
import it.polito.group9.reservationservice.entities.ReservationStatus

/**
 * Data Transfer Object (DTO) for Reservation. Represents the data structure used to transfer
 * reservation information between layers.
 *
 * @property id The unique identifier of the reservation.
 * @property vehicleId The ID of the vehicle associated with the reservation.
 * @property userId The ID of the user who made the reservation.
 * @property startDate The start date of the reservation in ISO 8601 format.
 * @property endDate The end date of the reservation in ISO 8601 format.
 * @property status The status of the reservation (e.g., PENDING, PICKED_UP, RETURNED, CANCELLED).
 */
data class ReservationDTO(
    @field:Schema(description = "Unique identifier of the reservation", example = "1")
    val id: Long? = null,
    @field:Schema(description = "ID of the vehicle associated with the reservation", example = "1")
    val vehicleId: Long,
    @field:Schema(description = "ID of the user who made the reservation", example = "1")
    val userId: Long,
    @field:Schema(description = "Start date of the reservation", example = "2023-10-01T10:00:00")
    val startDate: String,
    @field:Schema(description = "End date of the reservation", example = "2023-10-10T10:00:00")
    val endDate: String,
    val status: ReservationStatus? = null,
    @field:Schema(description = "Vehicle license plate", example = "VIN-BMW3-0004")
    val vehicleLicensePlate: String? = null,
) {
  /**
   * Converts this DTO to a Reservation entity.
   *
   * @return A Reservation entity with the data from this DTO.
   */
  fun toEntity(): Reservation {
    return Reservation(
        id = this.id,
        vehicle = null, // Vehicle will be set later
        userId = userId,
        startDate = startDate,
        endDate = endDate,
        status = status)
  }
}
