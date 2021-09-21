package com.ramble.messaging.controller

import com.ramble.messaging.common.*
import com.ramble.messaging.model.AccessTokenIsInvalidException
import com.ramble.messaging.model.InternalServerException
import com.ramble.messaging.model.InvalidUserEmailException
import com.ramble.messaging.model.UserNotFoundException
import io.swagger.v3.oas.annotations.Hidden
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@Suppress("unused")
@RestControllerAdvice
@Hidden // To hide it from Swagger! Controllers are specifying their exact errors.
class ControllerExceptionHandler {

    @ExceptionHandler(UserNotFoundException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun userNotFoundException(): ResponseEntity<ErrorBody> =
        ResponseEntity(userInfoNotFound, HttpStatus.BAD_REQUEST)

    @ExceptionHandler(InvalidUserEmailException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun invalidUserEmailException(): ResponseEntity<ErrorBody> =
        ResponseEntity(invalidUserEmail, HttpStatus.NOT_FOUND)

    @ExceptionHandler(AccessTokenIsInvalidException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun accessTokenIsInvalidException(): ResponseEntity<ErrorBody> =
        ResponseEntity(unauthorizedAccess, HttpStatus.FORBIDDEN)

    @ExceptionHandler(InternalServerException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun internalServerException(): ResponseEntity<ErrorBody> =
        ResponseEntity(internalServerError, HttpStatus.INTERNAL_SERVER_ERROR)

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun uncaughtException(): ResponseEntity<ErrorBody> =
        ResponseEntity(internalServerError, HttpStatus.INTERNAL_SERVER_ERROR)

}