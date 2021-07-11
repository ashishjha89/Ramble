package com.ramble.token.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@ConstructorBinding
@ConfigurationProperties(prefix = "jwt")
data class JwtConfigProperties(val signingKey: String)

@Configuration
@EnableConfigurationProperties(JwtConfigProperties::class)
class JwtTokenConfig(val jwtConfigProperties: JwtConfigProperties)