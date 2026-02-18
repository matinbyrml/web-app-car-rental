package it.polito.group9.cartrackingservice.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.group9.cartrackingservice.dtos.CarLocationDTO
import it.polito.group9.cartrackingservice.services.CarLocationService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(CarLocationController::class)
@ContextConfiguration(
    classes = [CarLocationController::class, CarLocationControllerTest.TestConfig::class])
@Import(CarLocationControllerTest.TestConfig::class)
@AutoConfigureMockMvc(addFilters = true)
class CarLocationControllerTest {
  @Autowired private lateinit var mockMvc: MockMvc
  @Autowired private lateinit var objectMapper: ObjectMapper

  private val sampleDTO =
      CarLocationDTO(
          id = 1L,
          carId = 1L,
          createdAt = "2023-10-01T12:00:00Z",
          latitude = 45.123456,
          longitude = 7.123456)

  companion object {
    val carLocationServiceMock: CarLocationService = mock()
  }

  @Configuration
  class TestConfig {
    @Bean fun carLocationService(): CarLocationService = carLocationServiceMock
  }

  @AfterEach
  fun tearDown() {
    reset(carLocationServiceMock)
  }

  @Test
  @WithMockUser(roles = ["CAR"])
  fun `createCarLocation should create car location`() {
    whenever(
            carLocationServiceMock.createCarLocation(
                carId = 1L,
                createdAt = "2023-10-01T12:00:00Z",
                latitude = 45.123456,
                longitude = 7.123456))
        .thenReturn(sampleDTO)

    mockMvc
        .perform(
            post("/api/v1/carlocations/")
                .with(csrf())
                .contentType("application/json")
                .content(
                    """
        {
          "createdAt": "2023-10-01T12:00:00Z",
          "latitude": 45.123456,
          "longitude": 7.123456,
          "carId": 1
        }
        """
                        .trimIndent()))
        .andExpect(status().isCreated)
        .andExpect(jsonPath("carId").value(1L))
        .andExpect(jsonPath("createdAt").value("2023-10-01T12:00:00Z"))
        .andExpect(jsonPath("latitude").value(45.123456))
        .andExpect(jsonPath("longitude").value(7.123456))
  }

  @Test
  fun `createCarLocation should return 400 for invalid request`() {
    mockMvc
        .perform(
            post("/api/v1/carlocations/")
                .with(csrf())
                .with(jwt().jwt { it.claim("carId", 1L).subject("car-1") })
                .contentType("application/json")
                .content(
                    """
        {
          "createdAt": "THIS IS NOT A VALID DATE",
          "latitude": 45.123456,
          "longitude": 7.123456
        }
        """
                        .trimIndent()))
        .andExpect(status().isBadRequest)
  }

  @Test
  @WithMockUser(roles = ["MANAGER"])
  fun `getCarLocations should return paginated locations for car for MANAGERS`() {
    val page = PageImpl(listOf(sampleDTO), PageRequest.of(0, 10), 1)
    whenever(carLocationServiceMock.getCarLocationsByCarId(1L, 0, 10)).thenReturn(page)

    mockMvc
        .perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                    "/api/v1/carlocations/1")
                .param("page", "0")
                .param("size", "10")
                .with(csrf()))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.content[0].id").value(1L))
        .andExpect(jsonPath("$.content[0].carId").value(1L))
        .andExpect(jsonPath("$.content[0].createdAt").value("2023-10-01T12:00:00Z"))
        .andExpect(jsonPath("$.content[0].latitude").value(45.123456))
        .andExpect(jsonPath("$.content[0].longitude").value(7.123456))
  }

  @Test
  @WithMockUser(roles = ["analytics_service"])
  fun `getCarLocations should return paginated locations for car for analytics_service`() {
    val page = PageImpl(listOf(sampleDTO), PageRequest.of(0, 10), 1)
    whenever(carLocationServiceMock.getCarLocationsByCarId(1L, 0, 10)).thenReturn(page)

    mockMvc
        .perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                    "/api/v1/carlocations/1")
                .param("page", "0")
                .param("size", "10")
                .with(csrf()))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.content[0].id").value(1L))
        .andExpect(jsonPath("$.content[0].carId").value(1L))
        .andExpect(jsonPath("$.content[0].createdAt").value("2023-10-01T12:00:00Z"))
        .andExpect(jsonPath("$.content[0].latitude").value(45.123456))
        .andExpect(jsonPath("$.content[0].longitude").value(7.123456))
  }
}
