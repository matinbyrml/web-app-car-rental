package it.polito.group9.cartrackingservice.controllers

import KeyCloakUser
import dasniko.testcontainers.keycloak.KeycloakContainer
import it.polito.group9.cartrackingservice.config.AccessTokenConfigProperties
import java.net.URI
import org.apache.http.client.utils.URIBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.json.JacksonJsonParser
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = [IntegrationTest.Initializer::class])
abstract class IntegrationTest {
  @Autowired private lateinit var accessTokenConfig: AccessTokenConfigProperties

  companion object {
    @Container
    val postgres =
        PostgreSQLContainer("postgres:latest")
            .withDatabaseName("db_reservation")
            .withUsername("user_car_tracking")
            .withPassword("pass_car_tracking")

    @Container
    val keycloak = KeycloakContainer().withRealmImportFile("keycloak/alberioauto-realm-test.json")

    init {
      keycloak.start()
    }
  }

  class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    override fun initialize(context: ConfigurableApplicationContext) {
      context.environment.apply {
        System.setProperty("spring.datasource.url", postgres.jdbcUrl)
        System.setProperty("spring.datasource.username", postgres.username)
        System.setProperty("spring.datasource.password", postgres.password)
        System.setProperty("spring.flyway.url", postgres.jdbcUrl)
        System.setProperty("spring.flyway.user", postgres.username)
        System.setProperty("spring.flyway.password", postgres.password)
        System.setProperty(
            "spring.security.oauth2.client.provider.iam-name.issuer-uri",
            keycloak.authServerUrl + "/realms/test-alberioauto")
        System.setProperty(
            "keycloak.tokenUri", keycloak.authServerUrl + "/protocol/openid-connect/token")
        System.setProperty("keycloak.realmUrl", keycloak.authServerUrl + "/realms/test-alberioauto")
      }
    }
  }

  fun getAccessToken(user: KeyCloakUser): String {
    val authorizationURI: URI =
        URIBuilder(
                keycloak.authServerUrl + "/realms/test-alberioauto/protocol/openid-connect/token")
            .build()
    val formData: MultiValueMap<String, String> = LinkedMultiValueMap()
    formData.put("grant_type", listOf("password"))
    formData.put("client_id", listOf(accessTokenConfig.clientId))
    formData.put("client_secret", listOf(accessTokenConfig.clientSecret))
    formData.put("username", listOf(user.username))
    formData.put("password", listOf(user.password))

    val restTemplate = RestTemplate()
    val headers = HttpHeaders()
    headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
    val request = HttpEntity(formData, headers)
    val result: String? = restTemplate.postForObject(authorizationURI, request, String::class.java)

    val jsonParser = JacksonJsonParser()
    return "Bearer " + jsonParser.parseMap(result)["access_token"].toString()
  }
}
