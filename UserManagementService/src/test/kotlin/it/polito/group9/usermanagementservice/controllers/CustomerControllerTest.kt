package it.polito.group9.usermanagementservice.controllers

import it.polito.group9.usermanagementservice.dtos.UserDTO
import it.polito.group9.usermanagementservice.entities.UserRole
import it.polito.group9.usermanagementservice.services.UserService
import java.text.SimpleDateFormat
import kotlin.test.Test
import org.junit.jupiter.api.AfterEach
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.client.RestTemplate

@WebMvcTest(CustomerController::class)
@ContextConfiguration(
    classes = [CustomerController::class, CustomerControllerTest.TestConfig::class])
class CustomerControllerTest {
  @Autowired private lateinit var mockMvc: MockMvc

  //  @Autowired private lateinit var objectMapper: ObjectMapper

  companion object {
    val userServiceMock: UserService = mock()
    val restTemplateMock: RestTemplate = mock()
  }

  @Configuration
  class TestConfig {
    @Bean fun userService(): UserService = userServiceMock

    @Bean fun restTemplate(): RestTemplate = restTemplateMock
  }

  @AfterEach
  fun tearDown() {
    // Reset the mocks after each test
    reset(userServiceMock)
  }

  private val customerUserDTO =
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
          createdDate = SimpleDateFormat("yyyy-MM-dd").parse("2025-04-17"),
          score = 6,
      )

  @Test
  @WithMockUser(roles = ["reservation_service"])
  fun `isCustomerEligible should return true when a customer with greater-equal than 6 is found`() {
    val userId = 1L
    whenever(userServiceMock.getCustomerById(userId)).thenReturn(customerUserDTO)

    // Act & Assert
    mockMvc
        .perform(get("/api/v1/customers/$userId/eligibility").with(csrf()))
        .andExpect(status().isOk)
        .andExpect(status().isOk)
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json("{\"isEligible\":true}"))
  }

  @Test
  @WithMockUser(roles = ["reservation_service"])
  fun `isCustomerEligible should return false when a customer with less than 6 is found`() {
    val userId = 1L
    val customerUserDTOfalse = customerUserDTO.copy(score = 5)
    whenever(userServiceMock.getCustomerById(userId)).thenReturn(customerUserDTOfalse)

    // Act & Assert
    mockMvc
        .perform(get("/api/v1/customers/$userId/eligibility").with(csrf()))
        .andExpect(status().isOk)
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json("{\"isEligible\":false}"))
  }
}
