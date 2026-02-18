package it.polito.group9.cartrackingservice.controllers

import CAR
import MANAGER
import TestUtils.getHeaders
import it.polito.group9.cartrackingservice.entities.CarLocation
import it.polito.group9.cartrackingservice.repositories.CarLocationRepository
import kotlin.test.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CarLocationControllerIntegrationTest : IntegrationTest() {
  @LocalServerPort private var port: Int = 0

  @Autowired private lateinit var restTemplate: TestRestTemplate

  @Autowired private lateinit var carLocationRepository: CarLocationRepository

  private val baseUrl: String
    get() = "http://localhost:${port}/api/v1/carlocations"

  private val sampleLocation =
      CarLocation(
          carId = 1L,
          latitude = 45.123456,
          longitude = 7.123456,
          createdAt = "2023-10-01T12:00:00Z")

  @BeforeEach
  fun setup() {
    carLocationRepository.deleteAll()
  }

  @Nested
  @DisplayName("POST /api/v1/carlocations")
  inner class PostCarLocationTests {
    @Test
    fun `should create a new car location and return 201 Created`() {
      val sampleLocation =
          mapOf(
              "carId" to 1L,
              "latitude" to 45.123456,
              "longitude" to 7.123456,
              "createdAt" to "2023-10-01T12:00:00Z",
              "carId" to 1L)

      val request = HttpEntity(sampleLocation, getHeaders(CAR, ::getAccessToken))

      val response = restTemplate.postForEntity(baseUrl, request, Map::class.java)

      assertEquals(HttpStatus.CREATED, response.statusCode)
    }
  }

  @Nested
  @DisplayName("GET /api/v1/carlocations")
  inner class GetCarLocationsTests {
    @Test
    fun `should create a new car location and return 200`() {
      val savedModel = carLocationRepository.save(sampleLocation)
      val request = HttpEntity(sampleLocation, getHeaders(MANAGER, ::getAccessToken))

      val response = restTemplate.exchange("$baseUrl/1", HttpMethod.GET, request, Map::class.java)

      assertEquals(HttpStatus.OK, response.statusCode)
      val body = response.body as Map<*, *>
      assertEquals(1, body["totalElements"])
      val content = body["content"] as List<*>
      assertEquals(savedModel.latitude, (content[0] as Map<*, *>)["latitude"])
      assertEquals(savedModel.longitude, (content[0] as Map<*, *>)["longitude"])
      assertEquals(savedModel.createdAt, (content[0] as Map<*, *>)["createdAt"])
      assertEquals(savedModel.carId.toInt(), (content[0] as Map<*, *>)["carId"])
    }
  }
}
