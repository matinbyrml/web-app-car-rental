// src/main/kotlin/it/polito/group9/apigateway/config/SecurityConfig.kt
package it.polito.group9.apigateway.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.core.oidc.OidcUserInfo
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.csrf.CookieCsrfTokenRepository

@EnableWebSecurity
@Configuration
class SecurityConfig(private val crr: ClientRegistrationRepository) {
  private val frontUrl = "http://localhost:4173/"

  private fun oidcLogoutHandler() =
      OidcClientInitiatedLogoutSuccessHandler(crr).apply {
        // After Keycloak ends the SSO session, go back to FE (?logout flag)
        setPostLogoutRedirectUri("${frontUrl}?logout")
      }

  @Bean
  fun filterChain(http: HttpSecurity): SecurityFilterChain =
      http
          /* 1 · Public routes vs secured ones */
          .authorizeHttpRequests { auth ->
            auth
                .requestMatchers(HttpMethod.POST, "/api/v1/users/**")
                .permitAll()
                .requestMatchers(
                    "/", // root → redirect to /ui (AuthController)
                    "/ui/**", // SPA via gateway proxy
                    "/assets/**", // Vite chunks
                    "/me",
                    "/csrf", // who-am-I & CSRF bootstrap
                    "/api/v1/models/**",
                    "/api/v1/vehicles/**",
                    "/api/v1/vehicles/available/**",
                    "/actuator/**") // <--- Consenti actuator
                .permitAll()
                .anyRequest()
                .authenticated()
          }

          /* 2 · OIDC login (Keycloak) */
          .oauth2Login { login ->
            login.defaultSuccessUrl(frontUrl, true).userInfoEndpoint {
              it.oidcUserService(customOidcUserService())
            }
          }

          /* 3 · Logout (POST /logout → RP-initiated logout to Keycloak) */
          .logout { logout ->
            logout
                .logoutUrl("/logout") // <-- POST (matches your Navbar hidden form)
                .logoutSuccessHandler(oidcLogoutHandler())
          }

          /* 4 · CSRF (token in cookie for FE to read and send as form param) */
          .csrf { csrf ->
            csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            csrf.ignoringRequestMatchers("/api/v1/users/**")
            // NOTE: do NOT ignore /logout anymore; we want CSRF on POST /logout
          }
          .build()

  /**
   * Custom OidcUserService: enriches OidcUser.attributes with the access_token, so controllers can
   * decode realm roles if you still need it.
   */
  private fun customOidcUserService(): OidcUserService {
    val delegate = OidcUserService()

    return OidcUserService().apply {
      setOidcUserMapper { userRequest, userInfo: OidcUserInfo? ->
        val oidcUser: OidcUser = delegate.loadUser(userRequest)

        // Build standard DefaultOidcUser
        val baseUser =
            DefaultOidcUser(
                oidcUser.authorities, userRequest.idToken, userInfo, "preferred_username")

        // Clone attributes and inject access_token
        val enrichedAttrs = HashMap(baseUser.attributes)
        enrichedAttrs["access_token"] = userRequest.accessToken.tokenValue

        // Wrap into an OidcUser that overrides attributes
        object : OidcUser by baseUser {
          override fun getAttributes(): Map<String, Any> = enrichedAttrs
        }
      }
    }
  }
}
