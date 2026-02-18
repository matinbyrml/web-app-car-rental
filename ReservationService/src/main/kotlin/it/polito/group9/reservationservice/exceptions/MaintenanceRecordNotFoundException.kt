package it.polito.group9.reservationservice.exceptions

/**
 * Exception thrown when a maintenance record is not found.
 *
 * This exception is used to indicate that a requested maintenance record does not exist in the
 * system. It extends the RuntimeException class.
 *
 * @param message The detail message explaining the reason for the exception.
 */
class MaintenanceRecordNotFoundException(message: String) : RuntimeException(message)
