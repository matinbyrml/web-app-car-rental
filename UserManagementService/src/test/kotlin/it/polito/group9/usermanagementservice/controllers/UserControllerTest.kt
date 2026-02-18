package it.polito.group9.usermanagementservice.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import it.polito.group9.usermanagementservice.dtos.UserDTO
import it.polito.group9.usermanagementservice.entities.UserRole
import it.polito.group9.usermanagementservice.services.UserService
import java.text.SimpleDateFormat
import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.jupiter.api.AfterEach
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(UserController::class)
@ContextConfiguration(classes = [UserController::class, UserControllerTest.TestConfig::class])
class UserControllerTest {
  @Autowired private lateinit var mockMvc: MockMvc

  @Autowired private lateinit var objectMapper: ObjectMapper

  companion object {
    val userServiceMock: UserService = mock()
  }

  @Configuration
  class TestConfig {
    @Bean fun userService(): UserService = userServiceMock
  }

  @AfterEach
  fun tearDown() {
    // Reset the mocks after each test
    reset(userServiceMock)
  }

  private val sampleUserDTO =
      UserDTO(
          id = 1L,
          username = "testUser",
          name = "Test",
          surname = "User",
          ssn = "123-45-6789",
          email = "test@gmail.com",
          password = "password123",
          phone = "+1234567890",
          address = "123 Main St, City, Country",
          dateOfBirth = SimpleDateFormat("yyyy-MM-dd").parse("2000-04-18"),
          role = UserRole.CUSTOMER.name,
          createdDate = SimpleDateFormat("yyyy-MM-dd").parse("2025-04-17"))

  @Test
  @WithMockUser(roles = ["STAFF"])
  fun `getAllUsers should return paginated list of users`() {
    val page = 0
    val size = 10
    val expectedUsers = listOf(sampleUserDTO)
    val pageRequest = PageRequest.of(0, 10)
    val mockPage: Page<UserDTO> = PageImpl(expectedUsers, pageRequest, expectedUsers.size.toLong())

    whenever(userServiceMock.getAllUsers(any(), eq(page), eq(size))).thenReturn(mockPage)

    mockMvc
        .perform(
            get("/api/v1/users")
                .param("page", page.toString())
                .param("size", size.toString())
                .accept(MediaType.APPLICATION_JSON)
                .with(csrf()))
        .andExpect(status().isOk)
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
  }

  @Test
  @WithMockUser(roles = ["CUSTOMER"])
  fun `getUserDetails should return user when id exists`() {
    val userId = 1L
    val expectedUser = sampleUserDTO
    whenever(userServiceMock.getUserbyId(userId)).thenReturn(expectedUser)

    val result =
        mockMvc
            .perform(get("/api/v1/users/$userId").accept(MediaType.APPLICATION_JSON).with(csrf()))
            .andExpect(status().isOk)
            .andReturn()

    val actualUser = objectMapper.readValue(result.response.contentAsString, UserDTO::class.java)
    assertEquals(expectedUser, actualUser)
  }

  @Test
  @WithMockUser(roles = ["CUSTOMER"])
  fun `createUser should return created when input is valid`() {
    val newUser = sampleUserDTO.copy(id = null)
    val createdUser = sampleUserDTO
    whenever(userServiceMock.createUser(newUser, newUser.password)).thenReturn(createdUser)

    val result =
        mockMvc
            .perform(
                post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newUser))
                    .with(csrf()))
            .andExpect(status().isCreated)
            .andReturn()

    val actualUser = objectMapper.readValue(result.response.contentAsString, UserDTO::class.java)
    assertEquals(createdUser, actualUser)
  }

  @Test
  @WithMockUser(roles = ["CUSTOMER"])
  fun `createUser should return BadRequest when body is missing`() {
    mockMvc
        .perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).with(csrf()))
        .andExpect(status().isBadRequest)
  }

  @Test
  @WithMockUser(roles = ["CUSTOMER"])
  fun `updateUser should return updated user when valid input`() {
    val userId = 1L
    val updatedUser = sampleUserDTO.copy(name = "UpdatedName")
    whenever(userServiceMock.updateUser(updatedUser.copy(id = userId))).thenReturn(updatedUser)

    val result =
        mockMvc
            .perform(
                put("/api/v1/users/$userId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updatedUser))
                    .with(csrf()))
            .andExpect(status().isOk)
            .andReturn()

    val actualUser = objectMapper.readValue(result.response.contentAsString, UserDTO::class.java)
    assertEquals(updatedUser, actualUser)
  }

  @Test
  @WithMockUser(roles = ["CUSTOMER"])
  fun `deleteUser should return NoContent when user exists`() {
    val userId = 1L

    mockMvc
        .perform(delete("/api/v1/users/$userId").accept(MediaType.APPLICATION_JSON).with(csrf()))
        .andExpect(status().isNoContent)
  }
}
