package it.polito.group9.usermanagementservice.dtos

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Data Transfer Object (DTO) for Reservation. Represents the data structure used to transfer
 * reservation information between layers.
 *
 * @property id The unique identifier of the reservation.
 * @property vehicleId The ID of the vehicle associated with the reservation.
 * @property userId The ID of the user who made the reservation.
 * @property startDate The start date of the reservation in ISO 8601 format.
 * @property endDate The end date of the reservation in ISO 8601 format.
 */
data class ReservationDTO(
    @field:Schema(description = "Unique identifier of the reservation", example = "1") val id: Long,
    @field:Schema(description = "ID of the vehicle associated with the reservation", example = "1")
    val vehicleId: Long,
    @field:Schema(description = "ID of the user who made the reservation", example = "1")
    val userId: Long,
    @field:Schema(description = "Start date of the reservation", example = "2023-10-01T10:00:00")
    val startDate: String,
    @field:Schema(description = "End date of the reservation", example = "2023-10-10T10:00:00")
    val endDate: String,
)
