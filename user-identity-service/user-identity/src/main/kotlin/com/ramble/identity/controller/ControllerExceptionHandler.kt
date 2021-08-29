package com.ramble.identity.controller

import com.ramble.email.EmailCredentialNotFoundException
import com.ramble.email.EmailSendingFailedException
import com.ramble.identity.common.*
import com.ramble.identity.models.*
import com.ramble.token.model.AccessTokenIsInvalidException
import com.ramble.token.model.InternalTokenStorageException
import com.ramble.token.model.RefreshTokenIsInvalidException
import io.swagger.v3.oas.annotations.Hidden
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
@Hidden // To hide it from Swagger! Controllers are specifying their exact errors.
class ControllerExceptionHandler {

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
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    fun registrationConfirmationTokenIsInvalidException(): ResponseEntity<ErrorBody> =
        ResponseEntity(unauthorizedAccess, HttpStatus.BAD_REQUEST)

    @ExceptionHandler(EmailCredentialNotFoundException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun credentialNotFoundException(): ResponseEntity<ErrorBody> =
        ResponseEntity(emailSendingFailed, HttpStatus.INTERNAL_SERVER_ERROR)

    @ExceptionHandler(EmailSendingFailedException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun emailSendingFailedException(): ResponseEntity<ErrorBody> =
        ResponseEntity(emailSendingFailed, HttpStatus.INTERNAL_SERVER_ERROR)

    @ExceptionHandler(InvalidEmailException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun invalidEmailException(): ResponseEntity<ErrorBody> =
        ResponseEntity(invalidEmailSyntaxError, HttpStatus.BAD_REQUEST)

    @ExceptionHandler(InternalServerException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun internalServerException(): ResponseEntity<ErrorBody> =
        ResponseEntity(internalServerError, HttpStatus.INTERNAL_SERVER_ERROR)

    @ExceptionHandler(InternalTokenStorageException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun tokenStorageException(): ResponseEntity<ErrorBody> =
        ResponseEntity(internalServerError, HttpStatus.INTERNAL_SERVER_ERROR)

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun uncaughtException(): ResponseEntity<ErrorBody> =
        ResponseEntity(internalServerError, HttpStatus.INTERNAL_SERVER_ERROR)

}