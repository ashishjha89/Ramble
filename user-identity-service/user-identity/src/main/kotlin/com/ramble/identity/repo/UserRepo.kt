package com.ramble.identity.repo

import com.ramble.identity.models.*
import com.ramble.identity.repo.persistence.UserDbImpl
import com.ramble.identity.utils.TimeAndIdGenerator
import com.ramble.identity.utils.UserIdentityCoroutineScopeBuilder
import org.springframework.stereotype.Repository

typealias Id = String

typealias Email = String

@Repository
class UserRepo(
    private val userDbImpl: UserDbImpl,
    private val timeAndIdGenerator: TimeAndIdGenerator,
    private val coroutineScopeBuilder: UserIdentityCoroutineScopeBuilder
) {

    @Throws(UserAlreadyActivatedException::class, UserSuspendedException::class, InternalServerException::class)
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

    @Throws(
        UserNotFoundException::class, UserAlreadyActivatedException::class,
        UserSuspendedException::class, InternalServerException::class
    )
    suspend fun activateRegisteredUser(email: Email): ApplicationUser {
        val scope = coroutineScopeBuilder.defaultIoScope
        val currentTimeInSeconds = timeAndIdGenerator.getCurrentTimeInSeconds()
        val user = userDbImpl.getApplicationUserFromEmail(email, scope) ?: throw UserNotFoundException()
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

    @Throws(InternalServerException::class)
    suspend fun deleteUsersWithEmailAndAccountStatus(email: Email, accountStatus: AccountStatus) =
        userDbImpl.deleteUsersWithEmailAndAccountStatus(email, accountStatus, coroutineScopeBuilder.defaultIoScope)

    @Throws(
        UserNotFoundException::class, UserSuspendedException::class,
        UserNotActivatedException::class, InternalServerException::class
    )
    suspend fun getUserInfo(id: Id): UserInfo {
        val applicationUser = getApplicationUser(id) ?: throw UserNotFoundException()
        return when (applicationUser.accountStatus) {
            AccountStatus.Suspended -> throw UserSuspendedException()
            AccountStatus.Registered -> throw UserNotActivatedException()
            AccountStatus.Activated -> applicationUser.toUserInfo()
        }
    }

    @Throws(InternalServerException::class)
    suspend fun getApplicationUser(id: Id): ApplicationUser? =
        userDbImpl.getApplicationUser(id, coroutineScopeBuilder.defaultIoScope)

    @Throws(InternalServerException::class)
    suspend fun getApplicationUserFromEmail(email: Email): ApplicationUser? =
        userDbImpl.getApplicationUserFromEmail(email, coroutineScopeBuilder.defaultIoScope)

}