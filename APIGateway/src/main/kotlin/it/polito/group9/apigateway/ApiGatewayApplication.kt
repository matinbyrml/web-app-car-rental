package it.polito.group9.apigateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class ApiGatewayApplication

fun main(args: Array<String>) {
  runApplication<ApiGatewayApplication>(*args)
}
