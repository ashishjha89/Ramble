package com.ramble.email.config

import com.ramble.email.registration.ConfirmRegistrationEmailBuilder
import com.ramble.email.registration.ConfirmRegistrationEmailService
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.stereotype.Service
import org.springframework.vault.authentication.TokenAuthentication
import org.springframework.vault.client.VaultEndpoint
import org.springframework.vault.core.VaultTemplate
import java.util.*

@Service
class EmailComponentBuilder(emailSenderConfig: EmailSenderConfig) {

    private val configProperties = emailSenderConfig.emailConfigProperties

    internal fun confirmRegistrationEmailService(): ConfirmRegistrationEmailService? {
        val emailCredential = emailCredential() ?: return null
        return ConfirmRegistrationEmailService(
                senderEmail = emailCredential.username,
                mailSender = mailSender(emailCredential),
                confirmRegistrationEmailBuilder = ConfirmRegistrationEmailBuilder()
        )
    }

    private fun mailSender(emailCredential: EmailSenderCredential): JavaMailSender {
        return JavaMailSenderImpl().apply {
            username = emailCredential.username
            password = emailCredential.password

            host = configProperties.host
            port = configProperties.port

            val props: Properties = javaMailProperties

            with(configProperties) {
                props["mail.transport.protocol"] = transportProtocol
                props["mail.smtp.ssl.trust"] = "*"
                props["mail.smtp.auth"] = smtpAuth
                props["mail.smtp.starttls.enable"] = startTlsEnabled
                props["mail.smtp.starttls.required"] = startTlsRequired
                props["mail.smtp.connectiontimeout"] = connectionTimeout
                props["mail.smtp.timeout"] = timeout
                props["mail.smtp.writetimeout"] = writeTimeout
            }
        }
    }

    private fun emailCredential(): EmailSenderCredential? {
        if (!configProperties.enableVault) {
            return EmailSenderCredential("dummy_user@ramble.com", "dummy_pwd")
        }
        val vaultTemplate =
                VaultTemplate(
                        VaultEndpoint().apply { scheme = "http" },
                        TokenAuthentication("00000000-0000-0000-0000-000000000000")
                )
        return try {
            val response = vaultTemplate.read("kv/ramble.email-sender", EmailSenderCredential::class.java)
            response?.data
        } catch (e: Exception) {
            null
        }
    }

}