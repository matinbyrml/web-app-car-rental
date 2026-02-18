package it.polito.group9.reservationservice.controllers

import TestUtils.getHeaders
import TestUtils.getUser
import it.polito.group9.reservationservice.dtos.CarModelDTO
import it.polito.group9.reservationservice.entities.*
import it.polito.group9.reservationservice.repositories.CarModelRepository
import junit.framework.TestCase.assertTrue
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
class CarModelControllerIntegrationTest : IntegrationTest() {

  @LocalServerPort private var port: Int = 0

  @Autowired private lateinit var restTemplate: TestRestTemplate

  @Autowired private lateinit var carModelRepository: CarModelRepository

  private val baseUrl: String
    get() = "http://localhost:${port}/api/v1/models"

  private val sampleModel =
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
          price = 20000.0)

  @BeforeEach
  fun setup() {
    carModelRepository.deleteAll()
  }

  @Nested
  @DisplayName("GET /api/v1/models")
  inner class GetAllCarModelsTests {
    @Test
    fun `should return empty list initially`() {
      val response = restTemplate.getForEntity(baseUrl, Map::class.java)

      assertEquals(HttpStatus.OK, response.statusCode)
      val content = response.body?.get("content") as List<*>
      Assertions.assertTrue(content.isEmpty())
    }

    @Test
    fun `should return list of car models`() {
      val savedModel = carModelRepository.save(sampleModel)

      val response = restTemplate.getForEntity(baseUrl, Map::class.java)

      assertEquals(HttpStatus.OK, response.statusCode)
      val body = response.body as Map<*, *>
      assertEquals(1, body["totalElements"])
      val content = body["content"] as List<*>
      assertEquals(savedModel.brand, (content[0] as Map<*, *>)["brand"])
    }
  }

  @Nested
  @DisplayName("GET /api/v1/models/{id}")
  inner class GetCarModelByIdTests {
    @Test
    fun `should return car model when id exists`() {
      val savedModel = carModelRepository.save(sampleModel)

      val response = restTemplate.getForEntity("$baseUrl/${savedModel.id}", CarModelDTO::class.java)

      assertEquals(HttpStatus.OK, response.statusCode)
      assertEquals(savedModel.id, response.body?.id)
      assertEquals(savedModel.brand, response.body?.brand)
    }

    @Test
    fun `should return 404 when id does not exist`() {
      val response = restTemplate.getForEntity("$baseUrl/999", Any::class.java)

      assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }
  }

  @Nested
  @DisplayName("POST /api/v1/models")
  inner class CreateCarModelTests {
    @ParameterizedTest(name = "should return 403 for {0}")
    @ValueSource(strings = ["CUSTOMER"])
    fun `should return 403 for other roles`(user: String) {
      val userObject = getUser(user)
      val dto = sampleModel.toDTO()
      val response =
          restTemplate.postForEntity(
              baseUrl,
              HttpEntity(dto, getHeaders(userObject, ::getAccessToken)),
              CarModelDTO::class.java)

      assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }

    @ParameterizedTest(name = "should create and return new car model for {0}")
    @ValueSource(strings = ["MANAGER", "FLEET_MANAGER", "STAFF"])
    fun `should create and return new car model`(user: String) {
      val userObject = getUser(user)
      val dto = sampleModel.toDTO()
      val response =
          restTemplate.postForEntity(
              baseUrl,
              HttpEntity(dto, getHeaders(userObject, ::getAccessToken)),
              CarModelDTO::class.java)

      assertEquals(HttpStatus.CREATED, response.statusCode)
      val createdId = response.body?.id
      assertTrue(
          response.headers["Location"]?.get(0)?.contains("/api/v1/models/$createdId") == true)
      assertEquals(dto.brand, response.body?.brand)
    }
  }

  @Nested
  @DisplayName("PUT /api/v1/models/{id}")
  inner class UpdateCarModelTests {
    @ParameterizedTest(name = "should update existing car model for {0}")
    @ValueSource(strings = ["MANAGER", "FLEET_MANAGER", "STAFF"])
    fun `should update existing car model`(user: String) {
      val userObject = getUser(user)
      val savedModel = carModelRepository.save(sampleModel)
      val updatedDto = savedModel.toDTO().copy(brand = "Honda")

      val request = HttpEntity(updatedDto, getHeaders(userObject, ::getAccessToken))

      val response =
          restTemplate.exchange(
              "$baseUrl/${savedModel.id}", HttpMethod.PUT, request, CarModelDTO::class.java)

      assertEquals(HttpStatus.OK, response.statusCode)
      assertEquals("Honda", response.body?.brand)
      assertEquals(savedModel.id, response.body?.id)
    }

    @ParameterizedTest(name = "should return 404 when id does not exist for {0}")
    @ValueSource(strings = ["MANAGER", "FLEET_MANAGER", "STAFF"])
    fun `should return 404 when id does not exist`(user: String) {
      val userObject = getUser(user)
      val response =
          restTemplate.exchange(
              "$baseUrl/999",
              HttpMethod.PUT,
              HttpEntity(sampleModel.toDTO(), getHeaders(userObject, ::getAccessToken)),
              Any::class.java)

      assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @ParameterizedTest(name = "should return 403 for {0}")
    @ValueSource(strings = ["CUSTOMER"])
    fun `should return 403 for other roles`(user: String) {
      val userObject = getUser(user)
      val savedModel = carModelRepository.save(sampleModel)
      val updatedDto = savedModel.toDTO().copy(brand = "Honda")

      val request = HttpEntity(updatedDto, getHeaders(userObject, ::getAccessToken))

      val response =
          restTemplate.exchange(
              "$baseUrl/${savedModel.id}", HttpMethod.PUT, request, CarModelDTO::class.java)

      assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }
  }

  @Nested
  @DisplayName("DELETE /api/v1/models/{id}")
  inner class DeleteCarModelTests {
    @ParameterizedTest(name = "should delete existing car model for {0}")
    @ValueSource(strings = ["MANAGER", "FLEET_MANAGER"])
    fun `should delete existing car model`(user: String) {
      val userObject = getUser(user)
      val savedModel = carModelRepository.save(sampleModel)

      val response =
          restTemplate.exchange(
              "$baseUrl/${savedModel.id}",
              HttpMethod.DELETE,
              HttpEntity(null, getHeaders(userObject, ::getAccessToken)),
              Void::class.java)

      assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
      assertEquals(false, carModelRepository.findById(savedModel.id!!).isPresent)
    }

    @ParameterizedTest(name = "should return 404 when id does not exist for {0}")
    @ValueSource(strings = ["MANAGER", "FLEET_MANAGER"])
    fun `should return 404 when id does not exist`(user: String) {
      val userObject = getUser(user)
      val response =
          restTemplate.exchange(
              "$baseUrl/999",
              HttpMethod.DELETE,
              HttpEntity(null, getHeaders(userObject, ::getAccessToken)),
              Any::class.java)

      assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @ParameterizedTest(name = "should return 403 for {0}")
    @ValueSource(strings = ["CUSTOMER"])
    fun `should return 403 for other roles`(user: String) {
      val userObject = getUser(user)
      val savedModel = carModelRepository.save(sampleModel)

      val response =
          restTemplate.exchange(
              "$baseUrl/${savedModel.id}",
              HttpMethod.DELETE,
              HttpEntity(null, getHeaders(userObject, ::getAccessToken)),
              Void::class.java)

      assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
      assertEquals(true, carModelRepository.findById(savedModel.id!!).isPresent)
    }
  }

  private fun CarModel.toDTO() =
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
          price = price.toDouble())
}
