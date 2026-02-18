package it.polito.group9.reservationservice.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import it.polito.group9.reservationservice.dtos.ReservationDTO
import it.polito.group9.reservationservice.dtos.ReturnInspectionRequestDTO
import it.polito.group9.reservationservice.entities.AvailabilityStatus
import it.polito.group9.reservationservice.entities.ReservationStatus
import it.polito.group9.reservationservice.exceptions.ReservationNotFoundException
import it.polito.group9.reservationservice.exceptions.UserNotEligibleException
import it.polito.group9.reservationservice.exceptions.VehicleNotAvailableException
import it.polito.group9.reservationservice.exceptions.VehicleNotFoundException
import it.polito.group9.reservationservice.services.AuthUserService
import it.polito.group9.reservationservice.services.KeycloakService
import it.polito.group9.reservationservice.services.ReservationService
import it.polito.group9.reservationservice.services.VehicleService
import java.util.*
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.domain.Page
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException

/**
 * Controller class for managing reservation-related operations. Provides endpoints for CRUD
 * operations on reservations.
 */
@CrossOrigin(origins = ["*"])
@RestController
@RequestMapping("/api/v1/reservations/", "api/v1/reservations")
class ReservationController(
    val reservationService: ReservationService,
    val vehicleService: VehicleService,
    val restTemplate: RestTemplate,
    val keycloakService: KeycloakService,
    private val authUserService: AuthUserService
) {

  private val log = LoggerFactory.getLogger(ReservationController::class.java)

  /**
   * Retrieves a paginated list of all reservations, optionally filtered by parameters.
   *
   * @param startDate Optional start date for filtering reservations.
   * @param endDate Optional end date for filtering reservations.
   * @param userId Optional user ID for filtering reservations.
   * @param vehicleId Optional vehicle ID for filtering reservations.
   * @param page Page number for pagination (default is 0).
   * @param size Page size for pagination (default is 10).
   * @return A paginated list of reservations matching the filters.
   */
  @Operation(
      summary = "Get all reservations",
      description = "Returns a paginated list of all reservations filtered by optional parameters.",
      responses =
          [
              ApiResponse(
                  responseCode = "200",
                  description = "List of filtered reservations",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              array =
                                  ArraySchema(
                                      schema = Schema(implementation = ReservationDTO::class)))])])
  @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'FLEET_MANAGER')")
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  fun getAllReservations(
      @RequestParam(required = false) startDate: Date?,
      @RequestParam(required = false) endDate: Date?,
      @RequestParam(required = false) userId: Long?,
      @RequestParam(required = false) vehicleId: Long?,
      @RequestParam(defaultValue = "0") page: Int,
      @RequestParam(defaultValue = "10") size: Int,
  ): Page<ReservationDTO> =
      reservationService.getAllReservations(
          startDate?.toString(), endDate?.toString(), userId, vehicleId, page, size)

  @Operation(
      summary = "Get reservations for current user",
      description = "Returns a paginated list of reservations belonging to the authenticated user.")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'MANAGER', 'FLEET_MANAGER')")
  @GetMapping("me/", "me")
  @ResponseStatus(HttpStatus.OK)
  fun getMyReservations(
      @AuthenticationPrincipal jwt: Jwt,
      @RequestParam(defaultValue = "0") page: Int,
      @RequestParam(defaultValue = "10") size: Int
  ): Page<ReservationDTO> {
    val userData = authUserService.getCurrentUser(jwt)
    return reservationService.getAllReservations(
        startDate = null,
        endDate = null,
        userId = userData.id,
        vehicleId = null,
        page = page,
        size = size)
  }

  /**
   * Retrieves a reservation by its ID.
   *
   * @param id The ID of the reservation to retrieve.
   * @return The reservation with the specified ID.
   * @throws ReservationNotFoundException if the reservation is not found.
   */
  @Operation(
      summary = "Get reservation by ID",
      description = "Returns a reservation by its ID.",
      responses =
          [
              ApiResponse(
                  responseCode = "200",
                  description = "Reservation found",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema = Schema(implementation = ReservationDTO::class))]),
              ApiResponse(
                  responseCode = "404",
                  description = "Reservation not found",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema =
                                  Schema(implementation = ReservationNotFoundException::class))])])
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'MANAGER', 'FLEET_MANAGER')")
  @GetMapping("{id}/", "{id}")
  @ResponseStatus(HttpStatus.OK)
  fun getReservationById(
      @PathVariable id: Long,
      @AuthenticationPrincipal jwt: Jwt
  ): ReservationDTO {
    val dto = reservationService.getReservationById(id)
    val user = authUserService.getCurrentUser(jwt)

    if (dto.userId != user.id && user.role == "CUSTOMER") {
      throw ResponseStatusException(
          HttpStatus.FORBIDDEN, "You are not allowed to access this reservation")
    }
    return dto
  }

  /**
   * Creates a new reservation for a vehicle.
   *
   * @param vehicleId The ID of the vehicle for which the reservation is created.
   * @param dto The reservation data transfer object containing reservation details.
   * @return dto of the created reservation.
   * @throws UserNotEligibleException if the user is not eligible to make the reservation.
   * @throws VehicleNotAvailableException if the vehicle is not available for reservation.
   * @throws VehicleNotFoundException if the vehicle is not found.
   */
  @Operation(
      summary = "Create a new reservation",
      description = "Creates a new reservation for a vehicle.",
      responses =
          [
              ApiResponse(
                  responseCode = "201",
                  description = "Reservation created successfully",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema = Schema(implementation = ReservationDTO::class))]),
              ApiResponse(
                  responseCode = "403",
                  description = "User not eligible to make this reservation",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema = Schema(implementation = UserNotEligibleException::class))]),
              ApiResponse(
                  responseCode = "404",
                  description = "Vehicle not found",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema = Schema(implementation = VehicleNotFoundException::class))]),
              ApiResponse(
                  responseCode = "409",
                  description = "Vehicle not available",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema =
                                  Schema(implementation = VehicleNotAvailableException::class))])])
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'MANAGER', 'FLEET_MANAGER')")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  fun createReservation(
      @RequestParam vehicleId: Long,
      @RequestBody dto: ReservationDTO,
      @AuthenticationPrincipal jwt: Jwt,
  ): ReservationDTO {
    val user = authUserService.getCurrentUser(jwt)
    val eligible: Map<String, Boolean> =
        restTemplate
            .exchange(
                "http://localhost:8081/api/v1/customers/${user.id}/eligibility",
                HttpMethod.GET,
                HttpEntity<String>(getAccessTokenHeader()),
                object : ParameterizedTypeReference<Map<String, Boolean>>() {})
            .body ?: mapOf("isEligible" to false)
    if (!eligible.getOrDefault("isEligible", false)) {
      throw UserNotEligibleException(
          "User with id ${dto.userId} is not eligible to make this reservation")
    }

    val effectiveDto =
        if (user.role == "CUSTOMER") dto.copy(userId = user.id, status = ReservationStatus.PENDING)
        else dto

    val vehicle = vehicleService.getVehicleEntityById(vehicleId)
    log.info("Saving new reservation ${vehicle.id}")
    log.info("Vehicle retrieved: $vehicle")
    if (vehicle.pendingRepair ||
        vehicle.pendingCleaning ||
        vehicle.availability != AvailabilityStatus.AVAILABLE) {
      throw VehicleNotAvailableException("Vehicle with id $vehicleId is not available")
    }

    return reservationService.createReservation(effectiveDto, vehicle)
  }

  /**
   * Marks a reservation as picked up.
   *
   * @param id The unique identifier of the reservation to pick up.
   * @return The updated `ReservationDTO` object after the reservation is picked up.
   * @throws ReservationNotFoundException if the reservation is not found.
   */
  @Operation(
      summary = "Pick up a reservation",
      description = "Picks up a reservation by its ID.",
      responses =
          [
              ApiResponse(
                  responseCode = "200",
                  description = "Reservation picked up successfully",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema = Schema(implementation = ReservationDTO::class))]),
              ApiResponse(
                  responseCode = "404",
                  description = "Reservation not found",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema =
                                  Schema(implementation = ReservationNotFoundException::class))])])
  @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'FLEET_MANAGER')")
  @PutMapping("{id}/pickup/", "{id}/pickup")
  @ResponseStatus(HttpStatus.OK)
  fun pickUpReservation(
      @PathVariable id: Long,
  ): ReservationDTO {

    if (reservationService.getReservationById(id).status != ReservationStatus.PAID) {
      throw IllegalStateException("Reservation with id $id is not in PAYED status, cannot pick up")
    }

    return reservationService.updateReservationByStatus(id, ReservationStatus.PICKED_UP)
  }

  /**
   * Marks a reservation as returned.
   *
   * @param id The unique identifier of the reservation to return.
   * @return The updated `ReservationDTO` object after the reservation is returned.
   * @throws ReservationNotFoundException if the reservation is not found.
   */
  @Operation(
      summary = "Return a reservation",
      description = "Returns a reservation by its ID.",
      responses =
          [
              ApiResponse(
                  responseCode = "200",
                  description = "Reservation returned successfully",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema = Schema(implementation = ReservationDTO::class))]),
              ApiResponse(
                  responseCode = "404",
                  description = "Reservation not found",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema =
                                  Schema(implementation = ReservationNotFoundException::class))])])
  @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'FLEET_MANAGER')")
  @PutMapping("{id}/return/", "{id}/return")
  @ResponseStatus(HttpStatus.OK)
  fun returnReservation(
      @PathVariable id: Long,
      @RequestBody body: ReturnInspectionRequestDTO
  ): ReservationDTO {
    if (reservationService.getReservationById(id).status != ReservationStatus.PICKED_UP) {
      throw IllegalStateException(
          "Reservation with id $id is not in PICKED_UP status, cannot return")
    }

    return reservationService.finishReturn(id, body)
  }

  /**
   * Updates an existing reservation.
   *
   * @param id The ID of the reservation to update.
   * @param dto The reservation data transfer object containing updated details.
   * @return The updated reservation.
   * @throws ReservationNotFoundException if the reservation is not found.
   */
  @Operation(
      summary = "Update a reservation",
      description = "Updates an existing reservation.",
      responses =
          [
              ApiResponse(
                  responseCode = "200",
                  description = "Reservation updated successfully",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema = Schema(implementation = ReservationDTO::class))]),
              ApiResponse(
                  responseCode = "404",
                  description = "Reservation not found",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema =
                                  Schema(implementation = ReservationNotFoundException::class))])])
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'MANAGER', 'FLEET_MANAGER')")
  @PutMapping("{id}/", "{id}")
  @ResponseStatus(HttpStatus.OK)
  fun updateReservation(
      @PathVariable id: Long,
      @RequestBody dto: ReservationDTO,
      @AuthenticationPrincipal jwt: Jwt,
  ): ReservationDTO {
    val reservationRequested = reservationService.getReservationById(id)
    val user = authUserService.getCurrentUser(jwt)
    if (reservationRequested.userId != user.id && user.role == "CUSTOMER") {
      throw ResponseStatusException(
          HttpStatus.FORBIDDEN, "You are not allowed to modify this reservation")
    }
    val securedDto = dto.copy(userId = reservationRequested.userId)

    return reservationService.updateReservation(id, securedDto)
  }

  /**
   * Deletes a reservation by its ID.
   *
   * @param id The ID of the reservation to delete.
   * @throws ReservationNotFoundException if the reservation is not found.
   */
  @Operation(
      summary = "Cancel a reservation",
      description =
          "Cancels a reservation by its ID with intelligent refund handling. Automatically processes refunds for paid reservations.",
      responses =
          [
              ApiResponse(
                  responseCode = "200",
                  description = "Reservation cancelled successfully",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema = Schema(implementation = ReservationDTO::class))]),
              ApiResponse(
                  responseCode = "404",
                  description = "Reservation not found",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema =
                                  Schema(implementation = ReservationNotFoundException::class))])])
  @PreAuthorize("hasAnyRole('CUSTOMER','STAFF', 'MANAGER', 'FLEET_MANAGER')")
  @DeleteMapping("{id}/", "{id}")
  @ResponseStatus(HttpStatus.OK)
  fun cancelReservation(@PathVariable id: Long, @AuthenticationPrincipal jwt: Jwt): ReservationDTO {
    /* Sistema di cancellazione intelligente:
     * - Se prenotazione PENDING: cancella direttamente (CANCELLED)
     * - Se prenotazione PAID/PICKED_UP/RETURNED: cancella con richiesta rimborso (CANCELLED_PENDING_REFUND)
     */
    val reservationRequested = reservationService.getReservationById(id)
    val user = authUserService.getCurrentUser(jwt)
    if (reservationRequested.userId != user.id && user.role == "CUSTOMER") {
      throw ResponseStatusException(
          HttpStatus.FORBIDDEN, "You are not allowed to modify this reservation")
    }

    return reservationService.cancelReservationWithRefund(id)
  }

  private fun getAccessTokenHeader() =
      HttpHeaders().apply { setBearerAuth(keycloakService.getAccessToken(restTemplate)) }
}
