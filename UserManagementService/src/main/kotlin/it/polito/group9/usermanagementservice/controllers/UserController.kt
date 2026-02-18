package it.polito.group9.usermanagementservice.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import it.polito.group9.usermanagementservice.dtos.UserDTO
import it.polito.group9.usermanagementservice.services.UserService
import java.util.Date
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

/**
 * Controller class for managing user-related operations. Provides endpoints for creating,
 * retrieving, updating, and deleting users.
 *
 * @property userService The service used to handle user-related business logic.
 */
@CrossOrigin(origins = ["*"])
@RestController
@RequestMapping("/api/v1/users/", "/api/v1/users")
class UserController(val userService: UserService) {

  private val log = LoggerFactory.getLogger(UserController::class.java)

  /**
   * Retrieves a paginated list of all users.
   *
   * @param page The page number to retrieve (0-based index).
   * @param size The number of users per page.
   * @return A `Page` object containing `UserDTO` instances for the requested page.
   */
  @Operation(
      summary = "Get all users",
      description = "Returns a paginated list of all users.",
      responses =
          [
              ApiResponse(
                  responseCode = "200",
                  description = "List of users",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema = Schema(implementation = UserDTO::class))])])
  @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'FLEET_MANAGER')")
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  fun getAllUsers(
      @AuthenticationPrincipal jwt: Jwt,
      @RequestParam(defaultValue = "0") page: Int,
      @RequestParam(defaultValue = "10") size: Int,
  ): Page<UserDTO> = userService.getAllUsers(jwt, page, size)

  /**
   * Retrieves the details of a user by their ID.
   *
   * @param userId The ID of the user to retrieve.
   * @return The details of the user as a [UserDTO].
   * @throws UserNotFoundException if the user with the specified ID does not exist.
   */
  @Operation(
      summary = "Get user details",
      description = "Returns details of the user with specified id.",
      responses =
          [
              ApiResponse(
                  responseCode = "200",
                  description = "User details",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema = Schema(implementation = UserDTO::class))]),
              ApiResponse(
                  responseCode = "404",
                  description = "User not found",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema = Schema(implementation = String::class))])])
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'MANAGER', 'FLEET_MANAGER')")
  @GetMapping("/details/{userId}/", "details/{userId}")
  @ResponseStatus(HttpStatus.OK)
  fun getUserDetails(
      @PathVariable userId: Long,
      @AuthenticationPrincipal jwt: Jwt,
  ): UserDTO {

    val user = userService.getUserbyId(userId)
    isUserAuthToCRUDOperations(user.username, jwt)

    return userService.getUserbyId(userId)
  }

  /**
   * Creates a new user with the provided details.
   *
   * @param dto The details of the user to create, encapsulated in a [UserDTO].
   * @return A [ResponseEntity] containing the created user and the location of the new resource.
   * @throws InvalidInputException if the provided user details are invalid.
   */
  @Operation(
      summary = "Create a new user",
      description = "Creates a new user with the provided details.",
      responses =
          [
              ApiResponse(
                  responseCode = "201",
                  description = "User created successfully",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema = Schema(implementation = UserDTO::class))]),
              ApiResponse(
                  responseCode = "400",
                  description = "Invalid input data",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema = Schema(implementation = String::class))])])
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  fun createUser(@RequestBody dto: UserDTO): ResponseEntity<UserDTO> {
    val clearPassword = dto.password
    val hashedPassword = BCryptPasswordEncoder().encode(clearPassword)
    val created =
        userService.createUser(
            dto.copy(role = "CUSTOMER", password = hashedPassword, createdDate = Date()),
            clearPassword)
    val location = "/api/v1/users/${created.id}"
    return ResponseEntity.created(java.net.URI.create(location)).body(created)
  }

  /**
   * Updates the details of an existing user.
   *
   * @param userId The ID of the user to update.
   * @param dto The updated user details, encapsulated in a [UserDTO].
   * @return A [ResponseEntity] containing the updated user details.
   * @throws UserNotFoundException if the user with the specified ID does not exist.
   */
  @Operation(
      summary = "Update a user",
      description = "Updates the details of an existing user.",
      responses =
          [
              ApiResponse(
                  responseCode = "200",
                  description = "User updated successfully",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema = Schema(implementation = UserDTO::class))]),
              ApiResponse(
                  responseCode = "404",
                  description = "User not found",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema = Schema(implementation = String::class))])])
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'MANAGER', 'FLEET_MANAGER')")
  @PutMapping("/{userId}", "{userId}")
  @ResponseStatus(HttpStatus.OK)
  fun updateUser(
      @PathVariable userId: Long,
      @RequestBody dto: UserDTO,
      @AuthenticationPrincipal jwt: Jwt,
  ): ResponseEntity<UserDTO> {

    val user = userService.getUserbyId(userId)
    isUserAuthToCRUDOperations(user.username, jwt)

    val updated = userService.updateUser(dto.copy(id = userId))
    return ResponseEntity.ok(updated)
  }

  /**
   * Deletes a user by their ID.
   *
   * @param userId The ID of the user to delete.
   * @throws UserNotFoundException if the user with the specified ID does not exist.
   */
  @Operation(
      summary = "Delete a user",
      description = "Deletes the user with the specified ID.",
      responses =
          [
              ApiResponse(responseCode = "204", description = "User deleted successfully"),
              ApiResponse(
                  responseCode = "404",
                  description = "User not found",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema = Schema(implementation = String::class))])])
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'MANAGER', 'FLEET_MANAGER')")
  @DeleteMapping("/{userId}", "{userId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun deleteUser(@PathVariable userId: Long) = userService.deleteUser(userId)

  /**
   * Retrieves the details of a user by their username.
   *
   * @param username The username of the user to retrieve.
   * @return The details of the user as a [UserDTO].
   * @throws UserNotFoundException if the user with the specified username does not exist.
   */
  @Operation(summary = "Get user by username", description = "Returns user details by username.")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'MANAGER', 'FLEET_MANAGER')")
  @GetMapping("/{username}", "{username}")
  @ResponseStatus(HttpStatus.OK)
  fun getUserByUsername(
      @PathVariable username: String,
      @AuthenticationPrincipal jwt: Jwt,
  ): UserDTO {
    isUserAuthToCRUDOperations(username, jwt)

    return userService.getUserByUsername(username)
  }

  // UserController.kt

  fun isUserAuthToCRUDOperations(username: String, jwt: Jwt): Boolean {
    val usernameJwt = jwt.getClaim<String>("preferred_username")
    val roles =
        (jwt.getClaim<Map<String, Any>>("realm_access")["roles"] as? List<*>) ?: emptyList<String>()

    log.info("User logged in as $usernameJwt and roles: $roles")

    // Recognize service accounts
    val clientId = jwt.getClaim<String?>("client_id") // present in client-credentials tokens
    val isServiceAccount = usernameJwt.startsWith("service-account-") || clientId != null
    if (isServiceAccount) return true

    val isManagerial =
        roles.contains("STAFF") || roles.contains("MANAGER") || roles.contains("FLEET_MANAGER")
    val isCustomer = roles.contains("CUSTOMER")

    if (!isManagerial && isCustomer && usernameJwt != username) {
      throw IllegalArgumentException("As a pure customer you can only access your own user details")
    }
    return true
  }
}
