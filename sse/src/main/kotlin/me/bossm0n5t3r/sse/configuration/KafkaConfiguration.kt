package me.bossm0n5t3r.sse.configuration

import org.springframework.boot.kafka.autoconfigure.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

@Configuration
@EnableKafka
class KafkaConfiguration(
    private val kafkaProperties: KafkaProperties,
) {
    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String, String>): KafkaTemplate<String, String> = KafkaTemplate(producerFactory)

    @Bean
    fun producerFactory(): ProducerFactory<String, String> = DefaultKafkaProducerFactory(kafkaProperties.buildProducerProperties())

    @Bean
    fun consumerFactory(): ConsumerFactory<String, String> = DefaultKafkaConsumerFactory(kafkaProperties.buildConsumerProperties())
}
