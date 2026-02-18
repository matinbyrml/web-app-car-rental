package it.polito.group9.reservationservice.config

import java.util.stream.Collectors
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
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
          it.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/models/**")
              .permitAll()
          it.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/vehicles/**")
              .permitAll()
          it.requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/vehicles/available")
              .permitAll()
          it.requestMatchers("/actuator/**").permitAll()
          it.anyRequest().authenticated()
        }
        .oauth2ResourceServer {
          it.jwt { jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()) }
        }
        .build()
  }
}
