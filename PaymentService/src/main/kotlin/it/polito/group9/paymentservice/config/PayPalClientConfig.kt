package it.polito.group9.paymentservice.config

import com.paypal.core.PayPalEnvironment
import com.paypal.core.PayPalHttpClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class PayPalClientConfig {
  @Bean
  fun payPalClient(props: PayPalProperties): PayPalHttpClient {
    val env =
        when (props.environment) {
          "sandbox" -> PayPalEnvironment.Sandbox(props.clientId, props.clientSecret)
          else -> PayPalEnvironment.Live(props.clientId, props.clientSecret)
        }
    return PayPalHttpClient(env)
  }
}
