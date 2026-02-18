package it.polito.group9.reservationservice.handlers

import it.polito.group9.reservationservice.exceptions.CarModelNotFoundException
import it.polito.group9.reservationservice.exceptions.DuplicateCarModelException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

/**
 * Exception handler for CarModel-related exceptions. This class handles exceptions related to car
 * models and returns appropriate HTTP responses.
 */
@RestControllerAdvice
class CarModelExceptionHandler : ResponseEntityExceptionHandler() {

  @ExceptionHandler(CarModelNotFoundException::class)
  fun handleNotFound(e: CarModelNotFoundException): ProblemDetail =
      ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message!!)

  @ExceptionHandler(DuplicateCarModelException::class)
  fun handleDuplicate(e: DuplicateCarModelException): ProblemDetail =
      ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.message!!)
}
