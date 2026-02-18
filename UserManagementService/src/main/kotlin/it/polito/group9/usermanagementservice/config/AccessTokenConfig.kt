package it.polito.group9.usermanagementservice.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@ConfigurationProperties(prefix = "keycloak")
data class AccessTokenConfigProperties(
    val clientId: String,
    val clientSecret: String,
    val tokenUri: String,
    val realmUrl: String
)

@Configuration
@EnableConfigurationProperties(AccessTokenConfigProperties::class)
class AccessTokenConfig
