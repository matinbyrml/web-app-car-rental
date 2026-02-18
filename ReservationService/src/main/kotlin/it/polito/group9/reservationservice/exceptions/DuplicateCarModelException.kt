package it.polito.group9.reservationservice.exceptions
/**
 * Custom exception to be thrown when a duplicate car model is found.
 *
 * @param message The detail message.
 */
class DuplicateCarModelException(message: String) : RuntimeException(message)
