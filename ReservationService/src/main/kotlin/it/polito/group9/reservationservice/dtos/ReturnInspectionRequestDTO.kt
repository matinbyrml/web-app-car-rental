package it.polito.group9.reservationservice.dtos

import it.polito.group9.reservationservice.entities.CleanlinessStatus

/** DTO per ispezione al ritorno. */
data class ReturnInspectionRequestDTO(
    val kmAtReturn: Int,
    val cleanliness: CleanlinessStatus,
    val needsMaintenance: Boolean,
    val damages: List<String>? = null
)
