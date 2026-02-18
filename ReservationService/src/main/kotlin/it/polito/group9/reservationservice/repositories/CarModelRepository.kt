package it.polito.group9.reservationservice.repositories

import it.polito.group9.reservationservice.entities.CarModel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * Repository interface for managing CarModel entities. It extends JpaRepository for basic CRUD
 * operations and JpaSpecificationExecutor for complex queries.
 */
@Repository
interface CarModelRepository : JpaRepository<CarModel, Long>, JpaSpecificationExecutor<CarModel> {
  fun findByBrandAndModel(brand: String, model: String): CarModel?

  @Query("SELECT DISTINCT c.model FROM CarModel c") fun findDistinctModelNames(): List<String>
}
