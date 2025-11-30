package me.bossm0n5t3r.sse.configuration

import org.springframework.boot.kafka.autoconfigure.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.kafka.receiver.ReceiverOptions
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions

@Configuration
class ReactorKafkaConfiguration(
    private val kafkaProperties: KafkaProperties,
) {
    @Bean
    fun senderOptions(): SenderOptions<String, String> {
        val producerProps = kafkaProperties.buildProducerProperties()
        return SenderOptions.create(producerProps)
    }

    @Bean
    fun kafkaSender(senderOptions: SenderOptions<String, String>): KafkaSender<String, String> = KafkaSender.create(senderOptions)

    @Bean
    fun receiverOptions(): ReceiverOptions<String, String> {
        val consumerProps = kafkaProperties.buildConsumerProperties()
        // group.id 는 streamFrom 안에서 override 할 거라 여기선 기본값만
        return ReceiverOptions.create(consumerProps)
    }
}
