package it.polito.group9.reservationservice.entities

import com.fasterxml.jackson.annotation.JsonManagedReference
import it.polito.group9.reservationservice.dtos.VehicleDTO
import jakarta.persistence.*

/**
 * Enum class representing the availability status of a vehicle. The vehicle can be in one of the
 * following states:
 * - AVAILABLE: The vehicle is available for use.
 * - RENTED: The vehicle is currently rented.
 * - MAINTENANCE: The vehicle is under maintenance.
 */
enum class AvailabilityStatus {
  AVAILABLE,
  RENTED,
  MAINTENANCE
}

/**
 * Entity class representing a vehicle in the system. This class is mapped to the "vehicle" table in
 * the database.
 *
 * @property id The unique identifier of the vehicle.
 * @property licensePlate The license plate of the vehicle. Must be unique and not null.
 * @property vin The Vehicle Identification Number (VIN). Must be unique and not null.
 * @property availability The current availability status of the vehicle.
 * @property km The current mileage of the vehicle in kilometers.
 * @property pendingCleaning Indicates whether the vehicle is pending cleaning.
 * @property pendingRepair Indicates whether the vehicle is pending repair.
 * @property maintenanceRecordHistory A list of maintenance records associated with the vehicle.
 * @property vehicleModel The model of the vehicle, represented as a reference to the `CarModel`
 *   entity.
 */
@Entity
@Table(name = "vehicle")
data class Vehicle(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
    @Column(name = "license_plate", unique = true, nullable = false) val licensePlate: String,
    @Column(name = "vin", unique = true, nullable = false) val vin: String,
    @Column(name = "availability", nullable = false)
    @Enumerated(EnumType.STRING)
    var availability: AvailabilityStatus,
    @Column(name = "km", nullable = false) var km: Int,
    @Column(name = "pending_cleaning", nullable = false) var pendingCleaning: Boolean,
    @Column(name = "pending_repair", nullable = false) var pendingRepair: Boolean,
    @OneToMany(mappedBy = "vehicle", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JsonManagedReference
    val maintenanceRecordHistory: List<MaintenanceRecord>,
    @ManyToOne @JoinColumn(name = "vehicle_model_id") val vehicleModel: CarModel? = null
) {
  fun toDTO(): VehicleDTO =
      VehicleDTO(
          id = id,
          licensePlate = licensePlate,
          vin = vin,
          availability = availability.name,
          km = km,
          pendingCleaning = pendingCleaning,
          pendingRepair = pendingRepair,
          maintenanceRecordHistory = maintenanceRecordHistory.map { it.toDTO() },
          vehicleModelId = vehicleModel!!.id!!)
}
