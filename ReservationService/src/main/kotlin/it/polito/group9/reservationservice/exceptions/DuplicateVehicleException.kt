package it.polito.group9.reservationservice.exceptions

/**
 * Exception thrown when attempting to create a vehicle that already exists in the system.
 *
 * @param message The detail message explaining the reason for the exception.
 */
class DuplicateVehicleException(message: String) : RuntimeException(message)
