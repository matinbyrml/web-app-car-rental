package it.polito.group9.usermanagementservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class UserManagementServiceApplication

fun main(args: Array<String>) {
  runApplication<UserManagementServiceApplication>(*args)
}
