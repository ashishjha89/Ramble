package com.ramble.email

import com.ramble.email.config.EmailComponentBuilder
import com.ramble.email.registration.ConfirmRegistrationEmailService
import org.springframework.stereotype.Service

@Service
class EmailSenderService(private val emailComponentBuilder: EmailComponentBuilder) {

    private var confirmRegistrationEmailService: ConfirmRegistrationEmailService? = null

    @Throws(EmailCredentialNotFoundException::class, EmailSendingFailedException::class)
    @Synchronized
    fun sendConfirmRegistrationEmail(emailId: String, fullName: String, token: String, signUpUrl: String, subject: String) {
        val confirmEmailService = confirmRegistrationEmailService
                ?: emailComponentBuilder.confirmRegistrationEmailService()
                ?: throw EmailCredentialNotFoundException()
        this.confirmRegistrationEmailService = confirmEmailService
        confirmEmailService.sendEmail(
                to = emailId,
                fullName = fullName,
                token = token,
                signUpUrlPath = signUpUrl,
                subject = subject
        )
    }
}