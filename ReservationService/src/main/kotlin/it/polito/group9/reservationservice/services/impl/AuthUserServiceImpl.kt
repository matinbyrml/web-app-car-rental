package it.polito.group9.reservationservice.services.impl

import it.polito.group9.reservationservice.dtos.UserDTO
import it.polito.group9.reservationservice.services.AuthUserService
import it.polito.group9.reservationservice.services.KeycloakService
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class AuthUserServiceImpl(
    private val restTemplate: RestTemplate,
    private val keycloakService: KeycloakService,
) : AuthUserService {

  private val logger = LoggerFactory.getLogger(AuthUserServiceImpl::class.java)

  override fun getCurrentUser(jwt: Jwt): UserDTO {
    val username = jwt.getClaim<String>("preferred_username")
    logger.info("Resolving current user from JWT username={}.", username)
    return getUserByUsername(username)
  }

  override fun getUserByUsername(username: String): UserDTO {
    return restTemplate
        .exchange(
            "http://localhost:8081/api/v1/users/$username",
            HttpMethod.GET,
            HttpEntity<String>(getAccessTokenHeader()),
            object : ParameterizedTypeReference<UserDTO>() {})
        .body ?: throw RuntimeException("User not found for username: $username")
  }

  private fun getAccessTokenHeader() =
      HttpHeaders().apply { setBearerAuth(keycloakService.getAccessToken(restTemplate)) }
}
