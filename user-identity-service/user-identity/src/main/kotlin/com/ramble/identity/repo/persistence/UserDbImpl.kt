package com.ramble.identity.repo.persistence

import com.ramble.identity.models.*
import com.ramble.identity.repo.Email
import com.ramble.identity.repo.persistence.entity.ApplicationUserEntity
import com.ramble.identity.utils.valueOf
import com.ramble.token.value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.withTimeout
import org.springframework.stereotype.Repository

@Repository
class UserDbImpl(private val userSqlRepo: UserSqlRepo) {

    private companion object {
        private const val SQL_TIMEOUT = 500L
    }

    @Throws(InternalServerException::class)
    suspend fun getApplicationUser(
        email: Email,
        scope: CoroutineScope,
        timeoutInMilliseconds: Long = SQL_TIMEOUT
    ): ApplicationUser? =
        performDeferredTask(
            deferredTask = scope.async { userSqlRepo.findById(email).value?.toApplicationUser() },
            timeoutInMilliseconds
        )

    suspend fun deleteUser(
        email: Email,
        scope: CoroutineScope,
        timeoutInMilliseconds: Long = SQL_TIMEOUT
    ) =
        performDeferredTask(
            deferredTask = scope.async { userSqlRepo.deleteById(email) },
            timeoutInMilliseconds
        )

    suspend fun save(
        applicationUser: ApplicationUser,
        scope: CoroutineScope,
        timeoutInMilliseconds: Long = SQL_TIMEOUT
    ): ApplicationUser =
        performDeferredTask(
            deferredTask = scope.async {
                userSqlRepo.save(applicationUser.toApplicationUserEntity()).toApplicationUser()
            },
            timeoutInMilliseconds
        )

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

    @Throws(InternalServerException::class)
    private suspend fun <T> performDeferredTask(deferredTask: Deferred<T>, timeoutInMilliseconds: Long): T =
        try {
            withTimeout(timeoutInMilliseconds) {
                deferredTask.await()
            }
        } catch (e: Exception) {
            if (deferredTask.isActive) deferredTask.cancel()
            throw InternalServerException()
        }
}