package com.ramble.identity.models

class InternalServerException: Exception()

class UserNotFoundException : Exception()

class UserAlreadyActivatedException : Exception()

class UserSuspendedException : Exception()

class UserNotActivatedException : Exception()

class ClientIdHeaderAbsentException: Exception()

class InvalidEmailException: Exception()

class InvalidRegistrationConfirmationToken: Exception()

