package it.polito.group9.reservationservice.exceptions
/**
 * Custom exception to be thrown when a car model is not found.
 *
 * @param message The detail message.
 */
class CarModelNotFoundException(message: String) : RuntimeException(message)
