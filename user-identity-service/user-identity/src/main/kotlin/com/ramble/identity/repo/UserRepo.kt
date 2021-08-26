package com.ramble.identity.repo

import com.ramble.identity.models.*
import com.ramble.identity.repo.persistence.UserDbImpl
import com.ramble.identity.utils.CoroutineScopeBuilder
import com.ramble.identity.utils.TimeAndIdGenerator
import org.springframework.stereotype.Repository

typealias Email = String

@Repository
class UserRepo(
    private val userDbImpl: UserDbImpl,
    private val timeAndIdGenerator: TimeAndIdGenerator,
    private val coroutineScopeBuilder: CoroutineScopeBuilder
) {

    @Throws(UserAlreadyActivatedException::class, UserSuspendedException::class)
    suspend fun saveNewUser(registerUserRequest: RegisterUserRequest): ApplicationUser {
        val scope = coroutineScopeBuilder.defaultIoScope
        val currentTimeInSeconds = timeAndIdGenerator.getCurrentTimeInSeconds()
        val id = timeAndIdGenerator.getTimeBasedId()
        when (userDbImpl.getApplicationUser(registerUserRequest.email, scope)?.accountStatus) {
            AccountStatus.Activated -> throw UserAlreadyActivatedException()
            AccountStatus.Suspended -> throw UserSuspendedException()
            AccountStatus.Registered -> userDbImpl.deleteUser(registerUserRequest.email, scope) // delete old entry
        }
        val user = registerUserRequest.toApplicationUser(
            roles = listOf(Roles.User),
            accountStatus = AccountStatus.Registered,
            registrationDateInSeconds = currentTimeInSeconds,
            id = id
        )
        return userDbImpl.save(user, scope)
    }

    @Throws(UserNotFoundException::class, UserAlreadyActivatedException::class, UserSuspendedException::class)
    suspend fun activateRegisteredUser(email: Email): ApplicationUser {
        val scope = coroutineScopeBuilder.defaultIoScope
        val currentTimeInSeconds = timeAndIdGenerator.getCurrentTimeInSeconds()
        val user = userDbImpl.getApplicationUser(email, scope) ?: throw UserNotFoundException()
        return when (user.accountStatus) {
            AccountStatus.Activated -> throw UserAlreadyActivatedException()
            AccountStatus.Suspended -> throw UserSuspendedException()
            AccountStatus.Registered -> {
                val activatedUser = user.copy(
                    accountStatus = AccountStatus.Activated,
                    activationDateInSeconds = currentTimeInSeconds
                )
                userDbImpl.save(activatedUser, scope)
            }
        }
    }

    @Throws(UserNotFoundException::class, UserSuspendedException::class, UserNotActivatedException::class)
    suspend fun getUserInfo(email: Email): UserInfo {
        val applicationUser = getApplicationUser(email) ?: throw UserNotFoundException()
        return when (applicationUser.accountStatus) {
            AccountStatus.Suspended -> throw UserSuspendedException()
            AccountStatus.Registered -> throw UserNotActivatedException()
            AccountStatus.Activated -> applicationUser.toUserInfo()
        }
    }

    suspend fun getApplicationUser(email: Email): ApplicationUser? =
        userDbImpl.getApplicationUser(email, coroutineScopeBuilder.defaultIoScope)

}