package com.ramble.email.registration

import com.ramble.email.EmailSendingFailedException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper

internal class ConfirmRegistrationEmailService(
        val senderEmail: String,
        private val mailSender: JavaMailSender,
        private val confirmRegistrationEmailBuilder: ConfirmRegistrationEmailBuilder
) {

    @Throws(EmailSendingFailedException::class)
    fun sendEmail(signUpUrl: String, to: String, fullName: String, token: String) {
        try {
            val body = confirmRegistrationEmailBuilder.buildEmail(
                    name = fullName,
                    link = confirmRegistrationEmailBuilder.getEmailLink(token, signUpUrl)
            )
            val mimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(mimeMessage, "utf-8")
            helper.setText(body, true)
            helper.setTo(to)
            helper.setSubject("Confirm your email")
            helper.setFrom(senderEmail)
            mailSender.send(mimeMessage)
        } catch (e: Exception) {
            throw EmailSendingFailedException()
        }
    }
}