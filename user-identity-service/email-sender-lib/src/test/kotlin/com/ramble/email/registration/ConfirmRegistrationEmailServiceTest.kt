package com.ramble.email.registration

import com.ramble.email.EmailSendingFailedException
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.verify
import org.mockito.Mockito.mock
import org.springframework.mail.MailSendException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import javax.mail.internet.MimeMessage

class ConfirmRegistrationEmailServiceTest {

    private val senderEmailId = "senderEmailId@ramble.com"
    private val emailBody = "some email body"

    private val to = "someEmailId@ramble.com"
    private val fullName = "SomeFirstName SomeLastName"
    private val token = "this_is_some_random_token"
    private val signUpUrlPath = "confirm"
    private val subject = "Confirm your email"

    private val mimeMessage = mock(MimeMessage::class.java)
    private val mimeMessageHelper = mock(MimeMessageHelper::class.java)
    private val mailSender = mock(JavaMailSender::class.java)
    private val confirmRegistrationEmailBuilder = mock(ConfirmRegistrationEmailBuilder::class.java)

    private val fullPath = "http://some_registration_confirmation_path@ramble.com"

    private val confirmRegistrationEmailService = ConfirmRegistrationEmailService(senderEmailId, mailSender, confirmRegistrationEmailBuilder)

    @Before
    fun setup() {
        given(mailSender.createMimeMessage()).willReturn(mimeMessage)
        given(confirmRegistrationEmailBuilder.mimeMessageHelper(mimeMessage)).willReturn(mimeMessageHelper)
        given(confirmRegistrationEmailBuilder.getEmailLink(token, signUpUrlPath)).willReturn(fullPath)
        given(confirmRegistrationEmailBuilder.buildEmail(fullName, fullPath)).willReturn(emailBody)
    }

    @Test
    fun `sendMail sends mimeMessage email via JavaMailSender `() {
        // Call method
        confirmRegistrationEmailService.sendEmail(to, fullName, token, signUpUrlPath, subject)

        // Verify
        verify(mimeMessageHelper).setText(emailBody, true)
        verify(mimeMessageHelper).setTo(to)
        verify(mimeMessageHelper).setFrom(senderEmailId)
        verify(mimeMessageHelper).setSubject(subject)
        verify(mailSender).send(mimeMessage)
    }

    @Test(expected = EmailSendingFailedException::class)
    fun `sendEmail throws EmailSendingFailedException if sending message failed`() {
        // Stub
        given(mailSender.send(mimeMessage)).willThrow(MailSendException("mail exception"))

        // Call method
        confirmRegistrationEmailService.sendEmail(to, fullName, token, signUpUrlPath, subject)
    }
}