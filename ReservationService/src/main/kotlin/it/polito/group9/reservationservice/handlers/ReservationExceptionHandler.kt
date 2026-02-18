package it.polito.group9.reservationservice.handlers

import it.polito.group9.reservationservice.exceptions.ReservationNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

/**
 * Global exception handler for the Reservation Service. Handles exceptions thrown by the
 * application and provides appropriate HTTP responses.
 */
@RestControllerAdvice
class ReservationExceptionHandler : ResponseEntityExceptionHandler() {

  /**
   * Handles the `ReservationNotFoundException` and returns a `ProblemDetail` object with a 404 Not
   * Found status and the exception message as the detail.
   *
   * @param e The `ReservationNotFoundException` instance.
   * @return A `ProblemDetail` object containing the HTTP status and error details.
   */
  @ExceptionHandler(ReservationNotFoundException::class)
  fun handleNotFound(e: ReservationNotFoundException): ProblemDetail =
      ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message!!)
}
