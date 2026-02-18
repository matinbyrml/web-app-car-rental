package it.polito.group9.apigateway.controllers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.net.URI
import java.util.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.bind.annotation.*

@RestController
class AuthController {

  private val logger = LoggerFactory.getLogger(AuthController::class.java)

  /*───────────────────────────────────────────────────────────*
   *  1 · PUBLIC-FACING “UTILITY” ENDPOINTS                    *
   *───────────────────────────────────────────────────────────*/

  @GetMapping("/") fun root(resp: HttpServletResponse) = resp.sendRedirect("http://localhost:4173/")

  @GetMapping("/me")
  fun me(auth: Authentication?, csrf: CsrfToken): Map<String, Any?> {
    logger.info("Received /me request. Auth={}", auth)

    if (auth?.principal is OidcUser) {
      val user = auth.principal as OidcUser
      logger.info(
          "OidcUser detected. username={}, email={}, authorities={}",
          user.preferredUsername,
          user.email,
          user.authorities)

      // Try to extract raw access token
      val accessToken =
          (auth as? OAuth2AuthenticationToken)?.principal?.attributes?.get("access_token")
              as? String

      logger.info("Extracted access token: {}", accessToken?.take(20)?.plus("...") ?: "null")

      val roles =
          if (accessToken != null) {
            try {
              val parts = accessToken.split(".")
              val payload = String(Base64.getUrlDecoder().decode(parts[1]))
              logger.debug("Decoded JWT payload: {}", payload)

              val claims = jacksonObjectMapper().readValue(payload, Map::class.java)
              logger.debug("Parsed claims: {}", claims)

              val realmAccess = claims["realm_access"] as? Map<*, *>
              val allRoles =
                  (realmAccess?.get("roles") as? Collection<*>)?.filterIsInstance<String>()
                      ?: emptyList()

              logger.info("All roles from token: {}", allRoles)

              allRoles
                  .filter {
                    it !in listOf("offline_access", "uma_authorization") &&
                        !it.startsWith("default-roles")
                  }
                  .map { "ROLE_$it" }
            } catch (e: Exception) {
              logger.error("Error decoding access token", e)
              emptyList()
            }
          } else {
            logger.warn("No access token found in OAuth2AuthenticationToken attributes.")
            emptyList()
          }

      logger.info("Returning filtered roles: {}", roles)

      return mapOf(
          "name" to user.preferredUsername,
          "email" to user.email,
          "roles" to roles,
          "csrf" to csrf.token)
    }

    logger.warn("No OidcUser principal found, returning error.")
    return mapOf("error" to "User not authenticated", "csrf" to csrf.token)
  }

  @GetMapping("/csrf")
  fun csrf(token: CsrfToken) =
      mapOf(
          "parameterName" to token.parameterName,
          "headerName" to token.headerName,
          "token" to token.token)

  /*───────────────────────────────────────────────────────────*
   *  2 · LOGIN / LOGOUT SHORT-CUTS                           *
   *───────────────────────────────────────────────────────────*/

  @GetMapping("/serverLogin")
  fun serverLogin(): ResponseEntity<Void> =
      ResponseEntity.status(HttpStatus.FOUND)
          .location(URI.create("/oauth2/authorization/gateway-client"))
          .build()

  @PostMapping("/logout")
  fun logout(req: HttpServletRequest, resp: HttpServletResponse) {
    req.logout()
    resp.status = HttpServletResponse.SC_NO_CONTENT
  }
}
