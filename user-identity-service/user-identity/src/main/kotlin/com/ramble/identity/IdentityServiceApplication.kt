package com.ramble.identity

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories

@SpringBootApplication(scanBasePackages = ["com.ramble"])
@EnableEurekaClient
@EnableRedisRepositories(basePackages = ["com.ramble.accesstoken"])
@EnableJpaRepositories(basePackages = ["com.ramble.token", "com.ramble.identity"])
@EntityScan(value = ["com.ramble.token.*", "com.ramble.accesstoken.*", "com.ramble.identity.*"])
class IdentityServiceApplication

fun main(args: Array<String>) {
    runApplication<IdentityServiceApplication>(*args)
}
