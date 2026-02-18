package it.polito.group9.reservationservice.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.group9.reservationservice.dtos.ReservationDTO
import it.polito.group9.reservationservice.entities.AvailabilityStatus
import it.polito.group9.reservationservice.entities.CarModel
import it.polito.group9.reservationservice.entities.CarSegment
import it.polito.group9.reservationservice.entities.Drivetrain
import it.polito.group9.reservationservice.entities.EngineType
import it.polito.group9.reservationservice.entities.InfotainmentSystem
import it.polito.group9.reservationservice.entities.ReservationStatus
import it.polito.group9.reservationservice.entities.TransmissionType
import it.polito.group9.reservationservice.entities.Vehicle
import it.polito.group9.reservationservice.exceptions.ReservationNotFoundException
import it.polito.group9.reservationservice.exceptions.VehicleNotAvailableException
import it.polito.group9.reservationservice.exceptions.VehicleNotFoundException
import it.polito.group9.reservationservice.handlers.ReservationExceptionHandler
import it.polito.group9.reservationservice.handlers.UserExceptionHandler
import it.polito.group9.reservationservice.handlers.VehicleExceptionHandler
import it.polito.group9.reservationservice.services.CarModelService
import it.polito.group9.reservationservice.services.KeycloakService
import it.polito.group9.reservationservice.services.ReservationService
import it.polito.group9.reservationservice.services.VehicleService
import java.time.LocalDateTime
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.client.RestTemplate

@WebMvcTest(ReservationController::class)
@ContextConfiguration(
    classes =
        [
            ReservationController::class,
            ReservationControllerTest.TestConfig::class,
            UserExceptionHandler::class,
            VehicleExceptionHandler::class,
            ReservationExceptionHandler::class])
@Import(ReservationControllerTest.TestConfig::class)
class ReservationControllerTest {
  @Autowired private lateinit var mockMvc: MockMvc

  @Autowired private lateinit var objectMapper: ObjectMapper

  companion object {
    val reservationServiceMock: ReservationService = mock()
    val vehicleServiceMock: VehicleService = mock()
    val carModelServiceMock: CarModelService = mock()
    val restTemplateMock: RestTemplate = mock()
    val keycloakServiceMock: KeycloakService = mock()
  }

  @Configuration
  class TestConfig {
    @Bean fun reservationService(): ReservationService = reservationServiceMock

    @Bean fun vehicleService(): VehicleService = vehicleServiceMock

    @Bean fun restTemplate(): RestTemplate = restTemplateMock

    @Bean fun carModelService(): CarModelService = carModelServiceMock

    @Bean fun keycloakService(): KeycloakService = keycloakServiceMock
  }

  val sampleDTO =
      ReservationDTO(
          id = 1L,
          vehicleId = 1L,
          userId = 1L,
          startDate = LocalDateTime.now().toString(),
          endDate = LocalDateTime.now().toString(),
      )

  val sampleCarModel =
      CarModel(
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
    reset(reservationServiceMock)
    reset(vehicleServiceMock)
    reset(restTemplateMock)
  }

  @Test
  @WithMockUser(roles = ["STAFF"])
  fun `should return 404 when reservation is not found`() {
    val reservationId = 999L
    whenever(reservationServiceMock.getReservationById(reservationId))
        .thenThrow(ReservationNotFoundException("Reservation with id $reservationId not found"))

    mockMvc
        .perform(get("/api/v1/reservations/{id}/", reservationId).with(csrf()))
        .andExpect(status().isNotFound)
  }

  @Test
  @WithMockUser(roles = ["CUSTOMER"])
  fun `should return 200 when reservation is found`() {
    val reservationId = 999L
    whenever(reservationServiceMock.getReservationById(reservationId)).thenReturn(sampleDTO)

    mockMvc
        .perform(get("/api/v1/reservations/{id}", reservationId).with(csrf()))
        .andExpect(status().isOk)
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(sampleDTO)))
  }

  @Test
  @WithMockUser(roles = ["CUSTOMER"])
  fun `should return 403 when user is not eligible to create a reservation`() {
    val vehicleId = 1L
    val responseEntity = ResponseEntity(mapOf("isEligible" to false), HttpStatus.OK)
    `when`(
            restTemplateMock.exchange(
                eq("http://localhost:8081/api/v1/customers/${sampleDTO.userId}/eligibility"),
                eq(HttpMethod.GET),
                any<HttpEntity<String>>(),
                any<ParameterizedTypeReference<Map<String, Boolean>>>()))
        .thenReturn(responseEntity)

    mockMvc
        .perform(
            post("/api/v1/reservations/")
                .param("vehicleId", vehicleId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleDTO))
                .with(csrf()))
        .andExpect(status().isForbidden)
  }

  @Test
  @WithMockUser(roles = ["CUSTOMER"])
  fun `should return 404 when vehicle is not found during reservation creation`() {
    val vehicleId = 999L
    val responseEntity = ResponseEntity(mapOf("isEligible" to true), HttpStatus.OK)
    `when`(
            restTemplateMock.exchange(
                eq("http://localhost:8081/api/v1/customers/${sampleDTO.userId}/eligibility"),
                eq(HttpMethod.GET),
                any<HttpEntity<String>>(),
                any<ParameterizedTypeReference<Map<String, Boolean>>>()))
        .thenReturn(responseEntity)
    whenever(vehicleServiceMock.getVehicleEntityById(vehicleId))
        .thenThrow(VehicleNotFoundException("Vehicle with id $vehicleId not found"))

    mockMvc
        .perform(
            post("/api/v1/reservations/")
                .param("vehicleId", vehicleId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleDTO))
                .with(csrf()))
        .andExpect(status().isNotFound)
  }

  /*
  @Test
  @WithMockUser(roles = ["CUSTOMER"])
  fun `should return 200 when reservation is created`() {
    val vehicleId = 999L
    val responseEntity = ResponseEntity(mapOf("isEligible" to true), HttpStatus.OK)
    `when`(
            restTemplateMock.exchange(
                eq("http://localhost:8081/api/v1/customers/${sampleDTO.userId}/eligibility"),
                eq(HttpMethod.GET),
                any<HttpEntity<String>>(),
                any<ParameterizedTypeReference<Map<String, Boolean>>>()))
        .thenReturn(responseEntity)
    whenever(vehicleServiceMock.getVehicleEntityById(vehicleId))
        .thenReturn(
            Vehicle(
                vehicleId,
                "ABC123",
                "123455",
                AvailabilityStatus.AVAILABLE,
                250000,
                false,
                false,
                emptyList(),
                sampleCarModel))
    whenever(reservationServiceMock.createReservation(any(), any())).thenReturn(sampleDTO)
    whenever(carModelServiceMock.getCarModelById(any())).thenReturn(sampleCarModel.toDTO())
    whenever(
            restTemplateMock.postForEntity(
                eq("http://localhost:8082/api/v1/orders"),
                any<HttpEntity<CreateOrderRequestDTO>>(),
                eq(CreateOrderResponseDTO::class.java)))
        .thenReturn(ResponseEntity(sampleCreateOrderResponseDTO, HttpStatus.CREATED))

    mockMvc
        .perform(
            post("/api/v1/reservations/")
                .param("vehicleId", vehicleId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleDTO))
                .with(csrf()))
        .andExpect(status().isCreated)
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
    // .andExpect(content().json(objectMapper.writeValueAsString(sampleDTO)))
  }
  */

  @Test
  @WithMockUser(roles = ["CUSTOMER"])
  fun `should return 409 when vehicle is not available for reservation`() {
    val vehicleId = 1L
    val responseEntity = ResponseEntity(mapOf("isEligible" to true), HttpStatus.OK)
    `when`(
            restTemplateMock.exchange(
                eq("http://localhost:8081/api/v1/customers/${sampleDTO.userId}/eligibility"),
                eq(HttpMethod.GET),
                any<HttpEntity<String>>(),
                any<ParameterizedTypeReference<Map<String, Boolean>>>()))
        .thenReturn(responseEntity)
    whenever(vehicleServiceMock.getVehicleEntityById(vehicleId))
        .thenThrow(VehicleNotAvailableException("Vehicle with id $vehicleId is not available"))

    mockMvc
        .perform(
            post("/api/v1/reservations/")
                .param("vehicleId", vehicleId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleDTO))
                .with(csrf()))
        .andExpect(status().isConflict)
  }

  @Test
  @WithMockUser(roles = ["CUSTOMER"])
  fun `should return 409 when vehicle is pending cleaning`() {
    val vehicleId = 1L
    val responseEntity = ResponseEntity(mapOf("isEligible" to true), HttpStatus.OK)
    `when`(
            restTemplateMock.exchange(
                eq("http://localhost:8081/api/v1/customers/${sampleDTO.userId}/eligibility"),
                eq(HttpMethod.GET),
                any<HttpEntity<String>>(),
                any<ParameterizedTypeReference<Map<String, Boolean>>>()))
        .thenReturn(responseEntity)
    whenever(vehicleServiceMock.getVehicleEntityById(vehicleId))
        .thenReturn(
            Vehicle(
                vehicleId,
                "ABC123",
                "123455",
                AvailabilityStatus.AVAILABLE,
                250000,
                true,
                true,
                emptyList()))

    mockMvc
        .perform(
            post("/api/v1/reservations/")
                .param("vehicleId", vehicleId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleDTO))
                .with(csrf()))
        .andExpect(status().isConflict)
  }

  @Test
  @WithMockUser(roles = ["CUSTOMER"])
  fun `should return 404 when reservation to update is not found`() {
    val reservationId = 999L
    val vehicle =
        Vehicle(
            sampleDTO.vehicleId,
            "ABC123",
            "123455",
            AvailabilityStatus.AVAILABLE,
            250000,
            true,
            true,
            emptyList())
    whenever(vehicleServiceMock.getVehicleEntityById(sampleDTO.vehicleId)).thenReturn(vehicle)
    whenever(
            reservationServiceMock.updateReservationByStatus(
                eq(reservationId), eq(ReservationStatus.CANCELLED)))
        .thenThrow(ReservationNotFoundException("Reservation with id $reservationId not found"))

    mockMvc
        .perform(
            put("/api/v1/reservations/{id}/", reservationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleDTO))
                .with(csrf()))
        .andExpect(status().isNotFound)
  }

  @Test
  @WithMockUser(roles = ["CUSTOMER"])
  fun `should return 204 when reservation is updated`() {
    val reservationId = 999L
    val vehicle =
        Vehicle(
            sampleDTO.vehicleId,
            "ABC123",
            "123455",
            AvailabilityStatus.AVAILABLE,
            250000,
            true,
            true,
            emptyList())
    whenever(vehicleServiceMock.getVehicleEntityById(sampleDTO.vehicleId)).thenReturn(vehicle)
    whenever(
            reservationServiceMock.updateReservationByStatus(
                eq(reservationId), eq(ReservationStatus.CANCELLED)))
        .thenReturn(sampleDTO)

    mockMvc
        .perform(
            put("/api/v1/reservations/{id}/", reservationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleDTO))
                .with(csrf()))
        .andExpect(status().isOk)
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(sampleDTO)))
  }

  @Test
  @WithMockUser(roles = ["CUSTOMER"])
  fun `should return 404 when vehicle is not found during reservation update`() {
    val reservationId = 999L
    whenever(vehicleServiceMock.getVehicleEntityById(sampleDTO.vehicleId))
        .thenThrow(VehicleNotFoundException("Vehicle with id ${sampleDTO.vehicleId} not found"))

    mockMvc
        .perform(
            put("/api/v1/reservations/{id}/", reservationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleDTO))
                .with(csrf()))
        .andExpect(status().isNotFound)
  }

  @Test
  @WithMockUser(roles = ["CUSTOMER"])
  fun `should return 200 when reservation is cancelled`() {
    val reservationId = 999L
    whenever(
            reservationServiceMock.updateReservationByStatus(
                eq(reservationId), eq(ReservationStatus.CANCELLED)))
        .thenReturn(sampleDTO)

    mockMvc
        .perform(delete("/api/v1/reservations/{id}/", reservationId).with(csrf()))
        .andExpect(status().isNoContent)
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(sampleDTO)))
  }

  @Test
  @WithMockUser(roles = ["CUSTOMER"])
  fun `should return 404 when reservation is not found during reservation cancellation`() {
    val reservationId = 999L
    whenever(
            reservationServiceMock.updateReservationByStatus(
                eq(reservationId), eq(ReservationStatus.CANCELLED)))
        .thenThrow(ReservationNotFoundException("Reservation with id $reservationId not found"))

    mockMvc
        .perform(delete("/api/v1/reservations/{id}/", reservationId).with(csrf()))
        .andExpect(status().isNotFound)
  }

  @Test
  @WithMockUser(roles = ["STAFF"])
  fun `should return 200 when reservation is picked up`() {
    val reservationId = 999L
    whenever(
            reservationServiceMock.updateReservationByStatus(
                eq(reservationId), eq(ReservationStatus.PICKED_UP)))
        .thenReturn(sampleDTO)

    mockMvc
        .perform(put("/api/v1/reservations/{id}/pickup", reservationId).with(csrf()))
        .andExpect(status().isOk)
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(sampleDTO)))
  }

  @Test
  @WithMockUser(roles = ["STAFF"])
  fun `should return 404 when reservation is not found during reservation pickup`() {
    val reservationId = 999L
    whenever(
            reservationServiceMock.updateReservationByStatus(
                eq(reservationId), eq(ReservationStatus.PICKED_UP)))
        .thenThrow(ReservationNotFoundException("Reservation with id $reservationId not found"))

    mockMvc
        .perform(put("/api/v1/reservations/{id}/pickup", reservationId).with(csrf()))
        .andExpect(status().isNotFound)
  }

  @Test
  @WithMockUser(roles = ["STAFF"])
  fun `should return 200 when reservation is returned`() {
    val reservationId = 999L
    whenever(
            reservationServiceMock.updateReservationByStatus(
                eq(reservationId), eq(ReservationStatus.RETURNED)))
        .thenReturn(sampleDTO)

    mockMvc
        .perform(put("/api/v1/reservations/{id}/return", reservationId).with(csrf()))
        .andExpect(status().isOk)
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(content().json(objectMapper.writeValueAsString(sampleDTO)))
  }

  @Test
  @WithMockUser(roles = ["STAFF"])
  fun `should return 404 when reservation is not found during reservation return`() {
    val reservationId = 999L
    whenever(
            reservationServiceMock.updateReservationByStatus(
                eq(reservationId), eq(ReservationStatus.RETURNED)))
        .thenThrow(ReservationNotFoundException("Reservation with id $reservationId not found"))

    mockMvc
        .perform(put("/api/v1/reservations/{id}/return", reservationId).with(csrf()))
        .andExpect(status().isNotFound)
  }
}
