package com.ramble.identity.service

import com.ramble.email.CredentialNotFoundException
import com.ramble.email.EmailSenderService
import com.ramble.email.EmailSendingFailedException
import com.ramble.identity.common.*
import com.ramble.identity.models.*
import com.ramble.identity.repo.UserRepo
import com.ramble.identity.service.validator.RegistrationRequestValidator
import com.ramble.token.RegistrationConfirmationService
import com.ramble.token.model.RegistrationConfirmationToken
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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

    private val registrationConfirmationToken = RegistrationConfirmationToken(userId, emailId, registrationTokenStr)
    private val registrationTime = Instant.now().toEpochMilli() / 1000
    private val currentTimeInSeconds = Instant.now().toEpochMilli() / 1000
    private val idGenerator: () -> Long = { 1L }

    private val userRegistrationService =
            UserRegistrationService(
                    userRepo, registrationRequestValidator, bCryptPasswordEncoder,
                    registrationConfirmationService, emailSenderService)

    @Before
    fun setup() {
        given(bCryptPasswordEncoder.encode(password)).willReturn(encryptedPassword)
        given(applicationUser.id).willReturn(userId)
        given(applicationUser.email).willReturn(emailId)
        given(applicationUser.fullName).willReturn(fullName)
        given(registrationConfirmationService
                .addRegistrationConfirmationToken(userId, emailId, now, expirationDurationAmount, expiryDurationUnit)
        ).willReturn(registrationConfirmationToken)
    }

    @Test
    fun `saveUser should return registeredUserResponse if user saved and email sent`() {
        // Stub
        given(userRepo.saveNewUser(userToSave, registrationTime, idGenerator)).willReturn(applicationUser)

        // Call method and assert
        val result =
                userRegistrationService.saveUser(
                        registerUserRequest,
                        now,
                        expirationDurationAmount,
                        expiryDurationUnit,
                        registrationTime,
                        idGenerator
                )
        assertTrue(result is Result.Success)
        assertEquals(RegisteredUserResponse(userId, emailId), result.data)
    }

    @Test
    fun `saveUser should return userAlreadyActivatedError if repo throws UserAlreadyActivatedException when savingNewUser`() {
        // Stub
        given(userRepo.saveNewUser(userToSave, registrationTime, idGenerator))
                .willThrow(UserAlreadyActivatedException())

        // Call method and assert
        val result =
                userRegistrationService.saveUser(
                        registerUserRequest,
                        now,
                        expirationDurationAmount,
                        expiryDurationUnit,
                        registrationTime,
                        idGenerator
                )
        assertTrue(result is Result.Error)
        assertEquals(HttpStatus.FORBIDDEN, result.httpStatus)
        assertEquals(userAlreadyActivatedError, result.errorBody)
    }

    @Test
    fun `saveUser should return userSuspendedError if repo throws UserSuspendedException when savingNewUser`() {
        // Stub
        given(userRepo.saveNewUser(userToSave, registrationTime, idGenerator))
                .willThrow(UserSuspendedException())

        // Call method and assert
        val result = userRegistrationService.saveUser(
                registerUserRequest,
                now,
                expirationDurationAmount,
                expiryDurationUnit,
                registrationTime,
                idGenerator
        )
        assertTrue(result is Result.Error)
        assertEquals(HttpStatus.FORBIDDEN, result.httpStatus)
        assertEquals(userSuspendedError, result.errorBody)
    }

    @Test
    fun `saveUser should return emailSendingFailed if CredentialNotFoundException when sending email`() {
        // Stub
        given(userRepo.saveNewUser(userToSave, registrationTime, idGenerator)).willReturn(applicationUser)
        given(emailSenderService
                .sendConfirmRegistrationEmail(
                        emailId, fullName, registrationTokenStr, SIGN_UP_CONFIRMATION_URL, REGISTER_EMAIL_SUBJECT)
        ).willThrow(CredentialNotFoundException())

        // Call method and assert
        val result =
                userRegistrationService.saveUser(
                        registerUserRequest,
                        now,
                        expirationDurationAmount,
                        expiryDurationUnit,
                        registrationTime,
                        idGenerator
                )
        assertTrue(result is Result.Error)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.httpStatus)
        assertEquals(emailSendingFailed, result.errorBody)
    }

    @Test
    fun `saveUser should return emailSendingFailed if EmailSendingFailedException when sending email`() {
        // Stub
        given(userRepo.saveNewUser(userToSave, registrationTime, idGenerator)).willReturn(applicationUser)
        given(emailSenderService
                .sendConfirmRegistrationEmail(
                        emailId, fullName, registrationTokenStr, SIGN_UP_CONFIRMATION_URL, REGISTER_EMAIL_SUBJECT)
        ).willThrow(EmailSendingFailedException())

        // Call method and assert
        val result =
                userRegistrationService.saveUser(
                        registerUserRequest,
                        now,
                        expirationDurationAmount,
                        expiryDurationUnit,
                        registrationTime,
                        idGenerator
                )
        assertTrue(result is Result.Error)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.httpStatus)
        assertEquals(emailSendingFailed, result.errorBody)
    }

    @Test
    fun `confirmToken should return registeredUserResponse`() {
        // Stub
        given(registrationConfirmationService.processRegistrationConfirmationToken(registrationTokenStr, now))
                .willReturn(registrationConfirmationToken)
        given(userRepo.activateRegisteredUser(emailId, currentTimeInSeconds)).willReturn(true)

        // Call method and assert
        val result = userRegistrationService.confirmToken(registrationTokenStr, now, currentTimeInSeconds)
        assertTrue(result is Result.Success)
        assertEquals(RegisteredUserResponse(userId, emailId), result.data)
    }

    @Test
    fun `confirmToken should return unauthorizedAccess when token is null`() {
        // Call method and assert
        val result = userRegistrationService.confirmToken(null, now, currentTimeInSeconds)
        assertTrue(result is Result.Error)
        assertEquals(HttpStatus.BAD_REQUEST, result.httpStatus)
        assertEquals(unauthorizedAccess, result.errorBody)
    }

    @Test
    fun `confirmToken should return unauthorizedAccess when token is invalid`() {
        // Stub
        given(registrationConfirmationService.processRegistrationConfirmationToken(registrationTokenStr, now))
                .willReturn(null)

        // Call method and assert
        val result = userRegistrationService.confirmToken(registrationTokenStr, now, currentTimeInSeconds)
        assertTrue(result is Result.Error)
        assertEquals(HttpStatus.BAD_REQUEST, result.httpStatus)
        assertEquals(unauthorizedAccess, result.errorBody)
    }

    @Test
    fun `confirmToken should return unauthorizedAccess if unable to change status to active`() {
        // Stub
        given(registrationConfirmationService.processRegistrationConfirmationToken(registrationTokenStr, now))
                .willReturn(registrationConfirmationToken)
        given(userRepo.activateRegisteredUser(emailId, currentTimeInSeconds)).willReturn(false)

        // Call method and assert
        val result = userRegistrationService.confirmToken(registrationTokenStr, now, currentTimeInSeconds)
        assertTrue(result is Result.Error)
        assertEquals(HttpStatus.FORBIDDEN, result.httpStatus)
        assertEquals(unauthorizedAccess, result.errorBody)
    }

    @Test
    fun `confirmToken should return badRequestError when UserNotFoundException is thrown by repo when trying to activate`() {
        // Stub
        given(registrationConfirmationService.processRegistrationConfirmationToken(registrationTokenStr, now))
                .willReturn(registrationConfirmationToken)
        given(userRepo.activateRegisteredUser(emailId, currentTimeInSeconds)).willThrow(UserNotFoundException())

        // Call method and assert
        val result = userRegistrationService.confirmToken(registrationTokenStr, now, currentTimeInSeconds)
        assertTrue(result is Result.Error)
        assertEquals(HttpStatus.BAD_REQUEST, result.httpStatus)
        assertEquals(unauthorizedAccess, result.errorBody)
    }

    @Test
    fun `confirmToken should return badRequestError when UserAlreadyActivatedException is thrown by repo when trying to activate`() {
        // Stub
        given(registrationConfirmationService.processRegistrationConfirmationToken(registrationTokenStr, now))
                .willReturn(registrationConfirmationToken)
        given(userRepo.activateRegisteredUser(emailId, currentTimeInSeconds)).willThrow(UserAlreadyActivatedException())

        // Call method and assert
        val result = userRegistrationService.confirmToken(registrationTokenStr, now, currentTimeInSeconds)
        assertTrue(result is Result.Error)
        assertEquals(HttpStatus.FORBIDDEN, result.httpStatus)
        assertEquals(userAlreadyActivatedError, result.errorBody)
    }

    @Test
    fun `confirmToken should return badRequestError when UserSuspendedException is thrown by repo when trying to activate`() {
        // Stub
        given(registrationConfirmationService.processRegistrationConfirmationToken(registrationTokenStr, now))
                .willReturn(registrationConfirmationToken)
        given(userRepo.activateRegisteredUser(emailId, currentTimeInSeconds)).willThrow(UserSuspendedException())

        // Call method and assert
        val result = userRegistrationService.confirmToken(registrationTokenStr, now, currentTimeInSeconds)
        assertTrue(result is Result.Error)
        assertEquals(HttpStatus.FORBIDDEN, result.httpStatus)
        assertEquals(userSuspendedError, result.errorBody)
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