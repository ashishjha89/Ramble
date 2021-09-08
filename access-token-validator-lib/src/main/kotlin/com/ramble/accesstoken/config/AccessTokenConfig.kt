package com.ramble.accesstoken.config

import com.ramble.accesstoken.AccessTokenValidatorService
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Suppress("unused")
@ComponentScan(basePackageClasses = [AccessTokenValidatorService::class])
@Configuration
@PropertySource(value = ["classpath:accesstoken-application.yml"], factory = YamlPropertySourceFactory::class)
class AccessTokenConfig {}

@ConstructorBinding
@ConfigurationProperties(prefix = "access-token-validator")
data class AccessTokenConfigProperties(val signingKeyAccessToken: String)

@Configuration
@EnableConfigurationProperties(AccessTokenConfigProperties::class)
class AccessTokenValidatorConfig(val accessTokenConfigProperties: AccessTokenConfigProperties)
