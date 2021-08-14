package com.ramble.identity.repo

import com.ramble.identity.models.*
import com.ramble.identity.repo.persistence.UserSqlRepo
import com.ramble.identity.repo.persistence.entity.ApplicationUserEntity
import com.ramble.identity.utils.TimeAndIdGenerator
import com.ramble.identity.utils.valueOf
import com.ramble.token.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Repository

typealias Email = String

@Repository
class UserRepo(
    private val userSqlRepo: UserSqlRepo,
    private val timeAndIdGenerator: TimeAndIdGenerator
) {

    @Throws(UserAlreadyActivatedException::class, UserSuspendedException::class)
    suspend fun saveNewUser(registerUserRequest: RegisterUserRequest): ApplicationUser =
        coroutineScope {
            withContext(Dispatchers.IO) {
                val currentTimeInSeconds = timeAndIdGenerator.getCurrentTimeInSeconds()
                val id = timeAndIdGenerator.getTimeBasedId()
                when (getApplicationUser(email = registerUserRequest.email)?.accountStatus) {
                    AccountStatus.Activated -> throw UserAlreadyActivatedException()
                    AccountStatus.Suspended -> throw UserSuspendedException()
                    AccountStatus.Registered -> userSqlRepo.deleteById(registerUserRequest.email) // delete old entry
                }
                val user = registerUserRequest.toApplicationUser(
                    roles = listOf(Roles.User),
                    accountStatus = AccountStatus.Registered,
                    registrationDateInSeconds = currentTimeInSeconds,
                    id = id
                )
                userSqlRepo.save(user.toApplicationUserEntity()).toApplicationUser()
            }
        }

    @Throws(UserNotFoundException::class, UserAlreadyActivatedException::class, UserSuspendedException::class)
    suspend fun activateRegisteredUser(email: Email): ApplicationUser =
        coroutineScope {
            withContext(Dispatchers.IO) {
                val currentTimeInSeconds = timeAndIdGenerator.getCurrentTimeInSeconds()
                val user = userSqlRepo.findById(email).value?.toApplicationUser() ?: throw UserNotFoundException()
                when (user.accountStatus) {
                    AccountStatus.Activated -> throw UserAlreadyActivatedException()
                    AccountStatus.Suspended -> throw UserSuspendedException()
                    AccountStatus.Registered -> {
                        val activatedUser = user.copy(
                            accountStatus = AccountStatus.Activated,
                            activationDateInSeconds = currentTimeInSeconds
                        )
                        userSqlRepo.save(activatedUser.toApplicationUserEntity()).toApplicationUser()
                    }
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
        coroutineScope {
            withContext(Dispatchers.IO) {
                userSqlRepo.findById(email).value?.toApplicationUser()
            }
        }

    private fun ApplicationUser.toApplicationUserEntity(): ApplicationUserEntity =
        ApplicationUserEntity(
            id = id,
            email = email,
            password = password ?: "",
            roles = roles.map { it.name },
            accountStatus = accountStatus.name,
            registrationDateInSeconds = registrationDateInSeconds,
            firstName = firstName ?: "",
            lastName = lastName ?: "",
            nickname = nickname ?: "",
            age = age ?: -1,
            gender = gender.name,
            houseNumber = houseNumber ?: "",
            streetName = streetName ?: "",
            postCode = postCode ?: "",
            city = city ?: "",
            country = country ?: "",
            activationDateInSeconds = activationDateInSeconds ?: -1
        )

    private fun ApplicationUserEntity.toApplicationUser(): ApplicationUser =
        ApplicationUser(
            id = id,
            email = email,
            password = password.ifBlank { null },
            roles = roles.mapNotNull { valueOf<Roles>(it) },
            accountStatus = valueOf<AccountStatus>(accountStatus) ?: AccountStatus.Registered,
            registrationDateInSeconds = registrationDateInSeconds,
            firstName = firstName.ifBlank { null },
            lastName = lastName.ifBlank { null },
            nickname = nickname.ifBlank { null },
            age = age.takeIf { it > 0 },
            gender = valueOf<Gender>(gender) ?: Gender.Undisclosed,
            houseNumber = houseNumber.ifBlank { null },
            streetName = streetName.ifBlank { null },
            postCode = postCode.ifBlank { null },
            city = city.ifBlank { null },
            country = country.ifBlank { null },
            activationDateInSeconds = activationDateInSeconds.takeIf { it > 0 }
        )

}