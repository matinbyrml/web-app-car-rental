package it.polito.group9.reservationservice.services.impl

import it.polito.group9.reservationservice.dtos.CreateNotificationRequest
import it.polito.group9.reservationservice.dtos.NotificationResponse
import it.polito.group9.reservationservice.entities.NotificationStatus
import it.polito.group9.reservationservice.entities.UserNotification
import it.polito.group9.reservationservice.repositories.UserNotificationRepository
import it.polito.group9.reservationservice.services.KeycloakService
import it.polito.group9.reservationservice.services.NotificationService
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate

@Service
class NotificationServiceImpl(
    private val repo: UserNotificationRepository,
    private val restTemplate: RestTemplate,
    private val keycloakService:
        KeycloakService // same service you use to get client-credentials token
) : NotificationService {

  private val logger = LoggerFactory.getLogger(javaClass)

  // --- Helpers ------------------------------------------------------------

  data class UserDTO(val id: Long, val username: String)

  private fun getAccessTokenHeader(): HttpHeaders =
      HttpHeaders().apply { setBearerAuth(keycloakService.getAccessToken(restTemplate)) }

  private fun resolveCurrentUserId(jwt: Jwt): Long {
    val username = jwt.getClaim<String>("preferred_username")
    val resp =
        restTemplate.exchange(
            "http://localhost:8081/api/v1/users/$username",
            HttpMethod.GET,
            HttpEntity<String>(getAccessTokenHeader()),
            object : ParameterizedTypeReference<UserDTO>() {})
    val user = resp.body ?: error("User not found for username: $username")
    return user.id
  }

  private fun map(n: UserNotification) =
      NotificationResponse(
          id = n.id!!,
          userId = n.userId,
          type = n.type,
          status = n.status,
          title = n.title,
          body = n.body,
          createdBy = n.createdBy,
          createdAt = n.createdAt)

  // --- API ---------------------------------------------------------------

  @Transactional
  override fun create(jwt: Jwt, req: CreateNotificationRequest): NotificationResponse {
    val currentUserId = resolveCurrentUserId(jwt)

    // If you also want to allow staff to create notifications for someone else,
    // you can check roles here and prefer req.userId when authorized.
    val targetUserId = currentUserId

    val n =
        UserNotification(
            userId = targetUserId,
            type = req.type,
            status = NotificationStatus.UNREAD,
            title = req.title,
            body = req.body,
            createdBy = jwt.getClaim("preferred_username"))
    return map(repo.save(n))
  }

  override fun create(req: CreateNotificationRequest): NotificationResponse {
    val n =
        UserNotification(
            userId = req.userId,
            type = req.type,
            status = NotificationStatus.UNREAD,
            title = req.title,
            body = req.body,
            createdBy = "System")
    return map(repo.save(n))
  }

  override fun listForCurrentUser(
      jwt: Jwt,
      status: String?,
      pageable: Pageable
  ): Page<NotificationResponse> {
    val userId = resolveCurrentUserId(jwt)
    return if (status == null || status.equals("ALL", true))
        repo.findByUserIdAndDeletedFalse(userId, pageable).map(::map)
    else
        repo
            .findByUserIdAndStatusAndDeletedFalse(
                userId, NotificationStatus.valueOf(status.uppercase()), pageable)
            .map(::map)
  }

  override fun unreadCountForCurrentUser(jwt: Jwt): Long {
    val userId = resolveCurrentUserId(jwt)
    return repo.countByUserIdAndStatusAndDeletedFalse(userId, NotificationStatus.UNREAD)
  }

  @Transactional
  override fun markAllReadForCurrentUser(jwt: Jwt): Int {
    val userId = resolveCurrentUserId(jwt)
    val page =
        repo.findByUserIdAndStatusAndDeletedFalse(
            userId, NotificationStatus.UNREAD, Pageable.ofSize(500))
    var updated = 0
    page.forEach {
      it.status = NotificationStatus.READ
      repo.save(it)
      updated++
    }
    return updated
  }

  @Transactional
  override fun markReadOwned(jwt: Jwt, id: Long): NotificationResponse {
    val userId = resolveCurrentUserId(jwt)
    val n =
        repo.findByIdAndUserIdAndDeletedFalse(id, userId).orElseThrow {
          NoSuchElementException("Notification not found or not owned")
        }
    n.status = NotificationStatus.READ
    return map(repo.save(n))
  }

  @Transactional
  override fun archiveOwned(jwt: Jwt, id: Long) {
    val userId = resolveCurrentUserId(jwt)
    val n =
        repo.findByIdAndUserIdAndDeletedFalse(id, userId).orElseThrow {
          NoSuchElementException("Notification not found or not owned")
        }
    n.status = NotificationStatus.ARCHIVED
    repo.save(n)
  }

  @Transactional
  override fun deleteSoftOwned(jwt: Jwt, id: Long) {
    val userId = resolveCurrentUserId(jwt)
    val n =
        repo.findByIdAndUserIdAndDeletedFalse(id, userId).orElseThrow {
          NoSuchElementException("Notification not found or not owned")
        }
    n.deleted = true
    repo.save(n)
  }
}
