package it.polito.group9.reservationservice.services

import org.springframework.web.client.RestTemplate

interface KeycloakService {
  fun getAccessToken(restTemplate: RestTemplate): String
}
