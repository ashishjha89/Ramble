package com.ramble.identity.repo.persistence

import com.ramble.identity.models.AccountStatus
import com.ramble.identity.models.ApplicationUser
import com.ramble.identity.models.Gender
import com.ramble.identity.models.Roles
import com.ramble.identity.repo.persistence.entity.ApplicationUserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.verify
import org.mockito.Mockito.mock
import java.time.Instant
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserDbImplTest {

    private val userSqlRepo = mock(UserSqlRepo::class.java)

    private val userDbImpl = UserDbImpl(userSqlRepo)

    private val scope = CoroutineScope(Job())

    @Test
    fun `getApplicationUser should return null user is not present in DB`() = runBlocking {
        val email = "someEmailId"

        // Stub
        given(userSqlRepo.findById(email)).willReturn(Optional.empty())

        // Call method and assert
        assertNull(userDbImpl.getApplicationUser(email, scope))
    }

    @Test
    fun `getApplicationUser should return user when user is present in DB`() = runBlocking {
        val email = "someEmailId"
        val currentTimeInSeconds = Instant.now().epochSecond
        val userEntityActivated = getApplicationUserEntity(
            email = email,
            accountStatus = AccountStatus.Activated.name,
            roles = listOf(Roles.User.name),
            activationDateInSeconds = currentTimeInSeconds
        )
        val expectedApplicationUser = ApplicationUser(
            id = userEntityActivated.id,
            email = userEntityActivated.email,
            password = userEntityActivated.password,
            roles = listOf(Roles.User),
            accountStatus = AccountStatus.Activated,
            registrationDateInSeconds = userEntityActivated.registrationDateInSeconds,
            activationDateInSeconds = userEntityActivated.activationDateInSeconds
        )

        // Stub
        given(userSqlRepo.findById(email)).willReturn(Optional.of(userEntityActivated))

        // Call method and assert
        assertEquals(expectedApplicationUser, userDbImpl.getApplicationUser(email, scope))
    }

    @Test
    fun deleteUserTest() = runBlocking<Unit> {
        val email = "someEmailId"
        // Call method
        userDbImpl.deleteUser(email, scope)

        // Verify
        verify(userSqlRepo).deleteById(email)
    }

    @Test
    fun saveUserTest() = runBlocking<Unit> {
        val currentTimeInSeconds = Instant.now().epochSecond
        val timeBasedId = 12345L
        val email = "someEmailId"
        val password = "somePassword"
        val firstName = "someFirstName"
        val lastName = "someLastName"
        val nickName = "someNickName"
        val age = 32
        val gender = Gender.Male.name
        val houseNumber = "123"
        val streetName = "Some Street"
        val postCode = "Some Postcode"
        val city = "Amsterdam"
        val country = "The Netherlands"
        val applicationUser = ApplicationUser(
            id = timeBasedId.toString(),
            email = email,
            password = password,
            roles = listOf(Roles.User),
            accountStatus = AccountStatus.Registered,
            registrationDateInSeconds = currentTimeInSeconds,
            firstName = firstName,
            lastName = lastName,
            nickname = nickName,
            age = age,
            gender = Gender.valueOf(gender),
            houseNumber = houseNumber,
            streetName = streetName,
            postCode = postCode,
            city = city,
            country = country
        )
        val userEntity = ApplicationUserEntity(
            id = applicationUser.id,
            email = applicationUser.email,
            password = applicationUser.password ?: "",
            roles = applicationUser.roles.map { it.name },
            accountStatus = applicationUser.accountStatus.name,
            registrationDateInSeconds = applicationUser.registrationDateInSeconds,
            activationDateInSeconds = applicationUser.activationDateInSeconds ?: -1,
            firstName = applicationUser.firstName ?: "",
            lastName = applicationUser.lastName ?: "",
            nickname = applicationUser.nickname ?: "",
            age = applicationUser.age ?: -1,
            gender = applicationUser.gender.name,
            houseNumber = applicationUser.houseNumber ?: "",
            streetName = applicationUser.streetName ?: "",
            postCode = applicationUser.postCode ?: "",
            city = applicationUser.city ?: "",
            country = applicationUser.country ?: ""
        )

        // Stub
        given(userSqlRepo.save(any())).willReturn(userEntity)

        // Call method and assert
        assertEquals(applicationUser, userDbImpl.save(applicationUser, scope))
    }

    private fun getApplicationUserEntity(
        id: String = "someUserId",
        email: String = "someEmailId",
        password: String = "somePassword",
        roles: List<String> = emptyList(),
        accountStatus: String = "",
        registrationDateInSeconds: Long = -1,
        firstName: String = "",
        lastName: String = "",
        nickname: String = "",
        age: Int = -1,
        gender: String = Gender.Undisclosed.name,
        houseNumber: String = "",
        streetName: String = "",
        postCode: String = "",
        city: String = "",
        country: String = "",
        activationDateInSeconds: Long = -1
    ) =
        ApplicationUserEntity(
            id = id,
            email = email,
            password = password,
            roles = roles,
            accountStatus = accountStatus,
            registrationDateInSeconds = registrationDateInSeconds,
            firstName = firstName,
            lastName = lastName,
            nickname = nickname,
            age = age,
            gender = gender,
            houseNumber = houseNumber,
            streetName = streetName,
            postCode = postCode,
            city = city,
            country = country,
            activationDateInSeconds = activationDateInSeconds
        )
}