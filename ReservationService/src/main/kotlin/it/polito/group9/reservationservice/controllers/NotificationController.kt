package it.polito.group9.reservationservice.controllers

import it.polito.group9.reservationservice.dtos.CreateNotificationRequest
import it.polito.group9.reservationservice.dtos.NotificationResponse
import it.polito.group9.reservationservice.services.NotificationService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@CrossOrigin(origins = ["*"])
@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController(private val service: NotificationService) {
  @PostMapping
  @PreAuthorize("hasAnyRole('CUSTOMER','STAFF','MANAGER','FLEET_MANAGER')")
  fun create(
      @AuthenticationPrincipal jwt: Jwt,
      @RequestBody req: CreateNotificationRequest
  ): NotificationResponse = service.create(jwt, req)

  @GetMapping
  @PreAuthorize("hasAnyRole('CUSTOMER','STAFF','MANAGER','FLEET_MANAGER')")
  fun list(
      @AuthenticationPrincipal jwt: Jwt,
      @RequestParam(required = false, defaultValue = "ALL") status: String?,
      @RequestParam(defaultValue = "0") page: Int,
      @RequestParam(defaultValue = "20") size: Int
  ): Page<NotificationResponse> =
      service.listForCurrentUser(jwt, status, PageRequest.of(page, size))

  @GetMapping("/unread-count")
  @PreAuthorize("hasAnyRole('CUSTOMER','STAFF','MANAGER','FLEET_MANAGER')")
  fun unreadCount(@AuthenticationPrincipal jwt: Jwt): Map<String, Long> =
      mapOf("count" to service.unreadCountForCurrentUser(jwt))

  @PatchMapping("/{id}/read")
  @PreAuthorize("hasAnyRole('CUSTOMER','STAFF','MANAGER','FLEET_MANAGER')")
  fun markRead(@AuthenticationPrincipal jwt: Jwt, @PathVariable id: Long): NotificationResponse =
      service.markReadOwned(jwt, id)

  @PatchMapping("/mark-all-read")
  @PreAuthorize("hasAnyRole('CUSTOMER','STAFF','MANAGER','FLEET_MANAGER')")
  fun markAllRead(@AuthenticationPrincipal jwt: Jwt): Map<String, Int> =
      mapOf("updated" to service.markAllReadForCurrentUser(jwt))

  @PatchMapping("/{id}/archive")
  @PreAuthorize("hasAnyRole('CUSTOMER','STAFF','MANAGER','FLEET_MANAGER')")
  fun archive(@AuthenticationPrincipal jwt: Jwt, @PathVariable id: Long) =
      service.archiveOwned(jwt, id)

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('CUSTOMER','STAFF','MANAGER','FLEET_MANAGER')")
  fun deleteSoft(@AuthenticationPrincipal jwt: Jwt, @PathVariable id: Long) =
      service.deleteSoftOwned(jwt, id)
}
