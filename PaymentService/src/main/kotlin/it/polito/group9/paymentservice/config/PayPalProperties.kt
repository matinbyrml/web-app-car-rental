package it.polito.group9.paymentservice.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration @EnableConfigurationProperties(PayPalProperties::class) class PayPalPropertiesConfig

@ConfigurationProperties(prefix = "paypal")
data class PayPalProperties(
    var clientId: String = "",
    var clientSecret: String = "",
    var environment: String = "",
    var currency: String = "",
    var returnUrl: String = "",
    var cancelUrl: String = "",
)
