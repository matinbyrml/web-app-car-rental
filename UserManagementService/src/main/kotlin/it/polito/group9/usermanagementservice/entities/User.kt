package it.polito.group9.usermanagementservice.entities

import it.polito.group9.usermanagementservice.dtos.UserDTO
import jakarta.persistence.*
import java.util.*

enum class UserRole {
  CUSTOMER,
  STAFF,
  FLEET_MANAGER,
  MANAGER,
}

/**
 * Entity class representing a user in the system.
 *
 * @property id The unique identifier of the user.
 * @property username The username of the user. Must be unique and not null.
 * @property name The name of the user.
 * @property surname The surname of the user.
 * @property ssn The social security number of the user. Must be unique and not null.
 * @property email The email address of the user. Must be unique and not null.
 * @property password The password of the user.
 * @property phone The phone number of the user.
 * @property address The address of the user.
 * @property dateOfBirth The date of birth of the user.
 * @property role The role of the user (e.g.CUSTOMER, STAFF, FLEET_MANAGER). Must be not null.
 * @property createdDate The date when the user was created.
 */
@Entity
@Table(name = "\"user\"")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long? = null,
    @Column(name = "username", unique = true, nullable = false) val username: String,
    @Column(name = "name", nullable = false) val name: String,
    @Column(name = "surname", nullable = false) val surname: String,
    @Column(name = "ssn", unique = true, nullable = false) val ssn: String,
    @Column(name = "email", unique = true, nullable = false) val email: String,
    @Column(name = "password", nullable = false) val password: String,
    @Column(name = "phone") val phone: String?,
    @Column(name = "address") val address: String?,
    @Column(name = "date_of_birth") val dateOfBirth: Date?,
    @Enumerated(EnumType.STRING) @Column(name = "role", nullable = false) val role: UserRole,
    @Column(name = "created_date", nullable = false) val createdDate: Date,
    @Column(name = "score", nullable = true) var score: Int? = null,
    @Column(name = "keycloak_user_id", nullable = true) var keycloakUserId: String? = null
) {
  fun toDTO(): UserDTO =
      UserDTO(
          id = id,
          username = username,
          name = name,
          surname = surname,
          email = email,
          password = password,
          phone = phone,
          address = address,
          dateOfBirth = dateOfBirth,
          role = role.toString(),
          createdDate = createdDate,
          ssn = ssn,
          score = score,
      )
}
