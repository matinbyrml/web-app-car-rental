package it.polito.group9.cartrackingservice.dtos

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

data class CreateCarLocationRequest(
    @field:NotNull
    @field:Pattern(
        regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?(Z|[+-]\\d{2}:\\d{2})?$")
    val createdAt: String,
    @field:NotNull val latitude: Double,
    @field:NotNull val longitude: Double,
    @field:NotNull val carId: Long
)
