package it.polito.group9.reservationservice.handlers

import it.polito.group9.reservationservice.exceptions.MaintenanceRecordNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

/**
 * Global exception handler for maintenance-related exceptions.
 *
 * This class provides centralized exception handling for the application, specifically for
 * exceptions related to maintenance records.
 */
@RestControllerAdvice
class MaintenanceExceptionHandler : ResponseEntityExceptionHandler() {

  /**
   * Handles MaintenanceRecordNotFoundException and returns a ProblemDetail response.
   *
   * @param e The MaintenanceRecordNotFoundException to handle.
   * @return A ProblemDetail object containing the HTTP status and error details.
   */
  @ExceptionHandler(MaintenanceRecordNotFoundException::class)
  fun handleNotFound(e: MaintenanceRecordNotFoundException): ProblemDetail =
      ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message!!)
}
