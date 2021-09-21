package com.ramble.messaging

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories

@SpringBootApplication(scanBasePackages = ["com.ramble"])
@EnableEurekaClient
@EnableRedisRepositories(basePackages = ["com.ramble.accesstoken"])
@EntityScan(value = ["com.ramble.accesstoken.*"])
class MessagingServiceApplication

fun main(args: Array<String>) {
    runApplication<MessagingServiceApplication>(*args)
}