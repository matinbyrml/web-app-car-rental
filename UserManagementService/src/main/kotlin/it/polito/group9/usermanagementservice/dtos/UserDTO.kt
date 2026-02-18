package it.polito.group9.usermanagementservice.dtos

import io.swagger.v3.oas.annotations.media.Schema
import it.polito.group9.usermanagementservice.entities.User
import it.polito.group9.usermanagementservice.entities.UserRole
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size
import java.util.*

/**
 * Data Transfer Object (DTO) for User. This class is used to transfer user data between the client
 * and server.
 *
 * @property id Unique identifier of the user.
 * @property username Username of the user.
 * @property name Name of the user.
 * @property surname Surname of the user.
 * @property ssn Social Security Number of the user.
 * @property email Email address of the user.
 * @property password Password of the user.
 * @property phone Phone number of the user.
 * @property address Address of the user.
 * @property dateOfBirth Date of birth of the user.
 * @property role Role of the user (e.g., CUSTOMER, FLEET_MANAGER, STAFF).
 * @property createdDate Date when the user was created.
 */
data class UserDTO(
    @field:Schema(description = "Unique identifier of the user", example = "1")
    val id: Long? = null,
    @field:Size(min = 1)
    @field:Schema(description = "Username of the user", example = "johndoe")
    val username: String,
    @field:Size(min = 1)
    @field:Schema(description = "Name of the user", example = "John")
    val name: String,
    @field:Size(min = 1)
    @field:Schema(description = "Surname of the user", example = "Doe")
    val surname: String,
    @field:Schema(description = "Social Security Number of the user", example = "123-45-6789")
    val ssn: String,
    @field:Email
    @field:Schema(description = "Email address of the user", example = "john@gmail.com")
    val email: String,
    @field:Size(min = 8)
    @field:Schema(description = "Password of the user", example = "password123")
    val password: String,
    @field:Size(min = 1)
    @field:Schema(description = "Phone number of the user", example = "+1234567890")
    val phone: String?,
    @field:Size(min = 1)
    @field:Schema(description = "Address of the user", example = "123 Main St, City, Country")
    val address: String?,
    @field:Schema(description = "Date of birth of the user", example = "1990-01-01")
    val dateOfBirth: Date?,
    @field:Schema(
        description = "Role of the user (e.g., CUSTOMER, FLEET_MANAGER, STAFF)",
        example = "CUSTOMER")
    val role: String?,
    @field:Schema(description = "Date when the user was created", example = "2023-10-01")
    val createdDate: Date?,
    @field:Schema(description = "Score of the user for eligibility", example = "7")
    val score: Int? = 6,
) {
  fun toEntity(): User {
    return User(
        id = this.id,
        username = this.username,
        name = this.name,
        surname = this.surname,
        ssn = this.ssn,
        email = this.email,
        password = this.password,
        phone = this.phone,
        address = this.address,
        dateOfBirth = this.dateOfBirth,
        role = UserRole.valueOf(this.role ?: "CUSTOMER"),
        createdDate = this.createdDate!!,
        score = this.score)
  }
}
