package it.polito.group9.cartrackingservice.repositories

import it.polito.group9.cartrackingservice.entities.CarLocation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface CarLocationRepository :
    JpaRepository<CarLocation, Long>, JpaSpecificationExecutor<CarLocation>
