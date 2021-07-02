package com.ramble.identity.models

import java.lang.Exception

class UserNotFoundException: Exception()

class UserAlreadyActivatedException: Exception()

class UserSuspendedException: Exception()

class UserNotActivatedException: Exception()