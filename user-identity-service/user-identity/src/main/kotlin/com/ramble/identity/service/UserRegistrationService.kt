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
import org.springframework.context.annotation.Lazy
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.Async
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserRegistrationService(
        private val userRepo: UserRepo,
        private val registrationRequestValidator: RegistrationRequestValidator,
        private val bCryptPasswordEncoder: BCryptPasswordEncoder,
        private val registrationConfirmationService: RegistrationConfirmationService,
        @Lazy private val emailSenderService: EmailSenderService
) {

    fun saveUser(registerUserRequest: RegisterUserRequest): Result<RegisteredUserResponse> {
        registrationRequestValidator.getRegistrationRequestError()?.let {
            return Result.Error(httpStatus = HttpStatus.BAD_REQUEST, errorBody = it)
        }
        return try {
            val userToSave = registerUserRequest.copy(password = bCryptPasswordEncoder.encode(registerUserRequest.password))
            val newlyRegisteredUser = userRepo.saveNewUser(userToSave)
            val confirmRegistrationToken = registrationConfirmationService.addRegistrationConfirmationToken(
                    userId = newlyRegisteredUser.id,
                    email = newlyRegisteredUser.email
            )
            val registeredUserResponse = RegisteredUserResponse(
                    userId = confirmRegistrationToken.userId,
                    email = confirmRegistrationToken.email
            )
            sendConfirmRegistrationEmail(confirmRegistrationToken, newlyRegisteredUser)

            Result.Success(data = registeredUserResponse)
        } catch (e: Exception) {
            when (e) {
                is UserAlreadyActivatedException -> Result.Error(HttpStatus.FORBIDDEN, userAlreadyActivatedError)
                is UserSuspendedException -> Result.Error(HttpStatus.FORBIDDEN, userSuspendedError)
                is CredentialNotFoundException, is EmailSendingFailedException -> Result.Error(HttpStatus.INTERNAL_SERVER_ERROR, emailSendingFailed)
                else -> Result.Error(HttpStatus.INTERNAL_SERVER_ERROR, internalServerError)
            }
        }
    }

    fun confirmToken(token: String?): Result<RegisteredUserResponse> {
        val badRequestError = Result.Error<RegisteredUserResponse>(HttpStatus.BAD_REQUEST, unauthorizedAccess)
        token ?: return badRequestError
        val confirmationToken = registrationConfirmationService.processRegistrationConfirmationToken(token)
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

    @Throws(CredentialNotFoundException::class, EmailSendingFailedException::class)
    @Async
    fun sendConfirmRegistrationEmail(confirmRegistrationToken: RegistrationConfirmationToken, newlyRegisteredUser: ApplicationUser) {
        emailSenderService.sendConfirmRegistrationEmail(
                emailId = confirmRegistrationToken.email,
                fullName = newlyRegisteredUser.fullName,
                token = confirmRegistrationToken.token,
                signUpUrl = SIGN_UP_CONFIRMATION_URL
        )
    }

}