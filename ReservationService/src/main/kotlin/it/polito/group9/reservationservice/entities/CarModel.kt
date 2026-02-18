package it.polito.group9.reservationservice.entities

import it.polito.group9.reservationservice.dtos.CarModelDTO
import jakarta.persistence.*

/**
 * Enum class representing different car segments.
 *
 * @property ECONOMY Economy segment.
 * @property COMPACT Compact segment.
 * @property MIDSIZE Midsize segment.
 * @property FULLSIZE Fullsize segment.
 * @property SUV SUV segment.
 * @property LUXURY Luxury segment.
 */
enum class CarSegment {
  ECONOMY,
  COMPACT,
  MIDSIZE,
  FULLSIZE,
  SUV,
  LUXURY
}

/**
 * Enum class representing different types of car engines.
 *
 * @property PETROL Petrol engine type.
 * @property DIESEL Diesel engine type.
 * @property ELECTRIC Electric engine type.
 * @property HYBRID Hybrid engine type.
 */
enum class EngineType {
  PETROL,
  DIESEL,
  ELECTRIC,
  HYBRID
}

/**
 * Enum class representing different types of car transmissions.
 *
 * @property MANUAL Manual transmission type.
 * @property AUTOMATIC Automatic transmission type.
 */
enum class TransmissionType {
  MANUAL,
  AUTOMATIC
}

/**
 * Enum class representing different types of drivetrains.
 *
 * @property FWD Front-wheel drive.
 * @property RWD Rear-wheel drive.
 * @property AWD All-wheel drive.
 */
enum class Drivetrain {
  FWD,
  RWD,
  AWD
}

/**
 * Enum class representing different types of infotainment systems.
 *
 * @property RADIO Radio system.
 * @property USB USB system.
 * @property BLUETOOTH Bluetooth system.
 */
enum class InfotainmentSystem {
  RADIO,
  USB,
  BLUETOOTH
}

@Entity
@Table(name = "car_model")
data class CarModel(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
    @Column(name = "brand", nullable = false) val brand: String,
    @Column(name = "model", unique = true, nullable = false) val model: String,
    @Column(name = "year", nullable = false) val year: Int,
    @Enumerated(EnumType.STRING)
    @Column(name = "segment", nullable = false)
    val segment: CarSegment,
    @Column(name = "doors", nullable = false) val doors: Int,
    @Column(name = "seats", nullable = false) val seats: Int,
    @Column(name = "luggage", nullable = false) val luggage: Int,
    @Column(name = "category", nullable = false) val category: String,
    @Enumerated(EnumType.STRING)
    @Column(name = "engine_type", nullable = false)
    val engineType: EngineType,
    @Enumerated(EnumType.STRING)
    @Column(name = "transmission_type", nullable = false)
    val transmissionType: TransmissionType,
    @Enumerated(EnumType.STRING)
    @Column(name = "drivetrain", nullable = false)
    val drivetrain: Drivetrain,
    @Column(name = "motor_displacement", nullable = false) val motorDisplacement: Int,
    @Column(name = "air_conditioning", nullable = false) val airConditioning: Boolean,
    @Enumerated(EnumType.STRING)
    @Column(name = "infotainment_system", nullable = false)
    val infotainmentSystem: InfotainmentSystem,
    @Column(name = "safety_features", nullable = false) val safetyFeatures: String,
    @Column(name = "price", nullable = false) val price: Double,
) {
  fun toDTO(): CarModelDTO =
      CarModelDTO(
          id = id,
          brand = brand,
          model = model,
          year = year,
          segment = segment,
          doors = doors,
          seats = seats,
          luggage = luggage,
          category = category,
          engineType = engineType,
          transmissionType = transmissionType,
          drivetrain = drivetrain,
          motorDisplacement = motorDisplacement,
          airConditioning = airConditioning,
          infotainmentSystem = infotainmentSystem,
          safetyFeatures = safetyFeatures,
          price = price)
}
