package com.ramble.email

import com.ramble.email.config.EmailComponentBuilder
import com.ramble.email.registration.ConfirmRegistrationEmailService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class EmailSenderService(private val emailComponentBuilder: EmailComponentBuilder) {

    private val logger = LoggerFactory.getLogger(EmailSenderService::class.java)

    private var confirmRegistrationEmailService: ConfirmRegistrationEmailService? = null

    @Throws(EmailCredentialNotFoundException::class, EmailSendingFailedException::class)
    @Synchronized
    fun sendConfirmRegistrationEmail(
        emailId: String,
        fullName: String,
        token: String,
        signUpUrl: String,
        subject: String
    ) {
        val confirmEmailService = confirmRegistrationEmailService
            ?: emailComponentBuilder.confirmRegistrationEmailService()
            ?: let {
                logger.error("sendConfirmRegistrationEmail failed as credentials for sending email not found.")
                throw EmailCredentialNotFoundException()
            }
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