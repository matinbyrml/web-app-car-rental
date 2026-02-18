package it.polito.group9.reservationservice.dtos

import it.polito.group9.reservationservice.entities.CleanlinessStatus
import java.time.OffsetDateTime

/** DTO per la ReservationNote, usato per pickup e return. */
data class ReservationNoteDTO(
    val id: Long? = null,
    val reservationId: Long? = null,
    val vehicleId: Long? = null,
    val startAt: OffsetDateTime? = null,
    val endAt: OffsetDateTime? = null,
    val kmAtPickup: Int? = null,
    val kmAtReturn: Int? = null,
    val cleanliness: CleanlinessStatus? = null,
    val needsMaintenance: Boolean? = null,
    val damages: List<String>? = null
)
