package it.polito.group9.cartrackingservice.dtos

data class LocationsPerDayDTO(val carId: Long, val locations: Map<String, List<CarLocationDTO>>)
