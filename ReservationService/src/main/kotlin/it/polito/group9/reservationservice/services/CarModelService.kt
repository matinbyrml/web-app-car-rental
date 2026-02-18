package it.polito.group9.reservationservice.services

import it.polito.group9.reservationservice.dtos.CarModelDTO
import it.polito.group9.reservationservice.entities.CarSegment
import org.springframework.data.domain.Page

/**
 * Service interface for managing car models. It provides methods to create, update, delete, and
 * retrieve car models.
 */
interface CarModelService {
  fun getAllCarModels(): List<CarModelDTO>

  fun getCarModelById(id: Long): CarModelDTO

  fun createCarModel(dto: CarModelDTO): CarModelDTO

  fun updateCarModel(dto: CarModelDTO): CarModelDTO

  fun deleteCarModel(id: Long)

  fun getCarModelsWithFilters(
      brand: String?,
      model: String?,
      year: Int?,
      segment: CarSegment?,
      priceMin: Double?,
      priceMax: Double?,
      page: Int,
      size: Int
  ): Page<CarModelDTO>

  fun getDistinctModelNames(): List<String>
}
