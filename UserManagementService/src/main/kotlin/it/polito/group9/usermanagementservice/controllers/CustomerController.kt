package it.polito.group9.usermanagementservice.controllers

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import it.polito.group9.usermanagementservice.dtos.ReservationDTO
import it.polito.group9.usermanagementservice.exceptions.UserNotFoundException
import it.polito.group9.usermanagementservice.services.UserService
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity

/**
 * Controller class for managing user-related operations. Provides endpoints for creating,
 * retrieving, updating, and deleting users.
 *
 * @property userService The service used to handle user-related business logic.
 */
@CrossOrigin(origins = ["*"])
@RestController
@RequestMapping("/api/v1/customers/", "/api/v1/customers")
class CustomerController(val userService: UserService, val restTemplate: RestTemplate) {

  /**
   * Check the elegibility of a customer by their ID.
   *
   * @param userId The ID of the customer to check.
   * @return the elegibility status of the customer. (true if elegible, false otherwise)
   * @throws UserNotFoundException if the customer with the specified ID does not exist.
   */
  @Operation(
      summary = "Check customer elegibility",
      description = "Check the elegibility of a customer by their ID.",
      responses =
          [
              ApiResponse(
                  responseCode = "200",
                  description = "Customer elegibility status",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema = Schema(implementation = Boolean::class))]),
              ApiResponse(
                  responseCode = "404",
                  description = "Customer not found",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema = Schema(implementation = String::class))])])
  @PreAuthorize("hasAnyRole('reservation_service', 'service_account_reservation')")
  @GetMapping("{userId}/eligibility/", "{userId}/eligibility")
  @ResponseStatus(HttpStatus.OK)
  fun isCustomerEligible(@PathVariable userId: Long): ResponseEntity<Map<String, Boolean>> {
    val user = userService.getCustomerById(userId)

    if (user.score!! >= 6) {
      return ResponseEntity.ok(mapOf("isEligible" to true))
    }
    return ResponseEntity.ok(mapOf("isEligible" to false))
  }

  /**
   * PATCH endpoint to update the score of a customer.
   *
   * @param userId The ID of the customer to update.
   *
   * #body score The new score to set for the customer.
   */
  @Operation(
      summary = "Update customer score",
      description = "Update the score of a customer by their ID.",
      responses =
          [
              ApiResponse(
                  responseCode = "200",
                  description = "Customer score updated successfully",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema = Schema(implementation = Int::class))]),
              ApiResponse(
                  responseCode = "404",
                  description = "Customer not found",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema = Schema(implementation = String::class))])])
  @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'FLEET_MANAGER')")
  @PatchMapping("{userId}/score/", "{userId}/score")
  @ResponseStatus(HttpStatus.OK)
  fun updateCustomerScore(
      @PathVariable userId: Long,
      @RequestBody score: Int
  ): ResponseEntity<Map<String, Int?>> {
    val updatedUser = userService.updateCustomerScore(userId, score)
    return ResponseEntity.ok(mapOf("score" to updatedUser.score))
  }

  /**
   * GET rental history of a customer by their ID.
   *
   * @param userId The ID of the customer to retrieve.
   * @return an array of the Reservation DTOs representing the rental history of the customer.
   * @throws UserNotFoundException if the customer with the specified ID does not exist.
   */
  @Operation(
      summary = "Get customer rental history",
      description = "Get the rental history of a customer by their ID.",
      responses =
          [
              ApiResponse(
                  responseCode = "200",
                  description = "Customer rental history",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              array =
                                  ArraySchema(
                                      schema = Schema(implementation = ReservationDTO::class)))]),
              ApiResponse(
                  responseCode = "404",
                  description = "Customer not found",
                  content =
                      [
                          Content(
                              mediaType = "application/json",
                              schema = Schema(implementation = String::class))])])
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'MANAGER', 'FLEET_MANAGER')")
  @GetMapping("{userId}/rental-history/", "{userId}/rental-history")
  @ResponseStatus(HttpStatus.OK)
  fun getCustomerRentalHistory(@PathVariable userId: Long): Page<ReservationDTO> {
    return restTemplate
        .getForEntity<Page<ReservationDTO>>("/api/v1/reservations?userId=$userId")
        .body ?: throw UserNotFoundException("Customer with ID $userId not found")
  }
}
