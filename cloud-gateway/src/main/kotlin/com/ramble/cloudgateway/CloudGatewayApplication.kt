package com.ramble.cloudgateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient

@SpringBootApplication
@EnableEurekaClient
class CloudGatewayApplication

fun main(args: Array<String>) {
	runApplication<CloudGatewayApplication>(*args)
}
