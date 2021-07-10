package com.ramble.email.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@ConstructorBinding
@ConfigurationProperties(prefix = "email-sender")
data class EmailConfigProperties(val enableVault: Boolean)

@Configuration
@EnableConfigurationProperties(EmailConfigProperties::class)
class EmailSenderConfig(emailConfigProperties: EmailConfigProperties) {

    init {
        Components.enableVault = emailConfigProperties.enableVault
    }
}