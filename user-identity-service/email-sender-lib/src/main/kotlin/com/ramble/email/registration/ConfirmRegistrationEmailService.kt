package com.ramble.email.registration

import com.ramble.email.EmailSendingFailedException
import org.slf4j.LoggerFactory
import org.springframework.mail.javamail.JavaMailSender

internal class ConfirmRegistrationEmailService(
    private val senderEmailId: String,
    private val mailSender: JavaMailSender,
    private val confirmRegistrationEmailBuilder: ConfirmRegistrationEmailBuilder
) {

    private val logger = LoggerFactory.getLogger(ConfirmRegistrationEmailService::class.java)

    @Throws(EmailSendingFailedException::class)
    fun sendEmail(to: String, fullName: String, token: String, signUpUrlPath: String, subject: String) {
        try {
            val body = confirmRegistrationEmailBuilder.buildEmail(
                name = fullName,
                link = confirmRegistrationEmailBuilder.getEmailLink(token, signUpUrlPath)
            )
            val mimeMessage = mailSender.createMimeMessage()
            val helper = confirmRegistrationEmailBuilder.mimeMessageHelper(mimeMessage)
            helper.setText(body, true)
            helper.setTo(to)
            helper.setSubject(subject)
            helper.setFrom(senderEmailId)
            mailSender.send(mimeMessage)
        } catch (e: Exception) {
            logger.error("sendEmail failed to:$to from:$senderEmailId exception.smg:${e.message}")
            throw EmailSendingFailedException()
        }
    }
}