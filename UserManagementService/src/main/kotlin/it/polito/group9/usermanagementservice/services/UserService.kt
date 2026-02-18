package it.polito.group9.usermanagementservice.services

import it.polito.group9.usermanagementservice.dtos.UserDTO
import it.polito.group9.usermanagementservice.entities.User
import org.springframework.data.domain.Page
import org.springframework.security.oauth2.jwt.Jwt

/**
 * Service interface for managing users. This interface defines the contract for user-related
 * operations, including CRUD operations and filtering users based on various criteria.
 */
interface UserService {

  /**
   * Retrieves a paginated list of all users.
   *
   * @param page The page number to retrieve (0-based index).
   * @param size The number of users per page.
   * @return A `Page` object containing `UserDTO` instances for the requested page.
   */
  fun getAllUsers(jwt: Jwt, page: Int, size: Int): Page<UserDTO>

  /**
   * Retrieves a user by its ID.
   *
   * @param id The ID of the user to retrieve.
   * @return The UserDTO representing the user.
   */
  fun getUserbyId(id: Long): UserDTO

  /**
   * Retrieves a User entity by its ID.
   *
   * This method fetches the User entity from the database using the provided ID. It is typically
   * used internally within the service layer when the full entity is required for further
   * processing or operations.
   *
   * @param id The ID of the user to retrieve.
   * @return The User entity corresponding to the given ID.
   */
  fun getUserEntityById(id: Long): User

  /**
   * Creates a new user.
   *
   * @param dto The UserDTO containing the details of the user to create.
   * @return The created UserDTO.
   */
  fun createUser(dto: UserDTO, clearPassword: String): UserDTO

  /**
   * Updates an existing user.
   *
   * @param dto The UserDTO containing the updated details.
   * @return The updated UserDTO.
   */
  fun updateUser(dto: UserDTO): UserDTO

  /**
   * Deletes a user by its ID.
   *
   * @param id The ID of the user to delete.
   */
  fun deleteUser(id: Long)

  /**
   * Retrieves a user by its ID.
   *
   * This method is used to fetch a user by its ID, typically for use in the context of customer
   * management or related operations.
   *
   * @param id The ID of the user to retrieve.
   * @return The UserDTO representing the user.
   */
  fun getCustomerById(id: Long): UserDTO

  /**
   * Update the score of a user.
   *
   * @param id The ID of the user to update.
   */
  fun updateCustomerScore(id: Long, score: Int): UserDTO

  /**
   * Retrieves a user by its username.
   *
   * This method is used to fetch a user by its username, typically for authentication or
   * user-specific operations.
   *
   * @param username The username of the user to retrieve.
   * @return The UserDTO representing the user.
   */
  fun getUserByUsername(username: String): UserDTO
}
