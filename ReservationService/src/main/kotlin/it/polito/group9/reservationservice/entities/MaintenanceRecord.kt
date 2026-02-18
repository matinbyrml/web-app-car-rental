package it.polito.group9.reservationservice.entities

import com.fasterxml.jackson.annotation.JsonBackReference
import it.polito.group9.reservationservice.dtos.MaintenanceRecordDTO
import jakarta.persistence.*
import java.util.*

/**
 * Entity class representing a maintenance record.
 *
 * This class maps to the "maintenance" table in the database and contains information about past
 * defects, completed maintenance, upcoming service needs, and the associated vehicle.
 *
 * @property id The unique identifier of the maintenance record.
 * @property pastDefects A description of past defects for the vehicle.
 * @property completedMaintenance A description of completed maintenance tasks.
 * @property upcomingServiceNeeds The date of upcoming service needs for the vehicle.
 * @property date The date of the maintenance record.
 * @property vehicle The vehicle associated with this maintenance record.
 */
@Entity
@Table(name = "maintenance")
data class MaintenanceRecord(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
    @Column(name = "past_defects", nullable = false) val pastDefects: String,
    @Column(name = "completed_maintenance", nullable = false) val completedMaintenance: String,
    @Column(name = "upcoming_service_needs", nullable = false) val upcomingServiceNeeds: Date,
    @Column(name = "date", nullable = false) val date: Date,
    @Column(name = "requires_further_maintenance", nullable = false)
    val requiresFurtherMaintenance: Boolean = false,
    @Column(name = "requires_further_cleaning", nullable = false)
    val requiresFurtherCleaning: Boolean = false,
    @ManyToOne(cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", referencedColumnName = "id")
    @JsonBackReference // Prevents infinite nesting in JSON serialization.
    val vehicle: Vehicle? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: MaintenanceStatus = MaintenanceStatus.SCHEDULED
) {
  /**
   * Converts this MaintenanceRecord entity to a MaintenanceRecordDTO.
   *
   * @return A MaintenanceRecordDTO containing the data from this entity.
   */
  fun toDTO(): MaintenanceRecordDTO =
      MaintenanceRecordDTO(
          id = id,
          pastDefects = pastDefects,
          completedMaintenance = completedMaintenance,
          upcomingServiceNeeds = upcomingServiceNeeds,
          date = date,
          vehicleId = vehicle!!.id!!,
          requiresFurtherMaintenance = requiresFurtherMaintenance,
          requiresFurtherCleaning = requiresFurtherCleaning)
}

enum class MaintenanceStatus {
  SCHEDULED,
  IN_PROGRESS,
  COMPLETED
}
