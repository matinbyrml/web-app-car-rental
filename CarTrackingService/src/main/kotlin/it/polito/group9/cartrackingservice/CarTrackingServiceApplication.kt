package it.polito.group9.cartrackingservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class CarTrackingServiceApplication

fun main(args: Array<String>) {
  runApplication<CarTrackingServiceApplication>(*args)
}
