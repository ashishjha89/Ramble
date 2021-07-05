package com.ramble.identity.models

class UserNotFoundException : Exception()

class UserAlreadyActivatedException : Exception()

class UserSuspendedException : Exception()

class UserNotActivatedException : Exception()