package it.polito.group9.reservationservice.config

import it.polito.group9.reservationservice.kafka.OutboxRecord
import it.polito.group9.reservationservice.kafka.PaymentCompletedEvent
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
  fun consumerFactory(): ConsumerFactory<String, Any> {
    val props = kafkaProperties.buildConsumerProperties()
    props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
    props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
    props[ConsumerConfig.GROUP_ID_CONFIG] = "reservation-service"
    return DefaultKafkaConsumerFactory(props)
  }

  @Bean
  fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, Any> {
    val factory = ConcurrentKafkaListenerContainerFactory<String, Any>()
    factory.consumerFactory = consumerFactory()
    factory.containerProperties.ackMode =
        ContainerProperties.AckMode.MANUAL // Enable manual acknowledgment
    return factory
  }

  @Bean
  fun paymentConsumerFactory(): ConsumerFactory<String, OutboxRecord<PaymentCompletedEvent>> {
    val props = kafkaProperties.buildConsumerProperties().toMutableMap()
    props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
    props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = JsonDeserializer::class.java
    props[ConsumerConfig.GROUP_ID_CONFIG] = "reservation-service"
    val keyDeserializer: Deserializer<String> = StringDeserializer()
    val valueDeserializer: Deserializer<OutboxRecord<PaymentCompletedEvent>> =
        JsonDeserializer<OutboxRecord<PaymentCompletedEvent>>(OutboxRecord::class.java).apply {
          addTrustedPackages("*")
        }
    return DefaultKafkaConsumerFactory(props, keyDeserializer, valueDeserializer)
  }

  @Bean(name = ["paymentListenerContainerFactory"])
  fun paymentListenerContainerFactory():
      ConcurrentKafkaListenerContainerFactory<String, OutboxRecord<PaymentCompletedEvent>> {
    val factory =
        ConcurrentKafkaListenerContainerFactory<String, OutboxRecord<PaymentCompletedEvent>>()
    factory.consumerFactory = paymentConsumerFactory()
    factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
    return factory
  }
}
