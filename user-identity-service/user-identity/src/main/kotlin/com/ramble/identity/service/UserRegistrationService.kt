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
import com.ramble.token.model.InternalTokenStorageException
import com.ramble.token.repository.persistence.entities.RegistrationConfirmationToken
import org.springframework.scheduling.annotation.Async
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.temporal.ChronoUnit

@Service
class UserRegistrationService(
    private val userRepo: UserRepo,
    private val registrationRequestValidator: RegistrationRequestValidator,
    private val bCryptPasswordEncoder: PasswordEncoder,
    private val registrationConfirmationService: RegistrationConfirmationService,
    private val emailSenderService: EmailSenderService,
    private val timeAndIdGenerator: TimeAndIdGenerator
) {

    @Throws(
        UserAlreadyActivatedException::class, UserSuspendedException::class, EmailCredentialNotFoundException::class,
        EmailSendingFailedException::class, InvalidEmailException::class,
        InternalServerException::class, InternalTokenStorageException::class
    )
    suspend fun saveUser(
        registerUserRequest: RegisterUserRequest,
        expirationDurationAmount: Long = 15,
        expiryDurationUnit: ChronoUnit = ChronoUnit.MINUTES
    ): RegisteredUserResponse {
        val now = timeAndIdGenerator.getCurrentTime()
        registrationRequestValidator.getRegistrationRequestError()?.let { throw InvalidEmailException() }
        val userToSave = registerUserRequest.copy(password = bCryptPasswordEncoder.encode(registerUserRequest.password))
        val newlyRegisteredUser = userRepo.saveNewUser(userToSave)
        val confirmRegistrationToken = registrationConfirmationService.addRegistrationConfirmationToken(
            email = newlyRegisteredUser.email,
            now = now,
            expirationDurationAmount = expirationDurationAmount,
            expiryDurationUnit = expiryDurationUnit
        )
        val registeredUserResponse = RegisteredUserResponse(
            userId = newlyRegisteredUser.id,
            email = confirmRegistrationToken.email
        )
        sendConfirmRegistrationEmail(confirmRegistrationToken, newlyRegisteredUser)
        return registeredUserResponse
    }

    @Throws(
        UserNotFoundException::class, UserAlreadyActivatedException::class,
        UserSuspendedException::class, InvalidRegistrationConfirmationToken::class,
        InternalServerException::class, InternalTokenStorageException::class
    )
    suspend fun confirmToken(token: String?): RegisteredUserResponse {
        val now = timeAndIdGenerator.getCurrentTime()
        token ?: throw InvalidRegistrationConfirmationToken()
        val confirmationToken = registrationConfirmationService.processRegistrationConfirmationToken(token, now)
            ?: throw InvalidRegistrationConfirmationToken()
        val applicationUser = userRepo.activateRegisteredUser(email = confirmationToken.email)

        try {
            // Delete old-registered users. This can happen if user registered with an email address, but didn't confirm it.
            userRepo.deleteUsersWithEmailAndAccountStatus(
                email = applicationUser.email,
                accountStatus = AccountStatus.Registered
            )
        } catch (e: Exception) {
            // ignore!
        }

        return RegisteredUserResponse(userId = applicationUser.id, email = applicationUser.email)
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