package com.ramble.email.config

import com.ramble.email.registration.ConfirmRegistrationEmailBuilder
import com.ramble.email.registration.ConfirmRegistrationEmailService
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.vault.authentication.TokenAuthentication
import org.springframework.vault.client.VaultEndpoint
import org.springframework.vault.core.VaultTemplate
import java.util.*

internal object Components {

    var enableVault = false

    private fun mailSender(emailCredential: EmailSenderCredential): JavaMailSender {
        return JavaMailSenderImpl().apply {
            host = "localhost"
            port = 1025
            username = emailCredential.username
            password = emailCredential.password

            val props: Properties = javaMailProperties
            props["mail.transport.protocol"] = "smtp"
            props["mail.smtp.ssl.trust"] = "*"
            props["mail.smtp.auth"] = "true"
            props["mail.smtp.starttls.enable"] = "true"
            props["mail.smtp.starttls.required"] = "true"
            props["mail.smtp.connectiontimeout"] = "5000"
            props["mail.smtp.timeout"] = "3000"
            props["mail.smtp.writetimeout"] = "5000"
        }
    }

    fun confirmRegistrationEmailService(): ConfirmRegistrationEmailService? {
        val emailCredential = emailCredential() ?: return null
        return ConfirmRegistrationEmailService(
                senderEmail = emailCredential.username,
                mailSender = mailSender(emailCredential),
                confirmRegistrationEmailBuilder = ConfirmRegistrationEmailBuilder()
        )
    }

    private val vaultTemplate =
            VaultTemplate(
                    VaultEndpoint().apply { scheme = "http" },
                    TokenAuthentication("00000000-0000-0000-0000-000000000000")
            )

    @Suppress("ConstantConditionIf")
    private fun emailCredential(): EmailSenderCredential? {
        if (!enableVault) {
            return EmailSenderCredential("dummy_user@ramble.com", "dummy_pwd")
        }
        return try {
            val response = vaultTemplate.read("kv/ramble.email-sender", EmailSenderCredential::class.java)
            response?.data
        } catch (e: Exception) {
            null
        }
    }
}