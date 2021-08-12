package com.ramble.identity.service

import com.ramble.email.EmailCredentialNotFoundException
import com.ramble.email.EmailSenderService
import com.ramble.email.EmailSendingFailedException
import com.ramble.identity.common.REGISTER_EMAIL_SUBJECT
import com.ramble.identity.common.SIGN_UP_CONFIRMATION_URL
import com.ramble.identity.models.*
import com.ramble.identity.repo.UserRepo
import com.ramble.identity.service.validator.RegistrationRequestValidator
import com.ramble.identity.utils.TimeAndIdGenerator
import com.ramble.token.RegistrationConfirmationService
import com.ramble.token.model.RegistrationConfirmationToken
import org.springframework.scheduling.annotation.Async
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.time.temporal.ChronoUnit

@Service
class UserRegistrationService(
    private val userRepo: UserRepo,
    private val registrationRequestValidator: RegistrationRequestValidator,
    private val bCryptPasswordEncoder: BCryptPasswordEncoder,
    private val registrationConfirmationService: RegistrationConfirmationService,
    private val emailSenderService: EmailSenderService,
    private val timeAndIdGenerator: TimeAndIdGenerator
) {

    @Throws(
        UserAlreadyActivatedException::class,
        UserSuspendedException::class,
        EmailCredentialNotFoundException::class,
        EmailSendingFailedException::class,
        InvalidEmailException::class
    )
    fun saveUser(
        registerUserRequest: RegisterUserRequest,
        expirationDurationAmount: Long = 15,
        expiryDurationUnit: ChronoUnit = ChronoUnit.MINUTES
    ): RegisteredUserResponse {
        val now = timeAndIdGenerator.getCurrentTime()
        registrationRequestValidator.getRegistrationRequestError()?.let { throw InvalidEmailException() }
        val userToSave = registerUserRequest.copy(password = bCryptPasswordEncoder.encode(registerUserRequest.password))
        val newlyRegisteredUser = userRepo.saveNewUser(userToSave)
        val confirmRegistrationToken = registrationConfirmationService.addRegistrationConfirmationToken(
            userId = newlyRegisteredUser.id,
            email = newlyRegisteredUser.email,
            now = now,
            expirationDurationAmount = expirationDurationAmount,
            expiryDurationUnit = expiryDurationUnit
        )
        val registeredUserResponse = RegisteredUserResponse(
            userId = confirmRegistrationToken.userId,
            email = confirmRegistrationToken.email
        )
        sendConfirmRegistrationEmail(confirmRegistrationToken, newlyRegisteredUser)
        return registeredUserResponse
    }

    @Throws(
        UserNotFoundException::class,
        UserAlreadyActivatedException::class,
        UserSuspendedException::class,
        InvalidRegistrationConfirmationToken::class
    )
    fun confirmToken(token: String?): RegisteredUserResponse {
        val now = timeAndIdGenerator.getCurrentTime()
        token ?: throw InvalidRegistrationConfirmationToken()
        val confirmationToken = registrationConfirmationService.processRegistrationConfirmationToken(token, now)
            ?: throw InvalidRegistrationConfirmationToken()
        userRepo.activateRegisteredUser(email = confirmationToken.email)
        return RegisteredUserResponse(userId = confirmationToken.userId, email = confirmationToken.email)
    }

    @Throws(EmailCredentialNotFoundException::class, EmailSendingFailedException::class)
    @Async
    fun sendConfirmRegistrationEmail(
        confirmRegistrationToken: RegistrationConfirmationToken,
        newlyRegisteredUser: ApplicationUser
    ) {
        emailSenderService.sendConfirmRegistrationEmail(
            emailId = confirmRegistrationToken.email,
            fullName = newlyRegisteredUser.fullName,
            token = confirmRegistrationToken.token,
            signUpUrl = SIGN_UP_CONFIRMATION_URL,
            subject = REGISTER_EMAIL_SUBJECT
        )
    }

}