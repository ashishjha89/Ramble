package com.ramble.email

import com.ramble.email.config.EmailComponentBuilder
import com.ramble.email.registration.ConfirmRegistrationEmailService
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.verify
import org.mockito.Mock
import org.mockito.Mockito.mock

class EmailSenderServiceTest {

    private val emailId = "someEmailId@ramble.com"
    private val fullName = "SomeFirstName SomeLastName"
    private val token = "this_is_some_random_token"
    private val signUpUrlPath = "register"
    private val subject = "Confirm your email"

    @Mock
    private val emailComponentBuilder = mock(EmailComponentBuilder::class.java)

    @Mock
    private val confirmRegistrationEmailService = mock(ConfirmRegistrationEmailService::class.java)

    @Test(expected = EmailCredentialNotFoundException::class)
    fun `sendConfirmRegistrationEmail should throw CredentialNotFoundException if ConfirmRegistrationEmailService creation failed`() {
        // Stub
        given(emailComponentBuilder.confirmRegistrationEmailService()).willReturn(null)

        // Call method
        EmailSenderService(emailComponentBuilder).sendConfirmRegistrationEmail(
            emailId,
            fullName,
            token,
            signUpUrlPath,
            subject
        )
    }

    @Test
    fun `sendConfirmRegistrationEmail should call confirmRegistrationEmailService to send email`() {
        // Stub
        given(emailComponentBuilder.confirmRegistrationEmailService()).willReturn(confirmRegistrationEmailService)

        // Call method
        EmailSenderService(emailComponentBuilder).sendConfirmRegistrationEmail(
            emailId,
            fullName,
            token,
            signUpUrlPath,
            subject
        )

        // Verify
        verify(confirmRegistrationEmailService).sendEmail(emailId, fullName, token, signUpUrlPath, subject)
    }

    @Test(expected = EmailSendingFailedException::class)
    fun `sendConfirmRegistrationEmail should throw EmailSendingFailedException if email sending failed`() {
        // Stub
        given(emailComponentBuilder.confirmRegistrationEmailService()).willReturn(confirmRegistrationEmailService)
        given(confirmRegistrationEmailService.sendEmail(emailId, fullName, token, signUpUrlPath, subject)).willThrow(
            EmailSendingFailedException()
        )

        // Call method
        EmailSenderService(emailComponentBuilder).sendConfirmRegistrationEmail(
            emailId,
            fullName,
            token,
            signUpUrlPath,
            subject
        )
    }
}