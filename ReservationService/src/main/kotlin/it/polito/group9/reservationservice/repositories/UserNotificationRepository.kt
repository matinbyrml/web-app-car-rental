package it.polito.group9.reservationservice.repositories

import it.polito.group9.reservationservice.entities.NotificationStatus
import it.polito.group9.reservationservice.entities.UserNotification
import java.util.Optional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserNotificationRepository : JpaRepository<UserNotification, Long> {
  fun findByUserIdAndDeletedFalse(userId: Long, pageable: Pageable): Page<UserNotification>

  fun findByUserIdAndStatusAndDeletedFalse(
      userId: Long,
      status: NotificationStatus,
      pageable: Pageable
  ): Page<UserNotification>

  fun countByUserIdAndStatusAndDeletedFalse(userId: Long, status: NotificationStatus): Long

  // NEW (ownership checks)
  fun findByIdAndUserIdAndDeletedFalse(id: Long, userId: Long): Optional<UserNotification>
}
