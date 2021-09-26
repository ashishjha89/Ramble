package com.ramble.identity.repo

import com.ramble.identity.models.*
import com.ramble.identity.repo.persistence.UserDbImpl
import com.ramble.identity.utils.TimeAndIdGenerator
import com.ramble.identity.utils.UserIdentityCoroutineScopeBuilder
import kotlinx.coroutines.CoroutineScope
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

typealias Id = String

typealias Email = String

@Repository
class UserRepo(
    private val userDbImpl: UserDbImpl,
    private val timeAndIdGenerator: TimeAndIdGenerator,
    private val coroutineScopeBuilder: UserIdentityCoroutineScopeBuilder
) {

    private val logger = LoggerFactory.getLogger(UserRepo::class.java)

    @Throws(UserAlreadyActivatedException::class, UserSuspendedException::class, InternalServerException::class)
    suspend fun saveNewUser(registerUserRequest: RegisterUserRequest): ApplicationUser {
        val scope = coroutineScopeBuilder.defaultIoScope
        val currentTimeInSeconds = timeAndIdGenerator.getCurrentTimeInSeconds()
        val id = timeAndIdGenerator.getTimeBasedId()
        val users = userDbImpl.getApplicationUsersWithEmail(registerUserRequest.email, scope)
        val activatedUsers = users?.filter { it.accountStatus == AccountStatus.Activated } ?: emptyList()
        val suspendedUsers = users?.filter { it.accountStatus == AccountStatus.Suspended } ?: emptyList()
        val registeredUsers = users?.filter { it.accountStatus == AccountStatus.Registered } ?: emptyList()

        if (activatedUsers.isNotEmpty()) {
            logger.warn("saveNewUser (registerUser) called for already Activated user. email:${registerUserRequest.email}")
            throw UserAlreadyActivatedException()
        }
        if (suspendedUsers.isNotEmpty()) {
            logger.warn("saveNewUser (registerUser) called for Suspended user email:${registerUserRequest.email}")
            throw UserSuspendedException()
        }
        if (registeredUsers.isNotEmpty()) {
            logger.warn("saveNewUser (registerUser) called for already registered user email:${registerUserRequest.email}")
            registeredUsers.forEach {
                userDbImpl.deleteUser(it.id, scope) // delete old entry
            }
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
        val user = getApplicationUserWithEmail(email, scope)
            ?: let {
                logger.warn("activateRegisteredUser called for non-registered user email:$email")
                throw UserNotFoundException()
            }
        return when (user.accountStatus) {
            AccountStatus.Activated -> {
                logger.warn("activateRegisteredUser called for already Activated user email:$email")
                throw UserAlreadyActivatedException()
            }
            AccountStatus.Suspended -> {
                logger.warn("activateRegisteredUser called for Suspended user email:$email")
                throw UserSuspendedException()
            }
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

    /**
     * Return UserInfo if user is activated.
     */
    @Throws(
        UserNotFoundException::class, UserSuspendedException::class,
        UserNotActivatedException::class, InternalServerException::class
    )
    suspend fun getActiveUserInfo(id: Id): UserInfo {
        val applicationUser = getApplicationUser(id) ?: let {
            logger.warn("getActiveUserInfo called for non-existing user id:$id")
            throw UserNotFoundException()
        }
        return when (applicationUser.accountStatus) {
            AccountStatus.Suspended -> {
                logger.info("getUserInfo for Suspended user id:$id")
                throw UserSuspendedException()
            }
            AccountStatus.Registered -> {
                logger.info("getUserInfo for Registered user (but not activated) id:$id")
                throw UserNotActivatedException()
            }
            AccountStatus.Activated -> applicationUser.toUserInfo()
        }
    }

    @Throws(InternalServerException::class)
    suspend fun getApplicationUser(id: Id): ApplicationUser? =
        userDbImpl.getApplicationUser(id, coroutineScopeBuilder.defaultIoScope)

    /**
     * Return most recent Active User or most recent registered user with this email.
     */
    @Throws(InternalServerException::class)
    suspend fun getApplicationUserWithEmail(email: Email, scope: CoroutineScope? = null): ApplicationUser? {
        val ioScope = scope ?: coroutineScopeBuilder.defaultIoScope
        val usersWithEmail = userDbImpl.getApplicationUsersWithEmail(email, ioScope) ?: return null
        val activeUsers = usersWithEmail
            .filter { it.accountStatus == AccountStatus.Activated }
            .filter { it.activationDateInSeconds != null && it.activationDateInSeconds > 0 }
        if (activeUsers.size > 1) {
            logger.warn("More than 1 Active Users with email:$email")
        }
        return activeUsers.maxByOrNull { it.activationDateInSeconds!! }
            ?: usersWithEmail.maxByOrNull { it.registrationDateInSeconds }
    }

}