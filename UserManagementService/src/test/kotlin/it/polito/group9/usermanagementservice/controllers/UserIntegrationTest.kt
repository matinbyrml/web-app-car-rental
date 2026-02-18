package it.polito.group9.usermanagementservice.controllers

import TestUtils.getHeaders
import TestUtils.getUser
import it.polito.group9.usermanagementservice.dtos.UserDTO
import it.polito.group9.usermanagementservice.entities.User
import it.polito.group9.usermanagementservice.entities.UserRole
import it.polito.group9.usermanagementservice.repositories.UserRepository
import java.text.SimpleDateFormat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.exchange
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserIntegrationTest : IntegrationTest() {

  @LocalServerPort private var port: Int = 0

  @Autowired private lateinit var restTemplate: TestRestTemplate

  @Autowired private lateinit var userRepository: UserRepository

  @BeforeEach
  fun setup() {
    userRepository.deleteAll()
  }

  @Nested
  inner class GetUserDetailsTests {
    @ParameterizedTest(name = "getUserDetails should return user when id exists for {0}")
    @ValueSource(strings = ["CUSTOMER", "STAFF", "MANAGER", "FLEET_MANAGER"])
    fun `getUserDetails should return user when id exists`(user: String) {
      val userObject = getUser(user)
      val user =
          userRepository.save(
              User(
                  username = "testUser",
                  name = "Test",
                  surname = "User",
                  ssn = "123-45-6789",
                  email = "test@gmail.com",
                  password = "password123",
                  phone = "+1234567890",
                  address = "123 Main St, City, Country",
                  dateOfBirth = SimpleDateFormat("yyyy-MM-dd").parse("2000-04-18"),
                  role = UserRole.CUSTOMER,
                  createdDate = SimpleDateFormat("yyyy-MM-dd").parse("2025-04-17")))
      val request = HttpEntity(null, getHeaders(userObject, ::getAccessToken))
      val response =
          restTemplate.exchange(
              "http://localhost:$port/api/v1/users/${user.id}",
              HttpMethod.GET,
              request,
              UserDTO::class.java)

      assertEquals(HttpStatus.OK, response.statusCode)
      assertEquals(user.id, response.body?.id)
      assertEquals(user.username, response.body?.username)
    }

    @Test
    fun `getUserDetails should return 401 when not logged in`() {
      val user =
          userRepository.save(
              User(
                  username = "testUser",
                  name = "Test",
                  surname = "User",
                  ssn = "123-45-6789",
                  email = "test@gmail.com",
                  password = "password123",
                  phone = "+1234567890",
                  address = "123 Main St, City, Country",
                  dateOfBirth = SimpleDateFormat("yyyy-MM-dd").parse("2000-04-18"),
                  role = UserRole.CUSTOMER,
                  createdDate = SimpleDateFormat("yyyy-MM-dd").parse("2025-04-17")))
      val response =
          restTemplate.getForEntity(
              "http://localhost:$port/api/v1/users/${user.id}", UserDTO::class.java)

      assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
    }

    @ParameterizedTest(name = "getUserDetails should return 404 when id does not exist for {0}")
    @ValueSource(strings = ["CUSTOMER", "STAFF", "MANAGER", "FLEET_MANAGER"])
    fun `getUserDetails should return 404 when id does not exist`(user: String) {
      val userObject = getUser(user)
      val request = HttpEntity(null, getHeaders(userObject, ::getAccessToken))
      val response =
          restTemplate.exchange(
              "http://localhost:$port/api/v1/users/999", HttpMethod.GET, request, Any::class.java)

      assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }
  }

  @Nested
  inner class GetAllUsersTests {

    @ParameterizedTest(name = "getAllUsers should return 2 users for {0}")
    @ValueSource(strings = ["STAFF", "MANAGER", "FLEET_MANAGER"])
    fun `getAllUsers should return 2 users`(user: String) {
      val userObject = getUser(user)
      val user =
          userRepository.save(
              User(
                  username = "testUser",
                  name = "Test",
                  surname = "User",
                  ssn = "123-45-6789",
                  email = "test@gmail.com",
                  password = "password123",
                  phone = "+1234567890",
                  address = "123 Main St, City, Country",
                  dateOfBirth = SimpleDateFormat("yyyy-MM-dd").parse("2000-04-18"),
                  role = UserRole.CUSTOMER,
                  createdDate = SimpleDateFormat("yyyy-MM-dd").parse("2025-04-17")))

      val user2 =
          userRepository.save(
              User(
                  username = "testUser2",
                  name = "Test2",
                  surname = "User2",
                  ssn = "123-455-789",
                  email = "test2@gmail.com",
                  password = "password123",
                  phone = "+1234567892",
                  address = "123 Main St, City, Country",
                  dateOfBirth = SimpleDateFormat("yyyy-MM-dd").parse("2000-04-18"),
                  role = UserRole.CUSTOMER,
                  createdDate = SimpleDateFormat("yyyy-MM-dd").parse("2025-04-17")))
      val request = HttpEntity(null, getHeaders(userObject, ::getAccessToken))
      val response =
          restTemplate.exchange(
              "http://localhost:$port/api/v1/users",
              HttpMethod.GET,
              request,
              object : ParameterizedTypeReference<Map<String, Any>>() {})

      assertEquals(HttpStatus.OK, response.statusCode)

      val body = response.body!!
      val content = body["content"] as List<*>
      val totalElements = (body["totalElements"] as Number).toInt()

      assertEquals(2, totalElements)
      assertEquals(2, content.size)

      val firstUser = content[0] as Map<*, *>
      assertEquals(user.username, firstUser["username"])
    }

    @ParameterizedTest(name = "getAllUsers should return 0 users for {0}")
    @ValueSource(strings = ["STAFF", "MANAGER", "FLEET_MANAGER"])
    fun `getAllUsers should return 0 users`(user: String) {
      val userObject = getUser(user)
      val request = HttpEntity(null, getHeaders(userObject, ::getAccessToken))
      val response =
          restTemplate.exchange(
              "http://localhost:$port/api/v1/users",
              HttpMethod.GET,
              request,
              object : ParameterizedTypeReference<Map<String, Any>>() {})

      assertEquals(HttpStatus.OK, response.statusCode)

      val body = response.body!!
      val content = body["content"] as List<*>
      val totalElements = (body["totalElements"] as Number).toInt()

      assertEquals(0, totalElements)
      assertEquals(0, content.size)
    }

    @ParameterizedTest(name = "getAllUsers should return 403 for {0}")
    @ValueSource(strings = ["CUSTOMER"])
    fun `getAllUsers should return 403 for other roles`(user: String) {
      val userObject = getUser(user)
      val request = HttpEntity(null, getHeaders(userObject, ::getAccessToken))
      val response =
          restTemplate.exchange(
              "http://localhost:$port/api/v1/users",
              HttpMethod.GET,
              request,
              object : ParameterizedTypeReference<Map<String, Any>>() {})

      assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }
  }

  @Nested
  inner class CreateUserTests {
    @ParameterizedTest(
        name = "createUser should return 201 when user is created successfully for {0}")
    @ValueSource(strings = ["CUSTOMER", "STAFF", "MANAGER", "FLEET_MANAGER"])
    fun `createUser should return 201 when user is created successfully`(user: String) {
      val userObject = getUser(user)
      val userDTO =
          UserDTO(
              username = "testUser",
              name = "Test",
              surname = "User",
              ssn = "123-45-6789",
              email = "test@gmail.com",
              password = "password123",
              phone = "+1234567890",
              address = "123 Main St, City, Country",
              dateOfBirth = SimpleDateFormat("yyyy-MM-dd").parse("2000-04-18"),
              role = UserRole.CUSTOMER.toString(),
              createdDate = SimpleDateFormat("yyyy-MM-dd").parse("2025-04-17"))
      val request = HttpEntity(userDTO, getHeaders(userObject, ::getAccessToken))
      val response =
          restTemplate.postForEntity(
              "http://localhost:$port/api/v1/users", request, UserDTO::class.java)

      assertEquals(HttpStatus.CREATED, response.statusCode)
      assertEquals(userDTO.username, response.body?.username)
    }

    @ParameterizedTest(name = "createUser should return 400 when user already exists for {0}")
    @ValueSource(strings = ["CUSTOMER", "STAFF", "MANAGER", "FLEET_MANAGER"])
    fun `createUser should return 400 when user already exists`(user: String) {
      val userObject = getUser(user)
      val userDTO =
          UserDTO(
              username = "testUser",
              name = "Test",
              surname = "User",
              ssn = "123-45-6789",
              email = "test@gmail.com",
              password = "password123",
              phone = "+1234567890",
              address = "123 Main St, City, Country",
              dateOfBirth = SimpleDateFormat("yyyy-MM-dd").parse("2000-04-18"),
              role = UserRole.CUSTOMER.toString(),
              createdDate = SimpleDateFormat("yyyy-MM-dd").parse("2025-04-17"))
      val request = HttpEntity(userDTO, getHeaders(userObject, ::getAccessToken))
      val existingUserDTO = userDTO.copy()

      val response =
          restTemplate.postForEntity(
              "http://localhost:$port/api/v1/users", request, UserDTO::class.java)

      assertEquals(HttpStatus.CREATED, response.statusCode)
      assertEquals(userDTO.username, response.body?.username)

      val request2 = HttpEntity(existingUserDTO, getHeaders(userObject, ::getAccessToken))
      val response2 =
          restTemplate.postForEntity(
              "http://localhost:$port/api/v1/users", request2, Any::class.java)

      assertEquals(HttpStatus.CONFLICT, response2.statusCode)
    }
  }
}
