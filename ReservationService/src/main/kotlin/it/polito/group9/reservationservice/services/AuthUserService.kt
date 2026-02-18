package it.polito.group9.reservationservice.services

import it.polito.group9.reservationservice.dtos.UserDTO
import org.springframework.security.oauth2.jwt.Jwt

/**
 * Servizio per ottenere informazioni sull'utente autenticato a partire dal JWT e per recuperare i
 * dettagli utente per username.
 */
interface AuthUserService {
  fun getCurrentUser(jwt: Jwt): UserDTO

  fun getUserByUsername(username: String): UserDTO
}
