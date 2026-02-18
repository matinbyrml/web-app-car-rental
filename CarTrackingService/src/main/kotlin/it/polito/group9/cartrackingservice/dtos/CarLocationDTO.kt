package it.polito.group9.cartrackingservice.dtos

import it.polito.group9.cartrackingservice.entities.CarLocation

data class CarLocationDTO(
    val id: Long? = null,
    val carId: Long,
    val createdAt: String,
    val latitude: Double,
    val longitude: Double,
) {
  companion object {
    fun fromEntity(entity: CarLocation): CarLocationDTO {
      return CarLocationDTO(
          id = entity.id,
          carId = entity.carId,
          createdAt = entity.createdAt,
          latitude = entity.latitude,
          longitude = entity.longitude)
    }

    fun toEntity(dto: CarLocationDTO): CarLocation {
      return CarLocation(
          id = dto.id,
          carId = dto.carId,
          createdAt = dto.createdAt,
          latitude = dto.latitude,
          longitude = dto.longitude)
    }
  }
}
