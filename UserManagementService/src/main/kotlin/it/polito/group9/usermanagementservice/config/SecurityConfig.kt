package it.polito.group9.usermanagementservice.config

import java.util.stream.Collectors
import org.keycloak.OAuth2Constants.CLIENT_CREDENTIALS
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtDecoders
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain

class KeycloakRealmRoleConverter : Converter<Jwt?, MutableCollection<GrantedAuthority?>?> {
  override fun convert(jwt: Jwt): MutableCollection<GrantedAuthority?>? {
    val realmAccess = jwt.getClaimAsMap("realm_access")
    if (realmAccess == null || realmAccess.isEmpty()) {
      return mutableListOf()
    }
    val roles = realmAccess["roles"] as MutableList<String?>

    return roles
        .stream()
        .map { role: String? -> SimpleGrantedAuthority("ROLE_$role") }
        // Spring derive roles from Authorities with "ROLE_" prefix
        .collect(Collectors.toList())
  }
}

@EnableWebSecurity
@Configuration
@EnableMethodSecurity
class SecurityConfig {

  private fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
    val converter = JwtAuthenticationConverter()
    converter.setJwtGrantedAuthoritiesConverter(KeycloakRealmRoleConverter())
    return converter
  }

  @Bean
  fun jwtDecoder(accessTokenConfig: AccessTokenConfigProperties): JwtDecoder {
    return JwtDecoders.fromIssuerLocation(accessTokenConfig.realmUrl)
  }

  @Bean
  fun securityFilterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
    return httpSecurity
        .authorizeHttpRequests {
          it.requestMatchers(HttpMethod.POST, "/api/v1/users/", "/api/v1/users")
              .permitAll() // Allow user registration without authentication
          it.requestMatchers("/actuator/**")
              .permitAll() // Allow Prometheus/Actuator endpoints
              .anyRequest()
              .authenticated() // All other requests require authentication
        }
        .oauth2ResourceServer {
          it.jwt { jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()) }
        }
        .csrf { csrf ->
          csrf.ignoringRequestMatchers("/api/v1/users/**")
          // NOTE: do NOT ignore /logout anymore; we want CSRF on POST /logout
        }
        .build()
  }
}

@Configuration
class KeycloakClientConfig(
    @Value("\${keycloak.client-secret}") private val secretKey: String,
    @Value("\${keycloak.client-id}") private val clientId: String,
    @Value("\${keycloak.auth-server-url}") private val authUrl: String,
    @Value("\${keycloak.realm}") private val realm: String
) {

  @Bean
  fun keycloak(): Keycloak {
    return KeycloakBuilder.builder()
        .grantType(CLIENT_CREDENTIALS)
        .serverUrl(authUrl)
        .realm(realm)
        .clientId(clientId)
        .clientSecret(secretKey)
        .build()
  }
}
