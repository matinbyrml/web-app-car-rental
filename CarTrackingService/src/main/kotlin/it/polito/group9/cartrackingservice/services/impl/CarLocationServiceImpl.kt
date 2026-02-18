package it.polito.group9.cartrackingservice.services.impl

import it.polito.group9.cartrackingservice.dtos.AverageDailyDistanceDTO
import it.polito.group9.cartrackingservice.dtos.CarLocationDTO
import it.polito.group9.cartrackingservice.dtos.LocationsPerDayDTO
import it.polito.group9.cartrackingservice.entities.CarLocation
import it.polito.group9.cartrackingservice.repositories.CarLocationRepository
import it.polito.group9.cartrackingservice.services.CarLocationService
import jakarta.persistence.criteria.Predicate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service

@Service
class CarLocationServiceImpl(private val carLocationRepository: CarLocationRepository) :
    CarLocationService {
  override fun createCarLocation(
      carId: Long,
      createdAt: String,
      latitude: Double,
      longitude: Double
  ): CarLocationDTO {
    val carLocation =
        CarLocation(
            carId = carId,
            createdAt = createdAt,
            latitude = latitude,
            longitude = longitude,
        )

    return carLocationRepository.save(carLocation).let { CarLocationDTO.fromEntity(it) }
  }

  override fun getCarLocationsByCarId(carId: Long, page: Int, size: Int): Page<CarLocationDTO> {
    val spec =
        Specification<CarLocation> { root, _, cb ->
          val predicates = mutableListOf<Predicate>(cb.equal(root.get<String>("carId"), carId))
          cb.and(*predicates.toTypedArray())
        }
    return carLocationRepository.findAll(spec, PageRequest.of(page, size)).map {
      CarLocationDTO.fromEntity(it)
    }
  }

  override fun getCarLocationsByCarId(carId: Long): List<CarLocationDTO> {
    val spec =
        Specification<CarLocation> { root, _, cb ->
          val predicates = mutableListOf<Predicate>(cb.equal(root.get<String>("carId"), carId))
          cb.and(*predicates.toTypedArray())
        }
    return carLocationRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "createdAt")).map {
      CarLocationDTO.fromEntity(it)
    }
  }

  override fun getLatestCarLocationByCarId(carId: Long): CarLocationDTO? {
    val spec =
        Specification<CarLocation> { root, _, cb ->
          val predicates = mutableListOf<Predicate>(cb.equal(root.get<String>("carId"), carId))
          cb.and(*predicates.toTypedArray())
        }
    return carLocationRepository
        .findAll(spec, PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "createdAt")))
        .firstOrNull()
        ?.let { it.let { CarLocationDTO.fromEntity(it) } }
  }

  override fun getCarLocationsByCarIdGroupedByDay(
      carId: Long,
      page: Int,
      size: Int
  ): Page<LocationsPerDayDTO> {
    val spec =
        Specification<CarLocation> { root, _, cb ->
          val predicates = mutableListOf<Predicate>(cb.equal(root.get<String>("carId"), carId))
          cb.and(*predicates.toTypedArray())
        }
    val paginated =
        carLocationRepository.findAll(
            spec, PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt")))
    val locations =
        paginated
            .map { CarLocationDTO.fromEntity(it) }
            .groupBy {
              OffsetDateTime.parse(it.createdAt).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            }
            .map { (day, locations) -> LocationsPerDayDTO(carId, mapOf(day to locations)) }

    return PageImpl(locations, PageRequest.of(page, size), paginated.totalElements)
  }

  override fun getAverageDailyDistance(carId: Long, page: Int, size: Int): AverageDailyDistanceDTO {
    val spec =
        Specification<CarLocation> { root, _, cb ->
          val predicates = mutableListOf<Predicate>(cb.equal(root.get<String>("carId"), carId))
          cb.and(*predicates.toTypedArray())
        }
    val paginated =
        carLocationRepository.findAll(
            spec, PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt")))
    val totalDistance =
        paginated.content.zipWithNext().sumOf { (loc1, loc2) ->
          distanceBetweenWayPoints(loc1, loc2, 6371.0)
        }
    val numberOfDays =
        paginated.content
            .map {
              OffsetDateTime.parse(it.createdAt).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            }
            .distinct()
            .size

    return AverageDailyDistanceDTO(carId, totalDistance / numberOfDays)
  }

  fun distanceBetweenWayPoints(
      point1: CarLocation,
      point2: CarLocation,
      earthRadius: Double
  ): Double {
    val lat1Rad = Math.toRadians(point1.latitude)
    val lon1Rad = Math.toRadians(point1.longitude)
    val lat2Rad = Math.toRadians(point2.latitude)
    val lon2Rad = Math.toRadians(point2.longitude)

    val dLat = lat2Rad - lat1Rad
    val dLon = lon2Rad - lon1Rad

    val a = sin(dLat / 2).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    return earthRadius * c // Distance in the same unit as earthRadius (default: km)
  }
}
