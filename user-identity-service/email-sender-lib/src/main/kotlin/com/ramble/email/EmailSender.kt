package com.ramble.email

import com.ramble.email.config.Components.confirmRegistrationEmailService
import com.ramble.email.registration.ConfirmRegistrationEmailService

class EmailSender {

    private var confirmRegistrationEmailService: ConfirmRegistrationEmailService? = null

    constructor() {
        this.confirmRegistrationEmailService = confirmRegistrationEmailService()
    }

    @Suppress("unused")
    internal constructor(confirmRegistrationEmailService: ConfirmRegistrationEmailService) {
        this.confirmRegistrationEmailService = confirmRegistrationEmailService
    }

    @Throws(CredentialNotFoundException::class, EmailSendingFailedException::class)
    @Synchronized
    fun sendConfirmRegistrationEmail(emailId: String, fullName: String, token: String, signUpUrl: String) {
        val confirmEmailService = confirmRegistrationEmailService ?: confirmRegistrationEmailService()
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