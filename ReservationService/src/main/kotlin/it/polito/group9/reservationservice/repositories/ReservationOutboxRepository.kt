package it.polito.group9.reservationservice.repositories

import it.polito.group9.reservationservice.entities.ReservationOutbox
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

/**
 * Repository interface for managing `Reservation` entities. Extends `JpaRepository` to provide
 * basic CRUD operations and `JpaSpecificationExecutor` to support complex queries using
 * specifications.
 */
@Repository
interface ReservationOutboxRepository :
    JpaRepository<ReservationOutbox, Long>, JpaSpecificationExecutor<ReservationOutbox> {}
