package it.polito.group9.reservationservice.entities

import jakarta.persistence.*
import java.time.OffsetDateTime

enum class NotificationType {
  SYSTEM,
  RESERVATION,
  PAYMENT,
  VEHICLE
}

enum class NotificationStatus {
  UNREAD,
  READ,
  ARCHIVED
}

@Entity
@Table(name = "user_notifications")
class UserNotification(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @Column(name = "user_id", nullable = false) var userId: Long,
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 40)
    var type: NotificationType = NotificationType.SYSTEM,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: NotificationStatus = NotificationStatus.UNREAD,
    @Column(name = "title", nullable = false, length = 120) var title: String,
    @Column(name = "body", nullable = false, columnDefinition = "TEXT") var body: String,
    @Column(name = "created_by", nullable = false, length = 80) var createdBy: String,
    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    var createdAt: OffsetDateTime = OffsetDateTime.now(),
    @Column(name = "is_deleted", nullable = false) var deleted: Boolean = false
)
