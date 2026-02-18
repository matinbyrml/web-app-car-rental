package it.polito.group9.reservationservice.services.impl

import it.polito.group9.reservationservice.dtos.CarModelDTO
import it.polito.group9.reservationservice.entities.CarModel
import it.polito.group9.reservationservice.entities.CarSegment
import it.polito.group9.reservationservice.exceptions.CarModelNotFoundException
import it.polito.group9.reservationservice.exceptions.DuplicateCarModelException
import it.polito.group9.reservationservice.repositories.CarModelRepository
import it.polito.group9.reservationservice.services.CarModelService
import jakarta.persistence.criteria.Predicate
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service

/**
 * Service implementation for managing car models. It provides methods to create, update, delete,
 * and retrieve car models.
 */
@Service
class CarModelServiceImpl(
    private val carModelRepository: CarModelRepository,
) : CarModelService {

  private val log = LoggerFactory.getLogger(CarModelServiceImpl::class.java)

  /**
   * Retrieves all car models.
   *
   * @return A list of all car models mapped to DTOs.
   */
  override fun getAllCarModels(): List<CarModelDTO> =
      carModelRepository.findAll().map { it.toDTO() }

  /**
   * Retrieves a car model by its ID.
   *
   * @param id The ID of the car model to retrieve.
   * @return The car model with the specified ID mapped to DTO.
   * @throws CarModelNotFoundException if no car model with the specified ID exists.
   */
  override fun getCarModelById(id: Long): CarModelDTO =
      carModelRepository
          .findById(id)
          .orElseThrow { CarModelNotFoundException("CarModel with ID $id does not exist.") }
          .toDTO()

  /**
   * Creates a new car model.
   *
   * @param dto The data transfer object containing the details of the car model to create.
   * @return The created car model mapped to DTO.
   * @throws DuplicateCarModelException if a car model with the same brand and model already exists.
   */
  override fun createCarModel(dto: CarModelDTO): CarModelDTO {
    log.info("Creating car model {}:", dto)
    carModelRepository.findByBrandAndModel(dto.brand, dto.model)?.let {
      log.error("CarModel '{}' and model '{}' is a duplicate entry.", dto.brand, dto.model)
      throw DuplicateCarModelException(
          "CarModel already exists for brand '${dto.brand}' and model '${dto.model}'.")
    }
    val carModel = dto.toEntity()
    val saved = carModelRepository.save(carModel)
    log.info("Created CarModel with ID = {}", saved.id)
    return saved.toDTO()
  }

  /**
   * Updates an existing car model.
   *
   * @param id The ID of the car model to update.
   * @param dto The data transfer object containing the updated details of the car model.
   * @return The updated car model mapped to DTO.
   * @throws CarModelNotFoundException if no car model with the specified ID exists.
   */
  override fun updateCarModel(dto: CarModelDTO): CarModelDTO {
    log.info("Updating car model: {}", dto)
    carModelRepository.findById(dto.id!!).orElseThrow {
      CarModelNotFoundException("CarModel with ID ${dto.id} does not exist.")
    }
    val saved = carModelRepository.save(dto.toEntity())
    log.info("Updated CarModel successfully with ID = {}", saved.id)
    return saved.toDTO()
  }

  /**
   * Deletes a car model by its ID.
   *
   * @param id The ID of the car model to delete.
   * @throws CarModelNotFoundException if no car model with the specified ID exists.
   */
  override fun deleteCarModel(id: Long) {
    log.info("Retrieving car model with ID: {}", id)
    val existing =
        carModelRepository.findById(id).orElseThrow {
          CarModelNotFoundException("CarModel with ID $id does not exist.")
        }
    carModelRepository.delete(existing)
    log.info("Deleted CarModel with ID = {}", existing.id)
  }

  /**
   * Retrieves car models with optional filters.
   *
   * @param brand The brand of the car model (optional).
   * @param model The model of the car (optional).
   * @param year The year of the car model (optional).
   * @param segment The segment of the car model (optional).
   * @param priceMin The minimum price of the car model (optional).
   * @param priceMax The maximum price of the car model (optional).
   * @param page The page number for pagination.
   * @param size The size of each page for pagination.
   * @return A page of car models mapped to DTOs matching the specified filters.
   */
  override fun getCarModelsWithFilters(
      brand: String?,
      model: String?,
      year: Int?,
      segment: CarSegment?,
      priceMin: Double?,
      priceMax: Double?,
      page: Int,
      size: Int
  ): Page<CarModelDTO> {
    val spec =
        Specification<CarModel> { root, _, cb ->
          val predicates = mutableListOf<Predicate>()
          if (brand != null) {
            predicates.add(cb.equal(root.get<String>("brand"), brand))
          }
          if (model != null) {
            predicates.add(cb.equal(root.get<String>("model"), model))
          }
          if (year != null) {
            predicates.add(cb.equal(root.get<Int>("modelYear"), year))
          }
          if (segment != null) {
            predicates.add(cb.equal(root.get<CarSegment>("segment"), segment))
          }
          if (priceMin != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("rentalPricePerDay"), priceMin))
          }
          if (priceMax != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("rentalPricePerDay"), priceMax))
          }
          cb.and(*predicates.toTypedArray())
        }
    log.debug("Filter Specification: {}", spec)

    val carModelsPage = carModelRepository.findAll(spec, PageRequest.of(page, size))

    return carModelsPage.map { it.toDTO() }
  }

  /**
   * Retrieves distinct model names of car models.
   *
   * @return A list of distinct model names.
   */
  override fun getDistinctModelNames(): List<String> {
    log.info("Retrieving distinct model names of car models")
    val distinctModels = carModelRepository.findDistinctModelNames()
    log.info("Found {} distinct model names", distinctModels.size)
    return distinctModels
  }
}
