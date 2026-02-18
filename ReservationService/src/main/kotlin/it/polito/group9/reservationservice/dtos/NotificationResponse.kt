package it.polito.group9.reservationservice.dtos

import it.polito.group9.reservationservice.entities.NotificationStatus
import it.polito.group9.reservationservice.entities.NotificationType
import java.time.OffsetDateTime

data class NotificationResponse(
    val id: Long,
    val userId: Long,
    val type: NotificationType,
    val status: NotificationStatus,
    val title: String,
    val body: String,
    val createdBy: String,
    val createdAt: OffsetDateTime
)
