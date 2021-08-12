package com.ramble.email.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@ConstructorBinding
@ConfigurationProperties(prefix = "email-sender")
data class EmailConfigProperties(
    val enableVault: Boolean,
    val vaultPath: String,
    val host: String,
    val port: Int,
    val transportProtocol: String,
    val smtpAuth: String,
    val startTlsEnabled: String,
    val startTlsRequired: String,
    val connectionTimeout: String,
    val timeout: String,
    val writeTimeout: String
)

@Configuration
@EnableConfigurationProperties(EmailConfigProperties::class)
class EmailSenderConfig(val emailConfigProperties: EmailConfigProperties)

internal class EmailSenderCredential(var username: String = "", val password: String = "")