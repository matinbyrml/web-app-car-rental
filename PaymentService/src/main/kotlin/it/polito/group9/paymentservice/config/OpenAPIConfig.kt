package it.polito.group9.paymentservice.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenAPIConfig {

  @Value("\${server.port}") private lateinit var serverPort: String

  @Bean
  fun customOpenAPI(): OpenAPI {
    return OpenAPI()
        .info(
            Info()
                .title("Payment Service API")
                .description("API for managing payment transactions")
                .version("1.0")
                .contact(Contact().name("Group 9").email("group9@example.com"))
                .license(License().name("MIT License").url("https://opensource.org/licenses/MIT")))
        .addServersItem(
            Server().url("http://localhost:$serverPort").description("Local development server"))
  }
}
