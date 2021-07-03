package com.ramble.identity.service

import com.ramble.identity.common.*
import com.ramble.identity.models.*
import com.ramble.identity.repo.UserRepo
import com.ramble.identity.service.helper.ConfirmRegistrationEmailBuilder
import com.ramble.identity.service.helper.ConfirmRegistrationEmailService
import com.ramble.identity.service.validator.RegistrationRequestValidator
import com.ramble.token.handler.RegistrationConfirmationHandler
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserRegistrationService(
        private val registrationRequestValidator: RegistrationRequestValidator,
        private val userRepo: UserRepo,
        private val registrationConfirmationHandler: RegistrationConfirmationHandler,
        private val confirmRegistrationEmailBuilder: ConfirmRegistrationEmailBuilder,
        private val confirmRegistrationEmailService: ConfirmRegistrationEmailService,
        private val bCryptPasswordEncoder: BCryptPasswordEncoder
) {

    fun saveUser(registerUserRequest: RegisterUserRequest): Result<RegisteredUserResponse> {
        registrationRequestValidator.getRegistrationRequestError()?.let {
            return Result.Error(httpStatus = HttpStatus.BAD_REQUEST, errorBody = it)
        }
        return try {
            val userToSave = registerUserRequest.copy(password = bCryptPasswordEncoder.encode(registerUserRequest.password))
            val newlyRegisteredUser = userRepo.saveNewUser(userToSave)
            val confirmRegistrationToken = registrationConfirmationHandler.addRegistrationConfirmationToken(
                    userId = newlyRegisteredUser.id,
                    email = newlyRegisteredUser.email
            )
            val registeredUserResponse = RegisteredUserResponse(
                    userId = confirmRegistrationToken.userId,
                    email = confirmRegistrationToken.email
            )
            println("UserRegistrationService saveUser() confirmationToken:${confirmRegistrationToken.token}")
            sendAccountActivationEmail(
                    token = confirmRegistrationToken.token,
                    emailId = newlyRegisteredUser.email,
                    fullName = newlyRegisteredUser.fullName
            )
            Result.Success(data = registeredUserResponse)
        } catch (e: Exception) {
            when (e) {
                is UserAlreadyActivatedException -> Result.Error(HttpStatus.FORBIDDEN, userAlreadyActivatedError)
                is UserSuspendedException -> Result.Error(HttpStatus.FORBIDDEN, userSuspendedError)
                else -> Result.Error(HttpStatus.INTERNAL_SERVER_ERROR, internalServerError)
            }

        }
    }

    fun confirmToken(token: String?): Result<RegisteredUserResponse> {
        val badRequestError = Result.Error<RegisteredUserResponse>(HttpStatus.BAD_REQUEST, unauthorizedAccess)
        token ?: return badRequestError
        val confirmationToken = registrationConfirmationHandler.processRegistrationConfirmationToken(token)
                ?: return badRequestError
        return try {
            val activated = userRepo.activateRegisteredUser(email = confirmationToken.email)
            if (activated) Result.Success(data = RegisteredUserResponse(userId = confirmationToken.userId, email = confirmationToken.email))
            else Result.Error(HttpStatus.FORBIDDEN, unauthorizedAccess)
        } catch (e: Exception) {
            when (e) {
                is UserNotFoundException -> badRequestError
                is UserAlreadyActivatedException -> Result.Error(HttpStatus.FORBIDDEN, userAlreadyActivatedError)
                is UserSuspendedException -> Result.Error(HttpStatus.FORBIDDEN, userSuspendedError)
                else -> Result.Error(HttpStatus.INTERNAL_SERVER_ERROR, internalServerError)
            }
        }
    }

    private fun sendAccountActivationEmail(token: String, emailId: String, fullName: String) {
        val emailLink = confirmRegistrationEmailBuilder.getEmailLink(token)
        val emailBody = confirmRegistrationEmailBuilder.buildEmail(fullName, emailLink)
        confirmRegistrationEmailService.sendEmail(emailId, emailBody)
    }

}