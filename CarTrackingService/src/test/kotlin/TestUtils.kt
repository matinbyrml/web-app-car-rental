import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

val CUSTOMER = KeyCloakUser("test.customer@email.com", "secret")
val STAFF = KeyCloakUser("test.staff@email.com", "secret")
val MANAGER = KeyCloakUser("test.manager@email.com", "secret")
val CAR = KeyCloakUser("test.car@email.com", "secret")
val FLEET_MANAGER = KeyCloakUser("test.fleet.manager@email.com", "secret")

data class KeyCloakUser(val username: String, val password: String)

object TestUtils {
  fun getHeaders(user: KeyCloakUser, getAccessToken: (KeyCloakUser) -> String): HttpHeaders {
    val headers = HttpHeaders()
    headers.contentType = MediaType.APPLICATION_JSON
    headers.add("Authorization", getAccessToken(user))
    return headers
  }
}
