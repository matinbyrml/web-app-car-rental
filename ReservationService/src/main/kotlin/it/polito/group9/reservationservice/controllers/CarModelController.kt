package it.polito.group9.reservationservice.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import it.polito.group9.reservationservice.dtos.CarModelDTO
import it.polito.group9.reservationservice.entities.CarSegment
import it.polito.group9.reservationservice.exceptions.CarModelNotFoundException
import it.polito.group9.reservationservice.exceptions.DuplicateCarModelException
import it.polito.group9.reservationservice.services.CarModelService
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@CrossOrigin(origins = ["*"])
@RestController
@RequestMapping("/api/v1/models/", "api/v1/models")
class CarModelController(val carModelService: CarModelService) {

  @Operation(
      summary = "Get car models with filters",
      description =
          "Returns a paginated list of car models filtered by brand, model, year, segment, and price range.",
      responses =
          [
              ApiResponse(
                  responseCode = "200",
                  description = "List of filtered car models",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              array =
                                  ArraySchema(
                                      schema = Schema(implementation = CarModelDTO::class)))])])
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  fun getAllCarModels(
      @RequestParam(required = false) brand: String?,
      @RequestParam(required = false) model: String?,
      @RequestParam(required = false) year: Int?,
      @RequestParam(required = false) segment: CarSegment?,
      @RequestParam(required = false) priceMin: Double?,
      @RequestParam(required = false) priceMax: Double?,
      @RequestParam(defaultValue = "0") page: Int,
      @RequestParam(defaultValue = "10") size: Int
  ): Page<CarModelDTO> {
    return carModelService.getCarModelsWithFilters(
        brand, model, year, segment, priceMin, priceMax, page, size)
  }

  @Operation(
      summary = "Get details of the car model",
      description = "Returns details of the card model with specified id.",
      responses =
          [
              ApiResponse(
                  responseCode = "200",
                  description = "Details of the car model",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema = Schema(implementation = CarModelDTO::class))]),
              ApiResponse(
                  responseCode = "404",
                  description = "Car model not found",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema =
                                  Schema(implementation = CarModelNotFoundException::class))])])
  @GetMapping("{id}/", "{id}")
  @ResponseStatus(HttpStatus.OK)
  fun getCarModel(@PathVariable id: Long): CarModelDTO = carModelService.getCarModelById(id)

  @Operation(
      summary = "Create a new car model",
      description =
          "Adds a new car model to the catalogue. " +
              "Throws a 409 Conflict error if a model with the same brand and model name already exists.",
      responses =
          [
              ApiResponse(
                  responseCode = "201",
                  description = "Car model successfully created",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema = Schema(implementation = CarModelDTO::class))]),
              ApiResponse(
                  responseCode = "409",
                  description =
                      "Duplicate car model error – a model with the same brand and model name already exists",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema =
                                  Schema(implementation = DuplicateCarModelException::class))])])
  @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'FLEET_MANAGER')")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  fun createCarModel(@RequestBody dto: CarModelDTO): ResponseEntity<CarModelDTO> {
    val created = carModelService.createCarModel(dto)
    val location = "/api/v1/models/${created.id}"
    return ResponseEntity.created(java.net.URI.create(location)).body(created)
  }

  @Operation(
      summary = "Update an existing car model",
      description = "Updates the details of an existing car model.",
      responses =
          [
              ApiResponse(
                  responseCode = "200",
                  description = "Car model successfully updated",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema = Schema(implementation = CarModelDTO::class))]),
              ApiResponse(
                  responseCode = "404",
                  description = "Car model not found",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema =
                                  Schema(implementation = CarModelNotFoundException::class))])])
  @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'FLEET_MANAGER')")
  @PutMapping("{id}/", "{id}")
  fun updateCarModel(@PathVariable id: Long, @RequestBody dto: CarModelDTO): CarModelDTO =
      carModelService.updateCarModel(dto.copy(id = id))

  /**
   * Deletes a car model by its ID.
   *
   * @param id the ID of the car model to delete
   * @throws CarModelNotFoundException if the car model with the specified ID does not exist
   */
  @Operation(
      summary = "Delete a car model",
      description = "Deletes the car model with the specified ID.",
      responses =
          [
              ApiResponse(responseCode = "204", description = "Car model successfully deleted"),
              ApiResponse(
                  responseCode = "404",
                  description = "Car model not found",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema =
                                  Schema(implementation = CarModelNotFoundException::class))])])
  @PreAuthorize("hasAnyRole('MANAGER', 'FLEET_MANAGER')")
  @DeleteMapping("{id}/", "{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun deleteCarModel(@PathVariable id: Long) = carModelService.deleteCarModel(id)

  // Get all names of car models
  @Operation(
      summary = "Get all car model names",
      description = "Returns a list of all car model names.",
      responses =
          [
              ApiResponse(
                  responseCode = "200",
                  description = "List of car model names",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              array =
                                  ArraySchema(schema = Schema(implementation = String::class)))])])
  @GetMapping("names")
  @ResponseStatus(HttpStatus.OK)
  fun getAllCarModel(): List<String> = carModelService.getDistinctModelNames()
}
