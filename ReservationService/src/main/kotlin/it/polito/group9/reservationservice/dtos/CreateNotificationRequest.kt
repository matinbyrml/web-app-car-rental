package it.polito.group9.reservationservice.dtos

import it.polito.group9.reservationservice.entities.NotificationType

data class CreateNotificationRequest(
    val userId: Long,
    val type: NotificationType = NotificationType.RESERVATION,
    val title: String,
    val body: String
)
