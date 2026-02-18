package it.polito.group9.reservationservice.exceptions

/**
 * Exception thrown when a reservation is not found in the system.
 *
 * @param message The detail message explaining the reason for the exception.
 */
class ReservationNotFoundException(message: String) : RuntimeException(message)
