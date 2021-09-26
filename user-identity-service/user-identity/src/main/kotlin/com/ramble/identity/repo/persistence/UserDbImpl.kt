package com.ramble.identity.repo.persistence

import com.ramble.identity.models.*
import com.ramble.identity.repo.Email
import com.ramble.identity.repo.Id
import com.ramble.identity.repo.persistence.entity.ApplicationUserEntity
import com.ramble.identity.utils.valueOf
import com.ramble.token.util.value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class UserDbImpl(private val userSqlRepo: UserSqlRepo) {

    private companion object {
        private const val SQL_TIMEOUT = 500L
    }

    private val logger = LoggerFactory.getLogger(UserDbImpl::class.java)

    @Throws(InternalServerException::class)
    suspend fun getApplicationUser(
        id: Id,
        scope: CoroutineScope,
        timeoutInMilliseconds: Long = SQL_TIMEOUT
    ): ApplicationUser? {
        val deferredTask = scope.async { userSqlRepo.findById(id).value?.toApplicationUser() }
        return try {
            performDeferredTask(deferredTask, timeoutInMilliseconds)
        } catch (e: Exception) {
            logger.error("Exception in Db Operation for getApplicationUser id:$id e:${e.message}")
            if (deferredTask.isActive) deferredTask.cancel()
            throw InternalServerException()
        }
    }

    @Throws(InternalServerException::class)
    suspend fun getApplicationUsersWithEmail(
        email: Email,
        scope: CoroutineScope,
        timeoutInMilliseconds: Long = SQL_TIMEOUT
    ): List<ApplicationUser>? {
        val deferredTask = scope.async { userSqlRepo.getUserByEmail(email)?.map { it.toApplicationUser() } }
        return try {
            performDeferredTask(deferredTask, timeoutInMilliseconds)
        } catch (e: Exception) {
            logger.error("Exception in Db Operation for getApplicationUsersWithEmail email:$email e:${e.message}")
            if (deferredTask.isActive) deferredTask.cancel()
            throw InternalServerException()
        }
    }

    @Throws(InternalServerException::class)
    suspend fun save(
        applicationUser: ApplicationUser,
        scope: CoroutineScope,
        timeoutInMilliseconds: Long = SQL_TIMEOUT
    ): ApplicationUser {
        val deferredTask = scope.async {
            userSqlRepo.save(applicationUser.toApplicationUserEntity()).toApplicationUser()
        }
        return try {
            performDeferredTask(deferredTask, timeoutInMilliseconds)
        } catch (e: Exception) {
            val userInfo = applicationUser.toUserInfo() // so that password is not put in logs.
            logger.error("Exception in Db Operation for saving user:$userInfo e:${e.message}")
            if (deferredTask.isActive) deferredTask.cancel()
            throw InternalServerException()
        }

    }

    @Throws(InternalServerException::class)
    suspend fun deleteUser(
        id: Id,
        scope: CoroutineScope,
        timeoutInMilliseconds: Long = SQL_TIMEOUT
    ) {
        val deferredTask = scope.async { userSqlRepo.deleteById(id) }
        return try {
            performDeferredTask(deferredTask, timeoutInMilliseconds)
        } catch (e: Exception) {
            logger.error("Exception in Db Operation for deleteUser id:$id e:${e.message}")
            if (deferredTask.isActive) deferredTask.cancel()
            throw InternalServerException()
        }
    }

    @Throws(InternalServerException::class)
    suspend fun deleteUsersWithEmailAndAccountStatus(
        email: Email,
        accountStatus: AccountStatus,
        scope: CoroutineScope,
        timeoutInMilliseconds: Long = SQL_TIMEOUT
    ) {
        val usersToDelete = getApplicationUsersWithEmail(email, scope, timeoutInMilliseconds)
            ?.filter { it.accountStatus == accountStatus } ?: emptyList()
        usersToDelete.forEach { deleteUser(it.id, scope, timeoutInMilliseconds) }
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

    private suspend fun <T> performDeferredTask(deferredTask: Deferred<T>, timeoutInMilliseconds: Long): T =
        withTimeout(timeoutInMilliseconds) {
            deferredTask.await()
        }
}