package com.ramble.identity.repo

import com.ramble.identity.models.*
import com.ramble.identity.utils.TimeAndIdGenerator
import org.springframework.stereotype.Repository

typealias Email = String

@Repository
class UserRepo(private val timeAndIdGenerator: TimeAndIdGenerator) {

    private val userMap = mutableMapOf<Email, ApplicationUser>()

    @Throws(UserAlreadyActivatedException::class, UserSuspendedException::class)
    fun saveNewUser(registerUserRequest: RegisterUserRequest): ApplicationUser {
        val currentTimeInSeconds = timeAndIdGenerator.getCurrentTimeInSeconds()
        val id = timeAndIdGenerator.getTimeBasedId()
        when (getApplicationUser(email = registerUserRequest.email)?.accountStatus) {
            AccountStatus.Activated -> throw UserAlreadyActivatedException()
            AccountStatus.Suspended -> throw UserSuspendedException()
            AccountStatus.Registered -> userMap.remove(registerUserRequest.email) // delete old entry
        }
        val user = registerUserRequest.toApplicationUser(
                roles = listOf(Roles.User),
                accountStatus = AccountStatus.Registered,
                registrationDateInSeconds = currentTimeInSeconds,
                id = id
        )
        userMap[registerUserRequest.email] = user
        return user
    }

    /**
     * Return true if newly-registered user accountStatus is changed to activated.
     */
    @Throws(UserNotFoundException::class, UserAlreadyActivatedException::class, UserSuspendedException::class)
    fun activateRegisteredUser(email: Email): Boolean {
        val currentTimeInSeconds = timeAndIdGenerator.getCurrentTimeInSeconds()
        val user = getApplicationUser(email) ?: throw UserNotFoundException()
        when (user.accountStatus) {
            AccountStatus.Activated -> throw UserAlreadyActivatedException()
            AccountStatus.Suspended -> throw UserSuspendedException()
            AccountStatus.Registered -> {
                val activatedUser = user.copy(
                        accountStatus = AccountStatus.Activated,
                        activationDateInSeconds = currentTimeInSeconds
                )
                userMap[email] = activatedUser
                return true
            }
        }
    }

    @Throws(UserNotFoundException::class, UserSuspendedException::class, UserNotActivatedException::class)
    fun getUserInfo(email: Email): UserInfo {
        val applicationUser = getApplicationUser(email) ?: throw UserNotFoundException()
        return when (applicationUser.accountStatus) {
            AccountStatus.Suspended -> throw UserSuspendedException()
            AccountStatus.Registered -> throw UserNotActivatedException()
            AccountStatus.Activated -> applicationUser.toUserInfo()
        }
    }

    fun getApplicationUser(email: Email): ApplicationUser? =
            userMap[email]

}