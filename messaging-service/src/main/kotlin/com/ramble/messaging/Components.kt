package com.ramble.messaging

import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class Components {

    @Bean
    @LoadBalanced
    fun restTemplate(): RestTemplate = RestTemplate()
}