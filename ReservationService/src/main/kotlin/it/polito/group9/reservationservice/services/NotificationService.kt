package it.polito.group9.reservationservice.services

import it.polito.group9.reservationservice.dtos.CreateNotificationRequest
import it.polito.group9.reservationservice.dtos.NotificationResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.oauth2.jwt.Jwt

interface NotificationService {
  fun create(jwt: Jwt, req: CreateNotificationRequest): NotificationResponse

  fun create(req: CreateNotificationRequest): NotificationResponse

  fun listForCurrentUser(jwt: Jwt, status: String?, pageable: Pageable): Page<NotificationResponse>

  fun unreadCountForCurrentUser(jwt: Jwt): Long

  fun markAllReadForCurrentUser(jwt: Jwt): Int

  fun markReadOwned(jwt: Jwt, id: Long): NotificationResponse

  fun archiveOwned(jwt: Jwt, id: Long)

  fun deleteSoftOwned(jwt: Jwt, id: Long)
}
