import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

data class KeyCloakUser(val username: String, val password: String)

val CUSTOMER = KeyCloakUser("test.customer@email.com", "secret")
val STAFF = KeyCloakUser("test.staff@email.com", "secret")
val MANAGER = KeyCloakUser("test.manager@email.com", "secret")
val FLEET_MANAGER = KeyCloakUser("test.fleet.manager@email.com", "secret")

object TestUtils {
  fun getHeaders(user: KeyCloakUser, getAccessToken: (KeyCloakUser) -> String): HttpHeaders {
    val headers = HttpHeaders()
    headers.contentType = MediaType.APPLICATION_JSON
    headers.add("Authorization", getAccessToken(user))
    return headers
  }

  fun getUser(user: String): KeyCloakUser {
    return when (user) {
      "CUSTOMER" -> CUSTOMER
      "STAFF" -> STAFF
      "MANAGER" -> MANAGER
      "FLEET_MANAGER" -> FLEET_MANAGER
      else -> throw IllegalArgumentException("Unknown user type: $user")
    }
  }
}
