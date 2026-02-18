package it.polito.group9.reservationservice.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.group9.reservationservice.dtos.CarModelDTO
import it.polito.group9.reservationservice.dtos.VehicleDTO
import it.polito.group9.reservationservice.entities.*
import it.polito.group9.reservationservice.services.CarModelService
import it.polito.group9.reservationservice.services.VehicleService
import kotlin.test.Test
import org.junit.jupiter.api.AfterEach
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
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

@WebMvcTest(VehicleController::class)
@ContextConfiguration(classes = [VehicleController::class, VehicleControllerTest.TestConfig::class])
@Import(VehicleControllerTest.TestConfig::class)
class VehicleControllerTest {
  @Autowired private lateinit var mockMvc: MockMvc

  @Autowired private lateinit var objectMapper: ObjectMapper

  companion object {
    val vehicleServiceMock: VehicleService = mock()
    val carModelServiceMock: CarModelService = mock()
  }

  @Configuration
  class TestConfig {
    @Bean fun vehicleService(): VehicleService = vehicleServiceMock

    @Bean fun carModelService(): CarModelService = carModelServiceMock
  }

  private val carModelDTO =
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

  private val sampleDTO =
      VehicleDTO(
          id = 1L,
          licensePlate = "ABC123",
          vin = "1HGCM82633A123456",
          availability = "AVAILABLE",
          km = 10000,
          pendingCleaning = false,
          pendingRepair = false,
          maintenanceRecordHistory = emptyList(),
          vehicleModelId = carModelDTO.id!!)

  @AfterEach
  fun tearDown() {
    reset(vehicleServiceMock)
  }

  @Test
  @WithMockUser(roles = ["CUSTOMER"])
  fun `should return paginated list of vehicles when filters are applied`() {
    val paginatedVehicles = PageImpl(listOf(sampleDTO), PageRequest.of(0, 10), 1)
    whenever(
            vehicleServiceMock.getVehiclesWithFilters(
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                any(),
                any()))
        .thenReturn(paginatedVehicles)

    mockMvc
        .perform(get("/api/v1/vehicles/").contentType(MediaType.APPLICATION_JSON).with(csrf()))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.content[0].licensePlate").value("ABC123"))
  }

  @Test
  @WithMockUser(roles = ["CUSTOMER"])
  fun `should return vehicle details when vehicle exists`() {
    val vehicleId = 1L
    whenever(vehicleServiceMock.getVehicleById(vehicleId)).thenReturn(sampleDTO)

    mockMvc
        .perform(
            get("/api/v1/vehicles/{id}/", vehicleId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.licensePlate").value("ABC123"))
  }

  @Test
  @WithMockUser(roles = ["MANAGER"])
  fun `should create a new vehicle and return 201 status`() {
    whenever(vehicleServiceMock.createVehicle(eq(sampleDTO), eq(carModelDTO.toEntity())))
        .thenReturn(sampleDTO)
    whenever(carModelServiceMock.getCarModelById(eq(carModelDTO.id!!))).thenReturn(carModelDTO)

    mockMvc
        .perform(
            post("/api/v1/vehicles/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleDTO))
                .with(csrf()))
        .andExpect(status().isCreated)
        .andExpect(header().string("Location", "/api/v1/vehicles/1"))
        .andExpect(jsonPath("$.licensePlate").value("ABC123"))
  }

  @Test
  @WithMockUser(roles = ["MANAGER"])
  fun `should update an existing vehicle and return updated details`() {
    val vehicleId = 1L
    whenever(vehicleServiceMock.updateVehicle(eq(sampleDTO), eq(carModelDTO.toEntity())))
        .thenReturn(sampleDTO)
    whenever(carModelServiceMock.getCarModelById(eq(carModelDTO.id!!))).thenReturn(carModelDTO)

    mockMvc
        .perform(
            put("/api/v1/vehicles/{id}/", vehicleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleDTO))
                .with(csrf()))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.licensePlate").value("ABC123"))
  }

  @Test
  @WithMockUser(roles = ["MANAGER"])
  fun `should delete a vehicle and return 204 status`() {
    val vehicleId = 1L

    mockMvc
        .perform(
            delete("/api/v1/vehicles/{id}/", vehicleId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
        .andExpect(status().isNoContent)
  }
}
