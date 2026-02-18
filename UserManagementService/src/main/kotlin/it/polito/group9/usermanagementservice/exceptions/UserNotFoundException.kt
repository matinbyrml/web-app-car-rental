package it.polito.group9.usermanagementservice.exceptions

/**
 * Custom exception to be thrown when a user is not found.
 *
 * @param message The detail message.
 */
class UserNotFoundException(message: String) : RuntimeException(message)
