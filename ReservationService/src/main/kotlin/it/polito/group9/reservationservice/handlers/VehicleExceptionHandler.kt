package it.polito.group9.reservationservice.handlers

import it.polito.group9.reservationservice.exceptions.DuplicateVehicleException
import it.polito.group9.reservationservice.exceptions.VehicleNotAvailableException
import it.polito.group9.reservationservice.exceptions.VehicleNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

/**
 * Exception handler for vehicle-related exceptions. This class provides centralized exception
 * handling for exceptions related to vehicles and returns appropriate HTTP responses with relevant
 * status codes and error details.
 */
@RestControllerAdvice
class VehicleExceptionHandler : ResponseEntityExceptionHandler() {

  /**
   * Handles the VehicleNotFoundException. This method is invoked when a VehicleNotFoundException is
   * thrown, returning a 404 Not Found status with the exception message as the error detail.
   *
   * @param e The VehicleNotFoundException instance.
   * @return A ProblemDetail object containing the HTTP status and error detail.
   */
  @ExceptionHandler(VehicleNotFoundException::class)
  fun handleNotFound(e: VehicleNotFoundException): ProblemDetail =
      ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message!!)

  /**
   * Handles the DuplicateVehicleException. This method is invoked when a DuplicateVehicleException
   * is thrown, returning a 409 Conflict status with the exception message as the error detail.
   *
   * @param e The DuplicateVehicleException instance.
   * @return A ProblemDetail object containing the HTTP status and error detail.
   */
  @ExceptionHandler(DuplicateVehicleException::class)
  fun handleDuplicate(e: DuplicateVehicleException): ProblemDetail =
      ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.message!!)

  @ExceptionHandler(VehicleNotAvailableException::class)
  fun handleNotAvailable(e: VehicleNotAvailableException): ProblemDetail =
      ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.message!!)
}
