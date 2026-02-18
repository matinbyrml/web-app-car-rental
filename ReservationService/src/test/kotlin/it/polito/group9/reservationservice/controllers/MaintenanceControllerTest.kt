package it.polito.group9.reservationservice.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.group9.reservationservice.dtos.MaintenanceRecordDTO
import it.polito.group9.reservationservice.services.MaintenanceService
import it.polito.group9.reservationservice.services.VehicleService
import java.util.*
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(MaintenanceController::class)
@ContextConfiguration(
    classes = [MaintenanceController::class, MaintenanceControllerTest.TestConfig::class])
@Import(MaintenanceControllerTest.TestConfig::class)
class MaintenanceControllerTest {
  @Autowired private lateinit var mockMvc: MockMvc

  @Autowired private lateinit var objectMapper: ObjectMapper

  companion object {
    val maintenanceServiceMock: MaintenanceService = mock()
    val vehicleServiceMock: VehicleService = mock()
  }

  @Configuration
  class TestConfig {
    @Bean fun maintenanceService(): MaintenanceService = maintenanceServiceMock

    @Bean fun vehicleService(): VehicleService = vehicleServiceMock
  }

  private val sampleMaintenanceDTO =
      MaintenanceRecordDTO(
          id = 1L,
          pastDefects = "Brake issues",
          completedMaintenance = "Brake pads replaced",
          upcomingServiceNeeds = Date(),
          date = Date(),
          vehicleId = 1L)

  @AfterEach
  fun tearDown() {
    reset(maintenanceServiceMock)
    reset(vehicleServiceMock)
  }

  @Test
  @WithMockUser(roles = ["STAFF"])
  fun `should return paginated list of maintenance records when filters are applied`() {
    val vehicleId = 1L
    val paginatedMaintenances = PageImpl(listOf(sampleMaintenanceDTO), PageRequest.of(0, 10), 1)
    whenever(
            maintenanceServiceMock.getMaintenancesWithFilters(
                eq(vehicleId), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), any(), any()))
        .thenReturn(paginatedMaintenances)

    mockMvc
        .perform(
            get("/api/v1/vehicles/{vehicleId}/maintenances/", vehicleId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.content[0].pastDefects").value("Brake issues"))
  }

  @Test
  @WithMockUser(roles = ["STAFF"])
  fun `should return maintenance record details when record exists`() {
    val vehicleId = 1L
    val maintenanceId = 1L
    whenever(maintenanceServiceMock.getMaintenanceById(maintenanceId))
        .thenReturn(sampleMaintenanceDTO)

    mockMvc
        .perform(
            get("/api/v1/vehicles/{vehicleId}/maintenances/{id}/", vehicleId, maintenanceId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.pastDefects").value("Brake issues"))
  }

  @Test
  @WithMockUser(roles = ["STAFF"])
  fun `should update an existing maintenance record and return updated details`() {
    val vehicleId = 1L
    val maintenanceId = 1L
    whenever(vehicleServiceMock.getVehicleEntityById(eq(sampleMaintenanceDTO.vehicleId)))
        .thenReturn(mock())
    whenever(
            maintenanceServiceMock.updateMaintenanceRecord(
                eq(sampleMaintenanceDTO.copy(id = maintenanceId)), any()))
        .thenReturn(sampleMaintenanceDTO)

    mockMvc
        .perform(
            put("/api/v1/vehicles/{vehicleId}/maintenances/{id}/", vehicleId, maintenanceId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleMaintenanceDTO))
                .with(csrf()))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.pastDefects").value("Brake issues"))
  }

  @Test
  @WithMockUser(roles = ["STAFF"])
  fun `should delete a maintenance record and return 204 status`() {
    val vehicleId = 1L
    val maintenanceId = 1L

    mockMvc
        .perform(
            delete("/api/v1/vehicles/{vehicleId}/maintenances/{id}/", vehicleId, maintenanceId)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
        .andExpect(status().isNoContent)
  }
}
