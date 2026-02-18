package it.polito.group9.usermanagementservice.repositories

import it.polito.group9.usermanagementservice.entities.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

/**
 * Repository interface for managing `Note` entities.
 *
 * This interface provides CRUD operations and additional query capabilities for the `Note` entity
 * by extending `JpaRepository` and `JpaSpecificationExecutor`.
 */
@Repository
interface UserRepository : JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
  fun findByEmail(email: String): User?

  fun findByUsername(username: String): User?

  fun findBySsn(phoneNumber: String): User?
}
