package it.polito.group9.reservationservice.dtos

import io.swagger.v3.oas.annotations.media.Schema
import it.polito.group9.reservationservice.entities.*
import jakarta.validation.constraints.Min

/**
 * Data Transfer Object (DTO) for CarModel. This class is used to transfer car model data between
 * the client and server.
 *
 * @property id Unique identifier of the car model.
 * @property brand Brand of the car.
 * @property model Model name of the car.
 * @property year Manufacturing year of the car.
 * @property segment Vehicle segment classification.
 * @property doors Number of doors in the car.
 * @property seats Number of seats available in the car.
 * @property luggage Luggage capacity in liters.
 * @property category Internal category classification for the car.
 * @property engineType Type of engine used in the car.
 * @property transmissionType Type of transmission.
 * @property drivetrain Drivetrain type (e.g. FWD, RWD, AWD).
 * @property motorDisplacement Motor displacement in cubic centimeters (cc).
 * @property airConditioning Indicates if the car has air conditioning.
 * @property infotainmentSystem Type of infotainment system.
 * @property safetyFeatures List of safety features (as a comma-separated string).
 * @property price Daily rental price of the car in EUR.
 */
data class CarModelDTO(
    @field:Schema(description = "Unique identifier of the car model", example = "1")
    val id: Long? = null,
    @field:Schema(description = "Brand of the car", example = "Toyota") val brand: String,
    @field:Schema(description = "Model name of the car", example = "Corolla") val model: String,
    @field:Min(value = 1800)
    @field:Schema(description = "Manufacturing year of the car", example = "2022")
    val year: Int,
    @field:Schema(description = "Vehicle segment classification", example = "COMPACT")
    val segment: CarSegment,
    @field:Min(value = 1)
    @field:Schema(description = "Number of doors in the car", example = "4")
    val doors: Int,
    @field:Min(value = 1)
    @field:Schema(description = "Number of seats available in the car", example = "5")
    val seats: Int,
    @field:Min(value = 0)
    @field:Schema(description = "Luggage capacity in liters", example = "450")
    val luggage: Int,
    @field:Schema(
        description = "Internal category classification for the car", example = "Standard")
    val category: String,
    @field:Schema(description = "Type of engine used in the car", example = "HYBRID")
    val engineType: EngineType,
    @field:Schema(description = "Type of transmission", example = "AUTOMATIC")
    val transmissionType: TransmissionType,
    @field:Schema(description = "Drivetrain type (e.g. FWD, RWD, AWD)", example = "FWD")
    val drivetrain: Drivetrain,
    @field:Min(value = 1)
    @field:Schema(description = "Motor displacement in cubic centimeters (cc)", example = "1600")
    val motorDisplacement: Int,
    @field:Schema(description = "Indicates if the car has air conditioning", example = "true")
    val airConditioning: Boolean,
    @field:Schema(description = "Type of infotainment system", example = "BLUETOOTH")
    val infotainmentSystem: InfotainmentSystem,
    @field:Schema(
        description = "List of safety features (as a comma-separated string)",
        example = "ABS, Airbags, Lane Assist")
    val safetyFeatures: String,
    @field:Min(value = 0)
    @field:Schema(description = "Daily rental price of the car in EUR", example = "49.99")
    val price: Double,
) {
  fun toEntity(): CarModel =
      CarModel(
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
