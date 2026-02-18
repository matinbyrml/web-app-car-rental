package it.polito.group9.usermanagementservice.services.impl

import it.polito.group9.usermanagementservice.dtos.UserDTO
import it.polito.group9.usermanagementservice.entities.User
import it.polito.group9.usermanagementservice.entities.UserRole
import it.polito.group9.usermanagementservice.exceptions.UserAlreadyExistException
import it.polito.group9.usermanagementservice.exceptions.UserNotFoundException
import it.polito.group9.usermanagementservice.repositories.UserRepository
import it.polito.group9.usermanagementservice.services.UserService
import jakarta.transaction.Transactional
import org.keycloak.admin.client.Keycloak
import org.keycloak.representations.idm.CredentialRepresentation
import org.keycloak.representations.idm.UserRepresentation
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(private val userRepository: UserRepository, private val keycloak: Keycloak) :
    UserService {
  private val log = LoggerFactory.getLogger(UserServiceImpl::class.java)

  /**
   * Retrieves a paginated list of all users.
   *
   * @param page The page number to retrieve (0-based index).
   * @param size The number of users per page.
   * @return A `Page` object containing `UserDTO` instances for the requested page.
   * @throws IllegalArgumentException If the page or size parameters are invalid.
   */
  override fun getAllUsers(jwt: Jwt, page: Int, size: Int): Page<UserDTO> {
    val usersPage = userRepository.findAll(PageRequest.of(page, size))
    val claims: Map<String, Any> = jwt.claims

    val username = jwt.getClaim<String>("preferred_username")
    val roles =
        (jwt.getClaim<Map<String, Any>>("realm_access")["roles"] as? List<*>) ?: emptyList<String>()

    log.info("""{ "username": "$username", "roles": $roles }""")

    return usersPage.map { it.toDTO() }
  }

  override fun getUserbyId(id: Long): UserDTO {
    return userRepository
        .findById(id)
        .orElseThrow { UserNotFoundException("User with id $id not found") }
        .toDTO()
  }

  override fun getUserEntityById(id: Long): User {
    return userRepository.findById(id).orElseThrow {
      UserNotFoundException("User with id $id not found")
    }
  }

  @Transactional
  override fun createUser(dto: UserDTO, clearPassword: String): UserDTO {
    log.info("Creating new user {}", dto)

    userRepository.findBySsn(dto.ssn)?.let {
      throw UserAlreadyExistException("User with id ${dto.id} already exists")
    }
    userRepository.findByEmail(dto.email)?.let {
      throw UserAlreadyExistException("User with email ${dto.email} already exists")
    }
    userRepository.findByUsername(dto.username)?.let {
      throw UserAlreadyExistException("User with username ${dto.username} already exists")
    }

    val savedUser = userRepository.save(dto.toEntity())

    log.info("Created new user in DB {}", savedUser)
    val keycloakUser = prepareKeycloakUser(dto, prepareKeycloakPassword(clearPassword))
    val response = keycloak.realm("alberioauto").users().create(keycloakUser)

    log.info("Response from KeyCloak {}", response)

    if (response.status != 201) {
      log.error("Error creating user in Keycloak, rolling back transaction")
      throw RuntimeException("Error creating user in Keycloak")
    }

    val keycloakUserId = response.location.path.split("/").last()

    savedUser.keycloakUserId = keycloakUserId

    userRepository.save(savedUser)

    val role = keycloak.realm("alberioauto").roles()["CUSTOMER"].toRepresentation()
    keycloak.realm("alberioauto").users().get(keycloakUserId).roles().realmLevel().add(listOf(role))

    return savedUser.toDTO()
  }

  private fun prepareKeycloakPassword(password: String): CredentialRepresentation {
    val credentialRepresentation = CredentialRepresentation()
    credentialRepresentation.isTemporary = false
    credentialRepresentation.type = CredentialRepresentation.PASSWORD
    credentialRepresentation.value = password
    return credentialRepresentation
  }

  private fun prepareKeycloakUser(
      dto: UserDTO,
      credentalRepresentation: CredentialRepresentation
  ): UserRepresentation {
    val userRepresentation = UserRepresentation()
    userRepresentation.username = dto.username
    userRepresentation.email = dto.email
    userRepresentation.isEmailVerified = true
    userRepresentation.isEnabled = true
    userRepresentation.credentials = listOf(credentalRepresentation)
    userRepresentation.firstName = dto.name
    userRepresentation.lastName = dto.surname

    return userRepresentation
  }

  override fun updateUser(dto: UserDTO): UserDTO {
    log.info("Updating user {}", dto)

    userRepository.findById(dto.id!!).orElseThrow {
      UserNotFoundException("User with id ${dto.id} not found")
    }

    val updatedUser = userRepository.save(dto.toEntity())
    log.info("User updated with id {}", updatedUser.id)

    return updatedUser.toDTO()
  }

  override fun deleteUser(id: Long) {
    log.info("Deleting user {}", id)

    val user =
        userRepository.findById(id).orElseThrow {
          UserNotFoundException("User with id $id not found")
        }

    userRepository.delete(user)

    log.info("User deleted with id {}", id)
  }

  override fun getCustomerById(id: Long): UserDTO {

    val user = getUserbyId(id)

    if (user.role == UserRole.CUSTOMER.toString()) {
      return user
    } else {
      throw UserNotFoundException("Customer with id $id not found")
    }
  }

  override fun updateCustomerScore(id: Long, score: Int): UserDTO {
    log.info("Updating user score {}", id)

    val user =
        userRepository.findById(id).orElseThrow {
          UserNotFoundException("User with id $id not found")
        }
    if (user.role != UserRole.CUSTOMER) {
      throw UserNotFoundException("Customer with id $id is not found")
    }

    user.score = score
    val updatedUser = userRepository.save(user)
    log.info("User score updated with id {}", updatedUser.id)

    return updatedUser.toDTO()
  }

  override fun getUserByUsername(username: String): UserDTO {
    return userRepository.findByUsername(username)?.toDTO()
        ?: throw UserNotFoundException("User with username $username not found")
  }
}
