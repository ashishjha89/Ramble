package com.ramble.identity.controller

import com.ramble.identity.common.*
import org.junit.Test
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import kotlin.test.assertEquals

class ControllerExceptionHandlerTest {

    private val exceptionHandler = ControllerExceptionHandler()

    @Test
    fun internalServerExceptionTest() {
        assertEquals(
                ResponseEntity(internalServerError, HttpStatus.INTERNAL_SERVER_ERROR),
                exceptionHandler.internalServerException()
        )
    }

    @Test
    fun userNotFoundExceptionTest() {
        assertEquals(
                ResponseEntity(userInfoNotFound, HttpStatus.BAD_REQUEST),
                exceptionHandler.userNotFoundException()
        )
    }

    @Test
    fun userNotActivatedExceptionTest() {
        assertEquals(
                ResponseEntity(userNotActivatedError, HttpStatus.FORBIDDEN),
                exceptionHandler.userNotActivatedException()
        )
    }

    @Test
    fun userSuspendedExceptionTest() {
        assertEquals(
                ResponseEntity(userSuspendedError, HttpStatus.FORBIDDEN),
                exceptionHandler.userSuspendedException()
        )
    }

    @Test
    fun userAlreadyActivatedExceptionTest() {
        assertEquals(
                ResponseEntity(userAlreadyActivatedError, HttpStatus.FORBIDDEN),
                exceptionHandler.userAlreadyActivatedException()
        )
    }

    @Test
    fun accessTokenIsInvalidExceptionTest() {
        assertEquals(
                ResponseEntity(unauthorizedAccess, HttpStatus.FORBIDDEN),
                exceptionHandler.accessTokenIsInvalidException()
        )
    }

    @Test
    fun refreshTokenIsInvalidExceptionTest() {
        assertEquals(
                ResponseEntity(refreshTokenInvalid, HttpStatus.FORBIDDEN),
                exceptionHandler.refreshTokenIsInvalidException()
        )
    }

    @Test
    fun registrationConfirmationTokenIsInvalidExceptionTest() {
        assertEquals(
                ResponseEntity(unauthorizedAccess, HttpStatus.BAD_REQUEST),
                exceptionHandler.registrationConfirmationTokenIsInvalidException()
        )
    }

    @Test
    fun credentialNotFoundExceptionTest() {
        assertEquals(
                ResponseEntity(emailSendingFailed, HttpStatus.INTERNAL_SERVER_ERROR),
                exceptionHandler.credentialNotFoundException()
        )
    }

    @Test
    fun emailSendingFailedExceptionTest() {
        assertEquals(
                ResponseEntity(emailSendingFailed, HttpStatus.INTERNAL_SERVER_ERROR),
                exceptionHandler.emailSendingFailedException()
        )
    }
}