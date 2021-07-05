package com.ramble.email

import com.ramble.email.registration.ConfirmRegistrationEmailService

class EmailSender {

    private val confirmRegistrationEmailService: ConfirmRegistrationEmailService

    constructor() {
        this.confirmRegistrationEmailService = Components.confirmRegistrationEmailService()
    }

    @Suppress("unused")
    internal constructor(confirmRegistrationEmailService: ConfirmRegistrationEmailService) {
        this.confirmRegistrationEmailService = confirmRegistrationEmailService
    }

    fun sendConfirmRegistrationEmail(emailId: String, fullName: String, token: String, signUpUrl: String): Unit {
        confirmRegistrationEmailService.sendEmail(
                signUpUrl = signUpUrl,
                to = emailId,
                fullName = fullName,
                token = token
        )
    }
}