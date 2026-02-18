package it.polito.group9.paymentservice.config

import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

@Configuration
class KafkaProducerConfig(private val kafkaProperties: KafkaProperties) {
  @Bean
  fun producerFactory(): ProducerFactory<String, Any> =
      DefaultKafkaProducerFactory(kafkaProperties.buildProducerProperties())

  @Bean fun kafkaTemplate(): KafkaTemplate<String, Any> = KafkaTemplate(producerFactory())
}
