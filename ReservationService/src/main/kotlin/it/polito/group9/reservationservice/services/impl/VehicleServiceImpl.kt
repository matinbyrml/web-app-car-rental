package it.polito.group9.reservationservice.services.impl

import it.polito.group9.reservationservice.dtos.VehicleDTO
import it.polito.group9.reservationservice.entities.CarModel
import it.polito.group9.reservationservice.entities.Vehicle
import it.polito.group9.reservationservice.exceptions.DuplicateVehicleException
import it.polito.group9.reservationservice.exceptions.VehicleNotFoundException
import it.polito.group9.reservationservice.repositories.VehicleRepository
import it.polito.group9.reservationservice.services.VehicleService
import jakarta.persistence.criteria.Predicate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import kotlin.math.min
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service

/**
 * Service implementation for managing vehicles. This class provides methods for CRUD operations and
 * filtering vehicles based on various criteria.
 *
 * @property vehicleRepository The repository for accessing vehicle data.
 */
@Service
class VehicleServiceImpl(private val vehicleRepository: VehicleRepository) : VehicleService {
  private val log = LoggerFactory.getLogger(VehicleServiceImpl::class.java)

  /**
   * Retrieves a vehicle by its ID.
   *
   * @param id The ID of the vehicle to retrieve.
   * @return The VehicleDTO representing the vehicle.
   * @throws VehicleNotFoundException if the vehicle with the given ID is not found.
   */
  override fun getVehicleById(id: Long): VehicleDTO {
    val vehicle =
        vehicleRepository.findById(id).orElseThrow {
          VehicleNotFoundException("Vehicle with id $id not found")
        }

    return vehicle.toDTO()
  }

  override fun getVehicleEntityById(id: Long): Vehicle =
      vehicleRepository.findById(id).orElseThrow {
        VehicleNotFoundException("Vehicle with id $id not found")
      }

  /**
   * Retrieves a paginated list of vehicles filtered by the provided criteria.
   *
   * @param licensePlate The license plate to filter by (optional).
   * @param vin The VIN to filter by (optional).
   * @param availability The availability status to filter by (optional).
   * @param kmMin The minimum kilometers to filter by (optional).
   * @param kmMax The maximum kilometers to filter by (optional).
   * @param pendingCleaning The cleaning status to filter by (optional).
   * @param pendingRepair The repair status to filter by (optional).
   * @param page The page number for pagination.
   * @param size The page size for pagination.
   * @return A paginated list of VehicleDTOs matching the filters.
   */
  override fun getVehiclesWithFilters(
      licensePlate: String?,
      vin: String?,
      availability: String?,
      kmMin: Int?,
      kmMax: Int?,
      pendingCleaning: Boolean?,
      pendingRepair: Boolean?,
      page: Int,
      size: Int
  ): Page<VehicleDTO> {
    val spec =
        Specification<Vehicle> { root, _, criteriaBuilder ->
          val predicates = mutableListOf<Predicate>()

          licensePlate?.let {
            predicates.add(criteriaBuilder.equal(root.get<String>("licensePlate"), it))
          }
          vin?.let { predicates.add(criteriaBuilder.equal(root.get<String>("vin"), it)) }
          availability?.let {
            predicates.add(criteriaBuilder.equal(root.get<String>("availability"), it))
          }
          kmMin?.let { predicates.add(criteriaBuilder.ge(root.get<Int>("km"), it)) }
          kmMax?.let { predicates.add(criteriaBuilder.le(root.get<Int>("km"), it)) }
          pendingCleaning?.let {
            predicates.add(criteriaBuilder.equal(root.get<Boolean>("pendingCleaning"), it))
          }
          pendingRepair?.let {
            predicates.add(criteriaBuilder.equal(root.get<Boolean>("pendingRepair"), it))
          }

          criteriaBuilder.and(*predicates.toTypedArray())
        }
    log.debug("Filter specification: {}", spec)

    return vehicleRepository.findAll(spec, PageRequest.of(page, size)).map { it.toDTO() }
  }

  /**
   * Creates a new vehicle.
   *
   * @param dto The VehicleDTO containing the details of the vehicle to create.
   * @return The created VehicleDTO.
   * @throws DuplicateVehicleException if a vehicle with the same license plate already exists.
   */
  override fun createVehicle(dto: VehicleDTO, carModel: CarModel): VehicleDTO {
    log.info("Creating new vehicle {}", dto)
    vehicleRepository.findByLicensePlate(dto.licensePlate)?.let {
      throw DuplicateVehicleException("Duplicate vehicle with licensePlate ${dto.licensePlate}")
    }
    vehicleRepository.findByVin(dto.vin)?.let {
      throw DuplicateVehicleException("Duplicate vehicle with vin ${dto.vin}")
    }
    val vehicle = dto.toEntity().copy(vehicleModel = carModel)
    val saved = vehicleRepository.save(vehicle)

    return saved.toDTO()
  }

  /**
   * Updates an existing vehicle.
   *
   * @param dto The VehicleDTO containing the updated details.
   * @return The updated VehicleDTO.
   * @throws VehicleNotFoundException if the vehicle with the given ID is not found.
   */
  override fun updateVehicle(dto: VehicleDTO, carModel: CarModel): VehicleDTO {
    log.info("Updating vehicle {}", dto)
    vehicleRepository.findById(dto.id!!).orElseThrow {
      VehicleNotFoundException("Vehicle with id ${dto.id} not found")
    }
    val saved = vehicleRepository.save(dto.toEntity().copy(vehicleModel = carModel))

    return saved.toDTO()
  }

  /**
   * Deletes a vehicle by its ID.
   *
   * @param id The ID of the vehicle to delete.
   * @throws VehicleNotFoundException if the vehicle with the given ID is not found.
   */
  override fun deleteVehicle(id: Long) {
    log.info("Deleting vehicle with id {}", id)
    val existing =
        vehicleRepository.findById(id).orElseThrow {
          VehicleNotFoundException("Vehicle with id $id not found")
        }
    vehicleRepository.delete(existing)
  }

  override fun getAvailableVehicles(
      startDate: Date,
      endDate: Date,
      page: Int,
      size: Int,
      models: List<String>?
  ): Page<VehicleDTO> {
    val startStr = formatDate(startDate)
    val endStr = formatDate(endDate)
    val vehicles = vehicleRepository.findAvailableVehicles(startStr, endStr).map { it.toDTO() }

    return paginate(vehicles, page, size)
  }

  override fun getAssignableVehiclesOptimized(
      startDate: Date,
      endDate: Date,
      page: Int,
      size: Int
  ): Page<VehicleDTO> {
    val startStr = formatDate(startDate)
    val endStr = formatDate(endDate)
    val vehicles =
        vehicleRepository.getAssignableVehiclesOptimized(startStr, endStr).map { it.toDTO() }

    return paginate(vehicles, page, size)
  }

  private fun formatDate(date: Date): String {
    val fmt = DateTimeFormatter.ISO_LOCAL_DATE
    val zone = ZoneId.systemDefault()
    return date.toInstant().atZone(zone).toLocalDate().format(fmt)
  }

  private fun paginate(vehicles: List<VehicleDTO>, page: Int, size: Int): Page<VehicleDTO> {
    val pages = vehicles.chunked(size)
    val pageToReturn = min(page, pages.size - 1)

    return PageImpl(pages[pageToReturn], PageRequest.of(page, size), vehicles.size.toLong())
  }
}
