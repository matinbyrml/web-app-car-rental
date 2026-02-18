package it.polito.group9.usermanagementservice.exceptions

/**
 * Custom exception to be thrown when a duplicate user is found.
 *
 * @param message The detail message.
 */
class UserAlreadyExistException(message: String) : RuntimeException(message)
