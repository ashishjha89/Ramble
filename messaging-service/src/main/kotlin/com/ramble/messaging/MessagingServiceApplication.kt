package com.ramble.messaging

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MessagingServiceApplication

fun main(args: Array<String>) {
	runApplication<MessagingServiceApplication>(*args)
}
