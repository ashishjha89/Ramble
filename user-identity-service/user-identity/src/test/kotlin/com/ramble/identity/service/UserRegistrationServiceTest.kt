package com.ramble.identity.service

import com.ramble.email.EmailCredentialNotFoundException
import com.ramble.email.EmailSenderService
import com.ramble.email.EmailSendingFailedException
import com.ramble.identity.common.REGISTER_EMAIL_SUBJECT
import com.ramble.identity.common.SIGN_UP_CONFIRMATION_URL
import com.ramble.identity.models.*
import com.ramble.identity.repo.UserRepo
import com.ramble.identity.service.validator.RegistrationRequestValidator
import com.ramble.identity.utils.TimeAndIdGenerator
import com.ramble.token.RegistrationConfirmationService
import com.ramble.token.model.RegistrationConfirmationToken
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.willDoNothing
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals

class UserRegistrationServiceTest {

    private val userRepo = mock(UserRepo::class.java)
    private val registrationRequestValidator = mock(RegistrationRequestValidator::class.java)
    private val bCryptPasswordEncoder = mock(BCryptPasswordEncoder::class.java)
    private val registrationConfirmationService = mock(RegistrationConfirmationService::class.java)
    private val emailSenderService = mock(EmailSenderService::class.java)

    private val userId = "someUserId"
    private val emailId = "someEmailId"
    private val password = "somePassword"
    private val encryptedPassword = "someEncryptedPassword"
    private val registrationTokenStr = "some_registration_token"
    private val fullName = "firstName_lastName"
    private val expirationDurationAmount = 15L
    private val expiryDurationUnit = ChronoUnit.MINUTES
    private val now = Instant.now()

    private val registerUserRequest = RegisterUserRequest(emailId, password)
    private val userToSave = RegisterUserRequest(emailId, encryptedPassword)
    private val applicationUser = mock(ApplicationUser::class.java)
    private val timeAndIdGenerator = mock(TimeAndIdGenerator::class.java)

    private val registrationConfirmationToken = RegistrationConfirmationToken(userId, emailId, registrationTokenStr)

    private val userRegistrationService =
        UserRegistrationService(
            userRepo, registrationRequestValidator, bCryptPasswordEncoder,
            registrationConfirmationService, emailSenderService, timeAndIdGenerator
        )

    @Before
    fun setup() {
        given(bCryptPasswordEncoder.encode(password)).willReturn(encryptedPassword)
        given(applicationUser.id).willReturn(userId)
        given(applicationUser.email).willReturn(emailId)
        given(applicationUser.fullName).willReturn(fullName)
        given(
            registrationConfirmationService
                .addRegistrationConfirmationToken(userId, emailId, now, expirationDurationAmount, expiryDurationUnit)
        ).willReturn(registrationConfirmationToken)
        given(timeAndIdGenerator.getCurrentTime()).willReturn(now)
    }

    @Test
    fun `saveUser should return registeredUserResponse if user saved and email sent`() {
        // Stub
        given(userRepo.saveNewUser(userToSave)).willReturn(applicationUser)

        // Call method and assert
        val result =
            userRegistrationService.saveUser(registerUserRequest, expirationDurationAmount, expiryDurationUnit)
        assertEquals(RegisteredUserResponse(userId, emailId), result)
    }

    @Test(expected = UserAlreadyActivatedException::class)
    fun `saveUser should throw UserAlreadyActivatedException if repo throws UserAlreadyActivatedException when savingNewUser`() {
        // Stub
        given(userRepo.saveNewUser(userToSave)).willThrow(UserAlreadyActivatedException())

        // Call method and assert
        userRegistrationService.saveUser(registerUserRequest, expirationDurationAmount, expiryDurationUnit)
    }

    @Test(expected = UserSuspendedException::class)
    fun `saveUser should throw UserSuspendedException if repo throws UserSuspendedException when savingNewUser`() {
        // Stub
        given(userRepo.saveNewUser(userToSave)).willThrow(UserSuspendedException())

        // Call method and assert
        userRegistrationService.saveUser(registerUserRequest, expirationDurationAmount, expiryDurationUnit)
    }

    @Test(expected = EmailCredentialNotFoundException::class)
    fun `saveUser should throw CredentialNotFoundException if emailSenderService throws CredentialNotFoundException when sending email`() {
        // Stub
        given(userRepo.saveNewUser(userToSave)).willReturn(applicationUser)
        given(
            emailSenderService
                .sendConfirmRegistrationEmail(
                    emailId, fullName, registrationTokenStr, SIGN_UP_CONFIRMATION_URL, REGISTER_EMAIL_SUBJECT
                )
        ).willThrow(EmailCredentialNotFoundException())

        // Call method and assert
        userRegistrationService.saveUser(registerUserRequest, expirationDurationAmount, expiryDurationUnit)
    }

    @Test(expected = EmailSendingFailedException::class)
    fun `saveUser should throw EmailSendingFailedException if emailSenderService throws EmailSendingFailedException when sending email`() {
        // Stub
        given(userRepo.saveNewUser(userToSave)).willReturn(applicationUser)
        given(
            emailSenderService
                .sendConfirmRegistrationEmail(
                    emailId, fullName, registrationTokenStr, SIGN_UP_CONFIRMATION_URL, REGISTER_EMAIL_SUBJECT
                )
        ).willThrow(EmailSendingFailedException())

        // Call method and assert
        userRegistrationService.saveUser(registerUserRequest, expirationDurationAmount, expiryDurationUnit)
    }

    @Test
    fun `confirmToken should return registeredUserResponse`() {
        // Stub
        given(registrationConfirmationService.processRegistrationConfirmationToken(registrationTokenStr, now))
            .willReturn(registrationConfirmationToken)
        willDoNothing().given(userRepo).activateRegisteredUser(emailId)

        // Call method and assert
        val result = userRegistrationService.confirmToken(registrationTokenStr)
        assertEquals(RegisteredUserResponse(userId, emailId), result)
    }

    @Test(expected = InvalidRegistrationConfirmationToken::class)
    fun `confirmToken should throw InvalidRegistrationConfirmationToken when token is null`() {
        // Call method and assert
        userRegistrationService.confirmToken(null)
    }

    @Test(expected = InvalidRegistrationConfirmationToken::class)
    fun `confirmToken should throw InvalidRegistrationConfirmationToken when token is invalid`() {
        // Stub
        given(registrationConfirmationService.processRegistrationConfirmationToken(registrationTokenStr, now))
            .willReturn(null)

        // Call method and assert
        userRegistrationService.confirmToken(registrationTokenStr)
    }

    @Test(expected = UserNotFoundException::class)
    fun `confirmToken should throw UserNotFoundException if userRepo throws UserNotFoundException`() {
        // Stub
        given(registrationConfirmationService.processRegistrationConfirmationToken(registrationTokenStr, now))
            .willReturn(registrationConfirmationToken)
        given(userRepo.activateRegisteredUser(emailId)).willThrow(UserNotFoundException())

        // Call method and assert
        userRegistrationService.confirmToken(registrationTokenStr)
    }

    @Test(expected = UserAlreadyActivatedException::class)
    fun `confirmToken should throw UserAlreadyActivatedException when UserAlreadyActivatedException is thrown by repo when trying to activate`() {
        // Stub
        given(registrationConfirmationService.processRegistrationConfirmationToken(registrationTokenStr, now))
            .willReturn(registrationConfirmationToken)
        given(userRepo.activateRegisteredUser(emailId)).willThrow(UserAlreadyActivatedException())

        // Call method and assert
        userRegistrationService.confirmToken(registrationTokenStr)
    }

    @Test(expected = UserSuspendedException::class)
    fun `confirmToken should throw UserSuspendedException when UserSuspendedException is thrown by repo when trying to activate`() {
        // Stub
        given(registrationConfirmationService.processRegistrationConfirmationToken(registrationTokenStr, now))
            .willReturn(registrationConfirmationToken)
        given(userRepo.activateRegisteredUser(emailId)).willThrow(UserSuspendedException())

        // Call method and assert
        userRegistrationService.confirmToken(registrationTokenStr)
    }

    @Test
    fun sendConfirmRegistrationEmailTest() {
        // Call method
        userRegistrationService.sendConfirmRegistrationEmail(registrationConfirmationToken, applicationUser)

        // Verify
        verify(emailSenderService).sendConfirmRegistrationEmail(
            emailId, fullName, registrationTokenStr, SIGN_UP_CONFIRMATION_URL, REGISTER_EMAIL_SUBJECT
        )
    }

}