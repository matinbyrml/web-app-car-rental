package it.polito.group9.reservationservice.controllers

import TestUtils.getHeaders
import TestUtils.getUser
import it.polito.group9.reservationservice.dtos.VehicleDTO
import it.polito.group9.reservationservice.entities.*
import it.polito.group9.reservationservice.repositories.CarModelRepository
import it.polito.group9.reservationservice.repositories.VehicleRepository
import java.time.LocalDate
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
class VehicleControllerIntegrationTest : IntegrationTest() {

  @LocalServerPort private var localPort: Int = 8081

  @Autowired private lateinit var restTemplate: TestRestTemplate

  @Autowired private lateinit var vehicleRepository: VehicleRepository

  @Autowired private lateinit var carModelRepository: CarModelRepository

  private lateinit var baseUrl: String
  private lateinit var carModel: CarModel

  @BeforeEach
  fun setup() {
    vehicleRepository.deleteAll()
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
                luggage = 3,
                category = "Sedan",
                engineType = EngineType.PETROL,
                transmissionType = TransmissionType.AUTOMATIC,
                drivetrain = Drivetrain.FWD,
                motorDisplacement = 1800,
                airConditioning = true,
                infotainmentSystem = InfotainmentSystem.BLUETOOTH,
                safetyFeatures = "ABS, Airbags, Lane Assist, Emergency Braking",
                price = 22000.00))

    baseUrl = "http://localhost:${localPort}/api/v1/vehicles"
  }

  @Nested
  @DisplayName("GET /api/v1/vehicles")
  inner class GetVehiclesTests {
    @Test
    fun `should return empty list when no vehicles exist`() {
      val response = restTemplate.getForEntity(baseUrl, Any::class.java)

      assertEquals(HttpStatus.OK, response.statusCode)
      val body = response.body as Map<*, *>
      assertEquals(0, body["totalElements"])
    }

    @Test
    fun `should return list of vehicles`() {
      val vehicle =
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

      val response = restTemplate.getForEntity(baseUrl, Any::class.java)

      assertEquals(HttpStatus.OK, response.statusCode)
      val body = response.body as Map<*, *>
      assertEquals(1, body["totalElements"])
      val content = body["content"] as List<*>
      assertEquals(vehicle.licensePlate, (content[0] as Map<*, *>)["licensePlate"])
    }
  }

  @Nested
  @DisplayName("GET /api/v1/vehicles/{id}")
  inner class GetVehicleByIdTests {
    @Test
    fun `should return vehicle when it exists`() {
      val vehicle =
          vehicleRepository.save(
              Vehicle(
                  licensePlate = "ABC123",
                  vin = "1HGCM82633A123456",
                  availability = AvailabilityStatus.AVAILABLE,
                  km = 15000,
                  pendingCleaning = false,
                  pendingRepair = false,
                  maintenanceRecordHistory = emptyList(),
                  vehicleModel = carModel))

      val response = restTemplate.getForEntity("$baseUrl/${vehicle.id}", VehicleDTO::class.java)

      assertEquals(HttpStatus.OK, response.statusCode)
      assertEquals(vehicle.id, response.body?.id)
      assertEquals("ABC123", response.body?.licensePlate)
    }

    @Test
    fun `should return 404 when vehicle does not exist`() {
      val response = restTemplate.getForEntity("$baseUrl/999", Any::class.java)

      assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }
  }

  @Nested
  @DisplayName("POST /api/v1/vehicles")
  inner class CreateVehicleTests {

    @ParameterizedTest(name = "should return 404 when car model not found for {0}")
    @ValueSource(strings = ["MANAGER", "FLEET_MANAGER"])
    fun `should return 404 when car model not found`(user: String) {
      val userObject = getUser(user)
      val dto =
          Vehicle(
              licensePlate = "NEW123Y",
              vin = "1HGCM82633A123456",
              availability = AvailabilityStatus.AVAILABLE,
              km = 15000,
              pendingCleaning = false,
              pendingRepair = false,
              maintenanceRecordHistory = emptyList(),
              vehicleModel = carModel)
      val request = HttpEntity(dto, getHeaders(userObject, ::getAccessToken))
      val response = restTemplate.postForEntity(baseUrl, request, Any::class.java)

      assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @ParameterizedTest(name = "should return 403 for other roles for {0}")
    @ValueSource(strings = ["STAFF", "CUSTOMER"])
    fun `should return 403 for other roles`(user: String) {
      val userObject = getUser(user)
      val dto =
          Vehicle(
              licensePlate = "NEW123Y",
              vin = "1HGCM82633A123456",
              availability = AvailabilityStatus.AVAILABLE,
              km = 15000,
              pendingCleaning = false,
              pendingRepair = false,
              maintenanceRecordHistory = emptyList(),
              vehicleModel = carModel)
      val request = HttpEntity(dto, getHeaders(userObject, ::getAccessToken))
      val response = restTemplate.postForEntity(baseUrl, request, Any::class.java)

      assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }
  }

  @Nested
  @DisplayName("DELETE /api/v1/vehicles/{id}")
  inner class DeleteVehicleTests {
    @ParameterizedTest(name = "should delete existing vehicle for {0}")
    @ValueSource(strings = ["MANAGER", "FLEET_MANAGER"])
    fun `should delete existing vehicle`(user: String) {
      val userObject = getUser(user)
      val vehicle =
          vehicleRepository.save(
              Vehicle(
                  licensePlate = "DEL123",
                  vin = "1HGCM82633A123456",
                  availability = AvailabilityStatus.AVAILABLE,
                  km = 15000,
                  pendingCleaning = false,
                  pendingRepair = false,
                  maintenanceRecordHistory = emptyList(),
                  vehicleModel = carModel))
      val request = HttpEntity(null, getHeaders(userObject, ::getAccessToken))
      val response =
          restTemplate.exchange(
              "$baseUrl/${vehicle.id}", HttpMethod.DELETE, request, Void::class.java)

      assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
      assertEquals(false, vehicleRepository.findById(vehicle.id!!).isPresent)
    }

    @ParameterizedTest(name = "should return 403 for {0}")
    @ValueSource(strings = ["STAFF", "CUSTOMER"])
    fun `should return 403 for other roles`(user: String) {
      val userObject = getUser(user)
      val vehicle =
          vehicleRepository.save(
              Vehicle(
                  licensePlate = "DEL123",
                  vin = "1HGCM82633A123456",
                  availability = AvailabilityStatus.AVAILABLE,
                  km = 15000,
                  pendingCleaning = false,
                  pendingRepair = false,
                  maintenanceRecordHistory = emptyList(),
                  vehicleModel = carModel))
      val request = HttpEntity(null, getHeaders(userObject, ::getAccessToken))
      val response =
          restTemplate.exchange(
              "$baseUrl/${vehicle.id}", HttpMethod.DELETE, request, Void::class.java)

      assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
      assertEquals(true, vehicleRepository.findById(vehicle.id!!).isPresent)
    }
  }

  @Nested
  @DisplayName("GET /api/v1/vehicles/available")
  inner class GetAvailableVehiclesTests {
    @Test
    fun `should return available vehicles`() {
      val vehicle =
          vehicleRepository.save(
              Vehicle(
                  licensePlate = "AVAIL123",
                  vin = "1HGCM82633A123456",
                  availability = AvailabilityStatus.AVAILABLE,
                  km = 15000,
                  pendingCleaning = false,
                  pendingRepair = false,
                  maintenanceRecordHistory = emptyList(),
                  vehicleModel = carModel))

      val startDate = LocalDate.now().plusDays(1)
      val endDate = LocalDate.now().plusDays(5)

      val response =
          restTemplate.getForEntity(
              "$baseUrl/available?startDate=$startDate&endDate=$endDate", Any::class.java)

      assertEquals(HttpStatus.OK, response.statusCode)
      val body = response.body as Map<*, *>
      assertEquals(true, (body["content"] as List<*>).isNotEmpty())
    }
  }
}
