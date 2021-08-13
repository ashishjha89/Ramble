package com.ramble.identity

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories

@SpringBootApplication(scanBasePackages = ["com.ramble"])
@EnableJpaRepositories(basePackages = ["com.ramble.token"])
@EnableRedisRepositories(basePackages = ["com.ramble.token"])
@EntityScan(value = ["com.ramble.token.*"])
class IdentityServiceApplication

fun main(args: Array<String>) {
    runApplication<IdentityServiceApplication>(*args)
}
