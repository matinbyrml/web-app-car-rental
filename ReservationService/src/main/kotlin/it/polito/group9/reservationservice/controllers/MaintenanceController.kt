package it.polito.group9.reservationservice.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import it.polito.group9.reservationservice.dtos.MaintenanceRecordDTO
import it.polito.group9.reservationservice.exceptions.MaintenanceRecordNotFoundException
import it.polito.group9.reservationservice.services.MaintenanceService
import it.polito.group9.reservationservice.services.VehicleService
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/**
 * Controller class for managing vehicle maintenance records.
 *
 * This class provides RESTful endpoints for retrieving, updating, and deleting maintenance records
 * associated with vehicles.
 *
 * @property maintenanceService The service handling maintenance-related operations.
 * @property vehicleService The service handling vehicle-related operations.
 */
@CrossOrigin(origins = ["*"])
@RestController
@RequestMapping(
    "/api/v1/vehicles/{vehicleId}/maintenances/", "api/v1/vehicles/{vehicleId}/maintenances")
class MaintenanceController(
    val maintenanceService: MaintenanceService,
    val vehicleService: VehicleService
) {

  /**
   * Retrieves a paginated list of maintenance records for a specific vehicle, filtered by optional
   * parameters.
   *
   * @param vehicleId The ID of the vehicle whose maintenance records are to be retrieved.
   * @param startDate Optional start date for filtering maintenance records.
   * @param endDate Optional end date for filtering maintenance records.
   * @param startUpcomingServiceNeeds Optional start date for filtering upcoming service needs.
   * @param endUpcomingServiceNeeds Optional end date for filtering upcoming service needs.
   * @param page The page number for pagination (default is 0).
   * @param size The page size for pagination (default is 10).
   * @return A paginated list of MaintenanceRecordDTO objects.
   */
  @Operation(
      summary = "Get vehicle maintenances with filters",
      description =
          "Returns a paginated list of vehicle maintenances filtered by start date, end date, and upcoming service needs.",
      responses =
          [
              ApiResponse(
                  responseCode = "200",
                  description = "List of filtered vehicle maintenances",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              array =
                                  ArraySchema(
                                      schema =
                                          Schema(implementation = MaintenanceRecordDTO::class)))])])
  @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'FLEET_MANAGER')")
  @GetMapping
  fun getMaintenancesByVehicleId(
      @PathVariable vehicleId: Long,
      @RequestParam(required = false) startDate: String?,
      @RequestParam(required = false) endDate: String?,
      @RequestParam(required = false) startUpcomingServiceNeeds: String?,
      @RequestParam(required = false) endUpcomingServiceNeeds: String?,
      @RequestParam(defaultValue = "0") page: Int,
      @RequestParam(defaultValue = "10") size: Int
  ): Page<MaintenanceRecordDTO> {
    return maintenanceService.getMaintenancesWithFilters(
        vehicleId, startDate, endDate, startUpcomingServiceNeeds, endUpcomingServiceNeeds, 0, 1000)
  }

  /**
   * Retrieves a specific maintenance record by its ID for a given vehicle.
   *
   * @param vehicleId The ID of the vehicle associated with the maintenance record.
   * @param id The ID of the maintenance record to retrieve.
   * @return The MaintenanceRecordDTO object representing the maintenance record.
   */
  @Operation(
      summary = "Get vehicle maintenance by ID",
      description = "Returns details of the vehicle maintenance with specified ID.",
      responses =
          [
              ApiResponse(
                  responseCode = "200",
                  description = "Details of the vehicle maintenance",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema = Schema(implementation = MaintenanceRecordDTO::class))]),
              ApiResponse(
                  responseCode = "404",
                  description = "MaintenanceRecord not found",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema =
                                  Schema(
                                      implementation =
                                          MaintenanceRecordNotFoundException::class))])])
  @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'FLEET_MANAGER')")
  @GetMapping("{id}/", "{id}")
  @ResponseStatus(HttpStatus.OK)
  fun getMaintenanceById(
      @PathVariable vehicleId: Long,
      @PathVariable id: Long
  ): MaintenanceRecordDTO {
    return maintenanceService.getMaintenanceById(id)
  }

  @PreAuthorize("hasAnyRole('STAFF')")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  fun createMaintenanceRecord(
      @PathVariable vehicleId: Long,
      @RequestBody dto: MaintenanceRecordDTO
  ): MaintenanceRecordDTO {
    val vehicle = vehicleService.getVehicleEntityById(vehicleId)
    return maintenanceService.createMaintenanceRecord(dto, vehicle)
  }

  /**
   * Updates a specific maintenance record for a given vehicle.
   *
   * @param vehicleId The ID of the vehicle associated with the maintenance record.
   * @param id The ID of the maintenance record to update.
   * @param dto The MaintenanceRecordDTO object containing updated data.
   * @return The updated MaintenanceRecordDTO object.
   */
  @Operation(
      summary = "Update vehicle maintenance",
      description = "Updates the vehicle maintenance record with the specified ID.",
      responses =
          [
              ApiResponse(
                  responseCode = "200",
                  description = "Updated vehicle maintenance record",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema = Schema(implementation = MaintenanceRecordDTO::class))]),
              ApiResponse(
                  responseCode = "404",
                  description = "MaintenanceRecord not found",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema =
                                  Schema(
                                      implementation =
                                          MaintenanceRecordNotFoundException::class))])])
  @PreAuthorize("hasAnyRole('STAFF')")
  @PutMapping("{id}/", "{id}")
  @ResponseStatus(HttpStatus.OK)
  fun updateMaintenanceRecord(
      @PathVariable vehicleId: Long,
      @PathVariable id: Long,
      @RequestBody dto: MaintenanceRecordDTO
  ): MaintenanceRecordDTO {
    val vehicle = vehicleService.getVehicleEntityById(dto.vehicleId)
    return maintenanceService.updateMaintenanceRecord(dto.copy(id = id), vehicle)
  }

  /**
   * Deletes a specific maintenance record for a given vehicle.
   *
   * @param vehicleId The ID of the vehicle associated with the maintenance record.
   * @param id The ID of the maintenance record to delete.
   */
  @Operation(
      summary = "Delete vehicle maintenance",
      description = "Deletes the vehicle maintenance record with the specified ID.",
      responses =
          [
              ApiResponse(
                  responseCode = "204",
                  description = "Vehicle maintenance record successfully deleted"),
              ApiResponse(
                  responseCode = "404",
                  description = "MaintenanceRecord not found",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema =
                                  Schema(
                                      implementation =
                                          MaintenanceRecordNotFoundException::class))])])
  @PreAuthorize("hasAnyRole('STAFF')")
  @DeleteMapping("{id}/", "{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun deleteMaintenanceRecord(@PathVariable vehicleId: Long, @PathVariable id: Long) {
    maintenanceService.deleteMaintenanceRecord(id)
  }
}
