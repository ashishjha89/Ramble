package com.ramble.messaging.model

class UserNotFoundException : Exception()

class InvalidUserEmailException : Exception()

class AccessTokenIsInvalidException : Exception()

class UnauthorizedException: Exception()

class InternalServerException : Exception()