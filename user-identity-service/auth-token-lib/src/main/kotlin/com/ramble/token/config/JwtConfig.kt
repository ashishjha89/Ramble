package com.ramble.token.config

import com.ramble.accesstoken.config.YamlPropertySourceFactory
import com.ramble.token.AuthTokensService
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Suppress("unused")
@ComponentScan(basePackageClasses = [AuthTokensService::class])
@Configuration
@PropertySource(value = ["classpath:authtoken-application.yml"], factory = YamlPropertySourceFactory::class)
class TokenConfig

@ConstructorBinding
@ConfigurationProperties(prefix = "jwt")
data class JwtConfigProperties(val signingKeyRefreshToken: String, val signingKeyRegistrationToken: String)

@Configuration
@EnableConfigurationProperties(JwtConfigProperties::class)
class JwtTokenConfig(val jwtConfigProperties: JwtConfigProperties)