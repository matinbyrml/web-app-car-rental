package it.polito.group9.paymentservice.config

import it.polito.group9.paymentservice.kafka.OutboxRecord
import it.polito.group9.paymentservice.kafka.ReservationCreatedEvent
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.support.serializer.JsonDeserializer

@EnableKafka
@Configuration
class KafkaConsumerConfig(private val kafkaProperties: KafkaProperties) {

  @Bean
  fun reservationConsumerFactory(): ConsumerFactory<String, OutboxRecord<ReservationCreatedEvent>> {
    val props = kafkaProperties.buildConsumerProperties().toMutableMap()
    props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
    props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = JsonDeserializer::class.java
    props[ConsumerConfig.GROUP_ID_CONFIG] = "payment-service"
    val keyDeserializer: Deserializer<String> = StringDeserializer()
    val valueDeserializer: Deserializer<OutboxRecord<ReservationCreatedEvent>> =
        JsonDeserializer<OutboxRecord<ReservationCreatedEvent>>(OutboxRecord::class.java).apply {
          addTrustedPackages("*")
        }
    return DefaultKafkaConsumerFactory(props, keyDeserializer, valueDeserializer)
  }

  @Bean(name = ["reservationListenerContainerFactory"])
  fun reservationListenerContainerFactory():
      ConcurrentKafkaListenerContainerFactory<String, OutboxRecord<ReservationCreatedEvent>> {
    val factory =
        ConcurrentKafkaListenerContainerFactory<String, OutboxRecord<ReservationCreatedEvent>>()
    factory.consumerFactory = reservationConsumerFactory()
    factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
    return factory
  }
}
