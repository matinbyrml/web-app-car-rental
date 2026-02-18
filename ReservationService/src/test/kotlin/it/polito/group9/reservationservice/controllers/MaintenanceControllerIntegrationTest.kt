package it.polito.group9.reservationservice.controllers

import TestUtils.getHeaders
import TestUtils.getUser
import it.polito.group9.reservationservice.dtos.MaintenanceRecordDTO
import it.polito.group9.reservationservice.entities.*
import it.polito.group9.reservationservice.repositories.CarModelRepository
import it.polito.group9.reservationservice.repositories.MaintenanceRepository
import it.polito.group9.reservationservice.repositories.VehicleRepository
import java.util.*
import kotlin.test.assertEquals
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MaintenanceControllerIntegrationTest : IntegrationTest() {

  @LocalServerPort private var port: Int = 0

  @Autowired private lateinit var restTemplate: TestRestTemplate

  @Autowired private lateinit var maintenanceRepository: MaintenanceRepository

  @Autowired private lateinit var vehicleRepository: VehicleRepository

  @Autowired private lateinit var carModelRepository: CarModelRepository

  private lateinit var baseUrl: String
  private lateinit var vehicle: Vehicle
  private lateinit var carModel: CarModel

  @BeforeEach
  fun setup() {
    vehicleRepository.deleteAll()
    maintenanceRepository.deleteAll()
    carModelRepository.deleteAll()

    carModel =
        carModelRepository.save(
            CarModel(
                brand = "Toyota",
                model = "Corolla",
                year = 2020,
                segment = CarSegment.COMPACT,
                doors = 4,
                seats = 5,
                luggage = 450,
                category = "Standard",
                engineType = EngineType.HYBRID,
                transmissionType = TransmissionType.AUTOMATIC,
                drivetrain = Drivetrain.FWD,
                motorDisplacement = 1600,
                airConditioning = true,
                infotainmentSystem = InfotainmentSystem.BLUETOOTH,
                safetyFeatures = "Airbags, ABS",
                price = 20000.0))

    vehicle =
        vehicleRepository.save(
            Vehicle(
                licensePlate = "AB123CD",
                vin = "1HGCM82633A123456",
                availability = AvailabilityStatus.AVAILABLE,
                km = 15000,
                pendingCleaning = false,
                pendingRepair = false,
                maintenanceRecordHistory = emptyList(),
                vehicleModel = carModel))

    baseUrl = "http://localhost:${port}/api/v1/vehicles/${vehicle.id}/maintenances"
  }

  @Nested
  @DisplayName("GET /api/v1/vehicles/{vehicleId}/maintenances")
  inner class GetMaintenancesTests {
    @ParameterizedTest(name = "should return empty list when no maintenances exist for {0}")
    @ValueSource(strings = ["MANAGER", "FLEET_MANAGER", "STAFF"])
    fun `should return empty list when no maintenances exist`(user: String) {
      val userObject = getUser(user)
      val request = HttpEntity(null, getHeaders(userObject, ::getAccessToken))
      val response = restTemplate.exchange(baseUrl, HttpMethod.GET, request, Any::class.java)

      assertEquals(HttpStatus.OK, response.statusCode)
      val body = response.body as Map<*, *>
      assertEquals(0, body["totalElements"])
    }

    @ParameterizedTest(name = "should return 403 for {0}")
    @ValueSource(strings = ["CUSTOMER"])
    fun `should return 403 for other roles`(user: String) {
      val userObject = getUser(user)
      val request = HttpEntity(null, getHeaders(userObject, ::getAccessToken))
      val response = restTemplate.exchange(baseUrl, HttpMethod.GET, request, Any::class.java)

      assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }
  }

  @Nested
  @DisplayName("GET /api/v1/vehicles/{vehicleId}/maintenances/{id}")
  inner class GetMaintenanceByIdTests {

    @ParameterizedTest(name = "should return 404 when maintenance does not exist for {0}")
    @ValueSource(strings = ["MANAGER", "FLEET_MANAGER", "STAFF"])
    fun `should return 404 when maintenance does not exist`(user: String) {
      val userObject = getUser(user)
      val request = HttpEntity(null, getHeaders(userObject, ::getAccessToken))
      val response = restTemplate.exchange("$baseUrl/999", HttpMethod.GET, request, Any::class.java)

      assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @ParameterizedTest(name = "should return 403 for {0}")
    @ValueSource(strings = ["CUSTOMER"])
    fun `should return 403 for other roles`(user: String) {
      val userObject = getUser(user)
      val request = HttpEntity(null, getHeaders(userObject, ::getAccessToken))
      val response = restTemplate.exchange("$baseUrl/999", HttpMethod.GET, request, Any::class.java)

      assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }
  }

  @Nested
  @DisplayName("PUT /api/v1/vehicles/{vehicleId}/maintenances/{id}")
  inner class UpdateMaintenanceTests {

    @ParameterizedTest(name = "should return 404 when maintenance does not exist for {0}")
    @ValueSource(strings = ["STAFF"])
    fun `should return 404 when maintenance does not exist`(user: String) {
      val userObject = getUser(user)
      val dto =
          MaintenanceRecordDTO(
              pastDefects = "Non-existent",
              completedMaintenance = "No maintenance",
              upcomingServiceNeeds = Date(),
              date = Date(),
              vehicleId = vehicle.id!!)
      val request = HttpEntity(dto, getHeaders(userObject, ::getAccessToken))
      val response = restTemplate.exchange("$baseUrl/999", HttpMethod.PUT, request, Any::class.java)

      assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @ParameterizedTest(name = "should return 403 for {0}")
    @ValueSource(strings = ["MANAGER", "CUSTOMER", "FLEET_MANAGER"])
    fun `should return 403 for other roles`(user: String) {
      val userObject = getUser(user)
      val dto =
          MaintenanceRecordDTO(
              pastDefects = "Non-existent",
              completedMaintenance = "No maintenance",
              upcomingServiceNeeds = Date(),
              date = Date(),
              vehicleId = vehicle.id!!)
      val request = HttpEntity(dto, getHeaders(userObject, ::getAccessToken))
      val response = restTemplate.exchange("$baseUrl/999", HttpMethod.PUT, request, Any::class.java)

      assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }
  }

  @Nested
  @DisplayName("DELETE /api/v1/vehicles/{vehicleId}/maintenances/{id}")
  inner class DeleteMaintenanceTests {

    @ParameterizedTest(name = "should return 404 when maintenance does not exist for {0}")
    @ValueSource(strings = ["STAFF"])
    fun `should return 404 when maintenance does not exist`(user: String) {
      val userObject = getUser(user)
      val request = HttpEntity(null, getHeaders(userObject, ::getAccessToken))
      val response =
          restTemplate.exchange("$baseUrl/999", HttpMethod.DELETE, request, Any::class.java)

      assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @ParameterizedTest(name = "should return 403 for {0}")
    @ValueSource(strings = ["MANAGER", "CUSTOMER", "FLEET_MANAGER"])
    fun `should return 403 for other roles`(user: String) {
      val userObject = getUser(user)
      val request = HttpEntity(null, getHeaders(userObject, ::getAccessToken))
      val response =
          restTemplate.exchange("$baseUrl/999", HttpMethod.DELETE, request, Any::class.java)

      assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }
  }
}
