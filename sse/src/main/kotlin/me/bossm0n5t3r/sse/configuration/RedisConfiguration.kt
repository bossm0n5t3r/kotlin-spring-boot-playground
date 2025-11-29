package me.bossm0n5t3r.sse.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate

@Configuration
class RedisConfiguration {
    @Bean
    @ConfigurationProperties(prefix = "spring.data.redis")
    fun redisConnectionFactory(): RedisConnectionFactory = LettuceConnectionFactory()

    @Bean
    fun stringRedisTemplate(connectionFactory: RedisConnectionFactory): StringRedisTemplate = StringRedisTemplate(connectionFactory)
}
