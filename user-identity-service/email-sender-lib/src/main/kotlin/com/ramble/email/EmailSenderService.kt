package com.ramble.email

import com.ramble.email.config.EmailComponentBuilder
import com.ramble.email.registration.ConfirmRegistrationEmailService
import org.springframework.stereotype.Service

@Service
class EmailSenderService(private val emailComponentBuilder: EmailComponentBuilder) {

    private var confirmRegistrationEmailService: ConfirmRegistrationEmailService? =
            emailComponentBuilder.confirmRegistrationEmailService()

    @Throws(CredentialNotFoundException::class, EmailSendingFailedException::class)
    @Synchronized
    fun sendConfirmRegistrationEmail(emailId: String, fullName: String, token: String, signUpUrl: String) {
        val confirmEmailService = confirmRegistrationEmailService
                ?: emailComponentBuilder.confirmRegistrationEmailService()
        if (confirmEmailService != null) {
            this.confirmRegistrationEmailService = confirmEmailService
            confirmEmailService.sendEmail(
                    signUpUrl = signUpUrl,
                    to = emailId,
                    fullName = fullName,
                    token = token
            )
        }
    }
}