package it.polito.group9.reservationservice.services.impl

import it.polito.group9.reservationservice.config.AccessTokenConfigProperties
import it.polito.group9.reservationservice.services.KeycloakService
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

@Service
class KeycloakServiceImpl(private val accessTokenConfig: AccessTokenConfigProperties) :
    KeycloakService {
  override fun getAccessToken(restTemplate: RestTemplate): String {
    val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_FORM_URLENCODED }
    val body =
        LinkedMultiValueMap<String, String>().apply {
          add("grant_type", "client_credentials")
          add("client_id", accessTokenConfig.clientId)
          add("client_secret", accessTokenConfig.clientSecret)
        }
    val request = HttpEntity(body, headers)
    val response = restTemplate.postForEntity(accessTokenConfig.tokenUri, request, Map::class.java)
    return response.body?.get("access_token") as String
  }
}
