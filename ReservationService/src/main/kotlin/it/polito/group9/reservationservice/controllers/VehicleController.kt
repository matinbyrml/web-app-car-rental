package it.polito.group9.reservationservice.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import it.polito.group9.reservationservice.dtos.VehicleDTO
import it.polito.group9.reservationservice.exceptions.CarModelNotFoundException
import it.polito.group9.reservationservice.exceptions.DuplicateVehicleException
import it.polito.group9.reservationservice.exceptions.VehicleNotFoundException
import it.polito.group9.reservationservice.services.CarModelService
import it.polito.group9.reservationservice.services.VehicleService
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import org.springframework.data.domain.Page
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@CrossOrigin(origins = ["*"])
@RestController
@RequestMapping("/api/v1/vehicles/", "api/v1/vehicles")
class VehicleController(val vehicleService: VehicleService, val carModelService: CarModelService) {
  /**
   * Retrieves a paginated list of vehicles filtered by various criteria.
   *
   * @param licensePlate Optional filter by license plate.
   * @param vin Optional filter by Vehicle Identification Number (VIN).
   * @param availability Optional filter by availability status.
   * @param kmMin Optional filter for minimum kilometers.
   * @param kmMax Optional filter for maximum kilometers.
   * @param pendingCleaning Optional filter for vehicles pending cleaning.
   * @param pendingRepair Optional filter for vehicles pending repair.
   * @param page Page number for pagination (default is 0).
   * @param size Page size for pagination (default is 10).
   * @return A paginated list of `VehicleDTO` objects matching the filters.
   */
  @Operation(
      summary = "Get vehicles with filters",
      description =
          "Returns a paginated list of vehicles filtered by license plate, VIN, availability, km range, pending cleaning, pending repair.",
      responses =
          [
              ApiResponse(
                  responseCode = "200",
                  description = "List of filtered vehicles",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              array =
                                  ArraySchema(
                                      schema = Schema(implementation = VehicleDTO::class)))])])
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  fun getAllVehicles(
      @RequestParam(required = false) licensePlate: String?,
      @RequestParam(required = false) vin: String?,
      @RequestParam(required = false) availability: String?,
      @RequestParam(required = false) kmMin: Int?,
      @RequestParam(required = false) kmMax: Int?,
      @RequestParam(required = false) pendingCleaning: Boolean?,
      @RequestParam(required = false) pendingRepair: Boolean?,
      @RequestParam(defaultValue = "0") page: Int,
      @RequestParam(defaultValue = "10") size: Int
  ): Page<VehicleDTO> {
    return vehicleService.getVehiclesWithFilters(
        licensePlate, vin, availability, kmMin, kmMax, pendingCleaning, pendingRepair, page, size)
  }

  /**
   * Retrieves the details of a specific vehicle by its ID.
   *
   * @param id The ID of the vehicle to retrieve.
   * @return The `VehicleDTO` object containing the vehicle details.
   * @throws VehicleNotFoundException if the vehicle with the specified ID is not found.
   */
  @Operation(
      summary = "Get details of the vehicle",
      description = "Returns details of the vehicle with specified id.",
      responses =
          [
              ApiResponse(
                  responseCode = "200",
                  description = "Details of the vehicle",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema = Schema(implementation = VehicleDTO::class))]),
              ApiResponse(
                  responseCode = "404",
                  description = "Vehicle not found",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema = Schema(implementation = VehicleNotFoundException::class))])])
  @GetMapping("{id}/", "{id}")
  @ResponseStatus(HttpStatus.OK)
  fun getVehicleById(@PathVariable id: Long): VehicleDTO {
    return vehicleService.getVehicleById(id)
  }

  /**
   * Creates a new vehicle in the system.
   *
   * @param vehicleDTO The `VehicleDTO` object containing the details of the vehicle to create.
   * @return A `ResponseEntity` containing the created `VehicleDTO` and the location of the new
   *   resource.
   * @throws DuplicateVehicleException if a vehicle with the same license plate already exists.
   */
  @Operation(
      summary = "Create a new vehicle",
      description =
          "Adds a new vehicle to the catalogue. " +
              "Throws a 409 Conflict error if a vehicle with the same license plate already exists.",
      responses =
          [
              ApiResponse(
                  responseCode = "201",
                  description = "Vehicle successfully created",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema = Schema(implementation = VehicleDTO::class))]),
              ApiResponse(
                  responseCode = "409",
                  description =
                      "Duplicate vehicle error – a vehicle with the same license plate already exists.",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema = Schema(implementation = DuplicateVehicleException::class))]),
              ApiResponse(
                  responseCode = "404",
                  description =
                      "Car Model not found – the specified car model ID does not exist or it is null.",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema =
                                  Schema(implementation = CarModelNotFoundException::class))])])
  @PreAuthorize("hasAnyRole('MANAGER', 'FLEET_MANAGER')")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  fun createVehicle(@RequestBody vehicleDTO: VehicleDTO): ResponseEntity<VehicleDTO> {
    val carModel = carModelService.getCarModelById(vehicleDTO.vehicleModelId)
    val created = vehicleService.createVehicle(vehicleDTO, carModel.toEntity())
    val location = "/api/v1/vehicles/${created.id}"
    return ResponseEntity.created(java.net.URI.create(location)).body(created)
  }

  /**
   * Updates the details of an existing vehicle.
   *
   * @param id The ID of the vehicle to update.
   * @param vehicleDTO The `VehicleDTO` object containing the updated details.
   * @return The updated `VehicleDTO` object.
   * @throws VehicleNotFoundException if the vehicle with the specified ID is not found.
   */
  @Operation(
      summary = "Update an existing vehicle",
      description = "Updates the details of an existing vehicle.",
      responses =
          [
              ApiResponse(
                  responseCode = "200",
                  description = "Vehicle successfully updated",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema = Schema(implementation = VehicleDTO::class))]),
              ApiResponse(
                  responseCode = "404",
                  description = "Vehicle not found",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema = Schema(implementation = VehicleNotFoundException::class))])])
  @PreAuthorize("hasAnyRole('MANAGER', 'FLEET_MANAGER')")
  @PutMapping("{id}/", "{id}")
  @ResponseStatus(HttpStatus.OK)
  fun updateVehicle(@PathVariable id: Long, @RequestBody vehicleDTO: VehicleDTO): VehicleDTO {
    val carModel = carModelService.getCarModelById(vehicleDTO.vehicleModelId)
    return vehicleService.updateVehicle(vehicleDTO.copy(id = id), carModel.toEntity())
  }

  /**
   * Deletes a vehicle by its ID.
   *
   * @param id The ID of the vehicle to delete.
   * @throws VehicleNotFoundException if the vehicle with the specified ID is not found.
   */
  @Operation(
      summary = "Delete a vehicle",
      description = "Deletes the vehicle with the specified ID.",
      responses =
          [
              ApiResponse(responseCode = "204", description = "Vehicle successfully deleted"),
              ApiResponse(
                  responseCode = "404",
                  description = "Vehicle not found",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema = Schema(implementation = VehicleNotFoundException::class))])])
  @PreAuthorize("hasAnyRole('MANAGER', 'FLEET_MANAGER')")
  @DeleteMapping("{id}/", "{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun deleteVehicle(@PathVariable id: Long) {
    vehicleService.deleteVehicle(id)
  }

  /**
   * Retrieves a list of all available vehicles within a specified rental period
   *
   * @param startDate The start date of the rental period
   * @param endDate The end date of the rental period
   */
  @Operation(
      summary = "Get available vehicles",
      description = "Returns a list of all available vehicles within the specified rental period.",
      responses =
          [
              ApiResponse(
                  responseCode = "200",
                  description = "List of available vehicles",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              array =
                                  ArraySchema(
                                      schema = Schema(implementation = VehicleDTO::class)))])])
  @GetMapping("available/", "available")
  @ResponseStatus(HttpStatus.OK)
  fun getVehiclesAvailable(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate,
      @RequestParam(defaultValue = "0") page: Int,
      @RequestParam(defaultValue = "10") size: Int,
      @RequestParam(required = false) models: List<String>?
  ): Page<VehicleDTO> {

    val start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
    val end = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
    return vehicleService.getAvailableVehicles(start, end, page, size, models)
  }

  @Operation(
      summary = "Get assignable vehicles",
      description =
          "Returns an optimized, paginated list of vehicles that are not pending cleaning or repair, and are available for assignment within the specified date range.",
      responses =
          [
              ApiResponse(
                  responseCode = "200",
                  description = "List of assignable vehicles",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              array =
                                  ArraySchema(
                                      schema = Schema(implementation = VehicleDTO::class)))])])
  @GetMapping("assignable", "assignable/")
  @ResponseStatus(HttpStatus.OK)
  @PreAuthorize("hasAnyRole('MANAGER', 'FLEET_MANAGER', 'STAFF')")
  fun getAssignableVehicles(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate,
      @RequestParam(defaultValue = "0") page: Int,
      @RequestParam(defaultValue = "10") size: Int
  ): Page<VehicleDTO> {
    // TODO Check, questa funzione serve davvero?? C'è già getVehiclesAvailable che fa la stessa
    // cosa
    val start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
    val end = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
    return vehicleService.getAssignableVehiclesOptimized(start, end, page, size)
  }
}
