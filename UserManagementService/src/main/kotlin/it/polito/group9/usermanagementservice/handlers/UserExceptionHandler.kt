package it.polito.group9.usermanagementservice.handlers

import it.polito.group9.usermanagementservice.exceptions.UserAlreadyExistException
import it.polito.group9.usermanagementservice.exceptions.UserNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

/**
 * Exception handler for User-related exceptions. This class handles exceptions related to user
 * models and returns appropriate HTTP responses.
 */
@RestControllerAdvice
class UserExceptionHandler : ResponseEntityExceptionHandler() {

  @ExceptionHandler(UserNotFoundException::class)
  fun handleNotFound(e: UserNotFoundException): ProblemDetail =
      ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message!!)

  @ExceptionHandler(UserAlreadyExistException::class)
  fun handleDuplicate(e: UserAlreadyExistException): ProblemDetail =
      ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.message!!)
}
