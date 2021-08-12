package com.ramble.token.config

import org.springframework.context.annotation.Bean
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisClientComponentBuilder {

    @Bean
    fun jedisConnectionFactory(): JedisConnectionFactory = JedisConnectionFactory()

    @Bean
    fun redisTemplate(): RedisTemplate<String, Any> =
        RedisTemplate<String, Any>().apply {
            setConnectionFactory(jedisConnectionFactory())
        }
}