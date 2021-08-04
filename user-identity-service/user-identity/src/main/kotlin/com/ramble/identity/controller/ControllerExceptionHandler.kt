package com.ramble.identity.controller

import com.ramble.email.CredentialNotFoundException
import com.ramble.email.EmailSendingFailedException
import com.ramble.identity.common.*
import com.ramble.identity.models.*
import com.ramble.token.model.AccessTokenIsInvalidException
import com.ramble.token.model.RefreshTokenIsInvalidException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ControllerExceptionHandler {

    @ExceptionHandler(InternalServerException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun internalServerException(): ResponseEntity<ErrorBody> =
            ResponseEntity(internalServerError, HttpStatus.INTERNAL_SERVER_ERROR)

    @ExceptionHandler(UserNotFoundException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun userNotFoundException(): ResponseEntity<ErrorBody> =
            ResponseEntity(userInfoNotFound, HttpStatus.BAD_REQUEST)


    @ExceptionHandler(UserNotActivatedException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun userNotActivatedException(): ResponseEntity<ErrorBody> =
            ResponseEntity(userNotActivatedError, HttpStatus.FORBIDDEN)

    @ExceptionHandler(UserSuspendedException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun userSuspendedException(): ResponseEntity<ErrorBody> =
            ResponseEntity(userSuspendedError, HttpStatus.FORBIDDEN)

    @ExceptionHandler(UserAlreadyActivatedException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun userAlreadyActivatedException(): ResponseEntity<ErrorBody> =
            ResponseEntity(userAlreadyActivatedError, HttpStatus.FORBIDDEN)

    @ExceptionHandler(AccessTokenIsInvalidException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun accessTokenIsInvalidException(): ResponseEntity<ErrorBody> =
            ResponseEntity(unauthorizedAccess, HttpStatus.FORBIDDEN)

    @ExceptionHandler(RefreshTokenIsInvalidException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun refreshTokenIsInvalidException(): ResponseEntity<ErrorBody> =
            ResponseEntity(refreshTokenInvalid, HttpStatus.FORBIDDEN)

    @ExceptionHandler(InvalidRegistrationConfirmationToken::class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    fun registrationConfirmationTokenIsInvalidException(): ResponseEntity<ErrorBody> =
            ResponseEntity(unauthorizedAccess, HttpStatus.BAD_REQUEST)

    @ExceptionHandler(CredentialNotFoundException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun credentialNotFoundException(): ResponseEntity<ErrorBody> =
            ResponseEntity(emailSendingFailed, HttpStatus.INTERNAL_SERVER_ERROR)

    @ExceptionHandler(EmailSendingFailedException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun emailSendingFailedException(): ResponseEntity<ErrorBody> =
            ResponseEntity(emailSendingFailed, HttpStatus.INTERNAL_SERVER_ERROR)

}