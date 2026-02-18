package it.polito.group9.apigateway.controllers

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class TestAuthenticationController {
  private val logger = LoggerFactory.getLogger(TestAuthenticationController::class.java)

  @GetMapping("/test")
  fun testAuthentication(): String {
    logger.info("Test authentication endpoint called")
    return "Authentication is working!"
  }
}
