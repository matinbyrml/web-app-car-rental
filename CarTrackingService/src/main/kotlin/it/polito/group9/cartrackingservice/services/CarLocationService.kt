package it.polito.group9.cartrackingservice.services

import it.polito.group9.cartrackingservice.dtos.AverageDailyDistanceDTO
import it.polito.group9.cartrackingservice.dtos.CarLocationDTO
import it.polito.group9.cartrackingservice.dtos.LocationsPerDayDTO
import org.springframework.data.domain.Page

interface CarLocationService {
  fun createCarLocation(
      carId: Long,
      createdAt: String,
      latitude: Double,
      longitude: Double
  ): CarLocationDTO

  fun getCarLocationsByCarId(carId: Long, page: Int, size: Int): Page<CarLocationDTO>

  fun getCarLocationsByCarId(carId: Long): List<CarLocationDTO>

  fun getLatestCarLocationByCarId(carId: Long): CarLocationDTO?

  fun getCarLocationsByCarIdGroupedByDay(
      carId: Long,
      page: Int,
      size: Int
  ): Page<LocationsPerDayDTO>

  fun getAverageDailyDistance(carId: Long, page: Int, size: Int): AverageDailyDistanceDTO
}
