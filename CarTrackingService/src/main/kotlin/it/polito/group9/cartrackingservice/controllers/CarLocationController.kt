package it.polito.group9.cartrackingservice.controllers

import it.polito.group9.cartrackingservice.dtos.CarLocationDTO
import it.polito.group9.cartrackingservice.dtos.CreateCarLocationRequest
import it.polito.group9.cartrackingservice.services.CarLocationService
import jakarta.validation.Valid
import java.net.URI
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@CrossOrigin(origins = ["*"])
@RestController
@RequestMapping("/api/v1/carlocations/", "api/v1/carlocations")
class CarLocationController(private val carLocationService: CarLocationService) {

  @PreAuthorize("hasRole('CAR')")
  @PostMapping
  fun createCarLocation(
      @Valid @RequestBody dto: CreateCarLocationRequest
  ): ResponseEntity<CarLocationDTO> {
    val created =
        carLocationService.createCarLocation(
            carId = dto.carId,
            createdAt = dto.createdAt,
            latitude = dto.latitude,
            longitude = dto.longitude)

    return ResponseEntity.created(URI.create("/api/v1/carlocations/${created.id}")).body(created)
  }

  @PreAuthorize("hasAnyRole('MANAGER', 'FLEET_MANAGER', 'analytics_service')")
  @GetMapping("/{carId}")
  fun getCarLocations(
      @PathVariable carId: Long,
      @RequestParam(defaultValue = "0") page: Int,
      @RequestParam(defaultValue = "10") size: Int
  ): Page<CarLocationDTO> {
    return carLocationService.getCarLocationsByCarId(carId, page, size)
  }

  @PreAuthorize("hasAnyRole('MANAGER', 'FLEET_MANAGER')")
  @GetMapping("/{carId}/latest")
  fun getLatestCarLocation(@PathVariable carId: Long): CarLocationDTO? {
    return carLocationService.getLatestCarLocationByCarId(carId)
  }
}
