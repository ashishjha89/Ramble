package com.ramble.messaging

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient

@SpringBootApplication
@EnableEurekaClient
class MessagingServiceApplication

fun main(args: Array<String>) {
    runApplication<MessagingServiceApplication>(*args)
}