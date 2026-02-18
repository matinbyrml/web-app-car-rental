package it.polito.group9.reservationservice.handlers

import it.polito.group9.reservationservice.exceptions.UserNotEligibleException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class UserExceptionHandler : ResponseEntityExceptionHandler() {
  @ExceptionHandler(UserNotEligibleException::class)
  fun handleNotEligible(e: UserNotEligibleException): ProblemDetail =
      ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.message!!)
}
