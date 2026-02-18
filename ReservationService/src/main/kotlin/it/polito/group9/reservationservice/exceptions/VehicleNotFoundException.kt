package it.polito.group9.reservationservice.exceptions

/**
 * Exception thrown when a requested vehicle is not found in the system.
 *
 * @param message The detail message explaining the reason for the exception.
 */
class VehicleNotFoundException(message: String) : RuntimeException(message)
