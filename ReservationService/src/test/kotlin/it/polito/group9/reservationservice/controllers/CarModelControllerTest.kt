package it.polito.group9.reservationservice.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.group9.reservationservice.dtos.CarModelDTO
import it.polito.group9.reservationservice.entities.*
import it.polito.group9.reservationservice.services.CarModelService
import kotlin.test.assertNotNull
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(CarModelController::class)
@ContextConfiguration(
    classes = [CarModelController::class, CarModelControllerTest.TestConfig::class])
@Import(CarModelControllerTest.TestConfig::class)
@AutoConfigureMockMvc(addFilters = true)
class CarModelControllerTest {

  @Autowired private lateinit var mockMvc: MockMvc

  @Autowired private lateinit var objectMapper: ObjectMapper

  companion object {
    val carModelServiceMock: CarModelService = mock()
  }

  @Configuration
  class TestConfig {
    @Bean fun carModelService(): CarModelService = carModelServiceMock
  }

  private val sampleDTO =
      CarModelDTO(
          id = 1L,
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

  @AfterEach
  fun tearDown() {
    reset(carModelServiceMock)
  }

  @Test
  @WithMockUser(roles = ["CUSTOMER"])
  fun `should return list of car models`() {
    val page = PageImpl(listOf(sampleDTO), PageRequest.of(0, 10), 1)
    whenever(
            carModelServiceMock.getCarModelsWithFilters(
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                any(),
                any()))
        .thenReturn(page)

    mockMvc
        .perform(get("/api/v1/models/").contentType(MediaType.APPLICATION_JSON).with(csrf()))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.content").isArray)
        .andExpect(jsonPath("$.content[0].brand").value("Toyota"))
  }

  @Test
  @WithMockUser(roles = ["CUSTOMER"])
  fun `should return car model details`() {
    val carModelId = 1L
    whenever(carModelServiceMock.getCarModelById(carModelId)).thenReturn(sampleDTO)

    mockMvc
        .perform(
            get("/api/v1/models/{id}/", carModelId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.id").value(carModelId))
        .andExpect(jsonPath("$.brand").value("Toyota"))
  }

  @Test
  @WithMockUser(roles = ["STAFF"])
  fun `should create a new car model`() {
    whenever(carModelServiceMock.createCarModel(any())).thenReturn(sampleDTO)

    mockMvc
        .perform(
            post("/api/v1/models/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleDTO))
                .with(csrf()))
        .andExpect(status().isCreated)
        .andExpect(header().string("Location", "/api/v1/models/1"))
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.brand").value("Toyota"))
  }

  @Test
  @WithMockUser(roles = ["STAFF"])
  fun `should update an existing car model`() {
    val carModelId = 1L
    whenever(carModelServiceMock.updateCarModel(any())).thenReturn(sampleDTO)

    mockMvc
        .perform(
            put("/api/v1/models/{id}/", carModelId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleDTO))
                .with(csrf()))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.id").value(carModelId))
        .andExpect(jsonPath("$.brand").value("Toyota"))
  }

  @Test
  @WithMockUser(roles = ["STAFF"])
  fun `should delete a car model`() {
    val carModelId = 1L
    doNothing().whenever(carModelServiceMock).deleteCarModel(carModelId)

    mockMvc
        .perform(
            delete("/api/v1/models/{id}/", carModelId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
        .andExpect(status().isNoContent)
  }

  @Test
  fun `getCarModelService should not be null`() {
    assertNotNull(carModelServiceMock)
  }
}
