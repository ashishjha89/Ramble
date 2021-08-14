package com.ramble.identity.repo

import com.ramble.identity.models.*
import com.ramble.identity.repo.persistence.UserSqlRepo
import com.ramble.identity.repo.persistence.entity.ApplicationUserEntity
import com.ramble.identity.utils.TimeAndIdGenerator
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.BDDMockito.*
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import java.time.Instant
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserRepoTest {

    private val userSqlRepo = mock(UserSqlRepo::class.java)
    private val timeAndIdGenerator = mock(TimeAndIdGenerator::class.java)

    private val userRepo = UserRepo(userSqlRepo, timeAndIdGenerator)

    @Test(expected = UserAlreadyActivatedException::class)
    fun `saveNewUser should throw UserAlreadyActivatedException if user is already activated`() = runBlocking<Unit> {
        val currentTimeInSeconds = Instant.now().epochSecond
        val timeBasedId = 12345L
        val email = "someEmailId"
        val password = "somePassword"
        val registerUserResponse = RegisterUserRequest(email = email, password = password)
        val userEntity = getApplicationUserEntity(email = email, accountStatus = AccountStatus.Activated.name)

        // Stub
        given(timeAndIdGenerator.getCurrentTimeInSeconds()).willReturn(currentTimeInSeconds)
        given(timeAndIdGenerator.getTimeBasedId()).willReturn(timeBasedId)
        given(userSqlRepo.findById(email)).willReturn(Optional.of(userEntity))

        // Call method and assert
        userRepo.saveNewUser(registerUserResponse)
    }

    @Test(expected = UserSuspendedException::class)
    fun `saveNewUser should throw UserSuspendedException if user is suspended`() = runBlocking<Unit> {
        val currentTimeInSeconds = Instant.now().epochSecond
        val timeBasedId = 12345L
        val email = "someEmailId"
        val password = "somePassword"
        val registerUserResponse = RegisterUserRequest(email = email, password = password)
        val userEntity = getApplicationUserEntity(email = email, accountStatus = AccountStatus.Suspended.name)

        // Stub
        given(timeAndIdGenerator.getCurrentTimeInSeconds()).willReturn(currentTimeInSeconds)
        given(timeAndIdGenerator.getTimeBasedId()).willReturn(timeBasedId)
        given(userSqlRepo.findById(email)).willReturn(Optional.of(userEntity))

        // Call method and assert
        userRepo.saveNewUser(registerUserResponse)
    }

    @Test
    fun `saveNewUser should save user if user is new`() = runBlocking<Unit> {
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
        val registerUserRequest = RegisterUserRequest(
            email = email,
            password = password,
            firstName = firstName,
            lastName = lastName,
            nickname = nickName,
            age = age,
            gender = gender,
            houseNumber = houseNumber,
            streetName = streetName,
            postCode = postCode,
            city = city,
            country = country
        )
        val registerUserRequestSpy = spy(registerUserRequest)
        val userEntity = getApplicationUserEntity(
            id = timeBasedId.toString(),
            email = email,
            password = password,
            roles = listOf(Roles.User.name),
            accountStatus = AccountStatus.Registered.name,
            registrationDateInSeconds = currentTimeInSeconds,
            firstName = firstName,
            lastName = lastName,
            nickname = nickName,
            age = age,
            gender = gender,
            houseNumber = houseNumber,
            streetName = streetName,
            postCode = postCode,
            city = city,
            country = country
        )
        val expectedApplicationUser = ApplicationUser(
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

        // Stub
        given(timeAndIdGenerator.getCurrentTimeInSeconds()).willReturn(currentTimeInSeconds)
        given(timeAndIdGenerator.getTimeBasedId()).willReturn(timeBasedId)
        given(userSqlRepo.findById(email)).willReturn(Optional.empty())
        given(userSqlRepo.save(any())).willReturn(userEntity)

        // Call method and assert
        assertEquals(expectedApplicationUser, userRepo.saveNewUser(registerUserRequestSpy))
        verify(userSqlRepo, times(0)).deleteById(email)
        verify(registerUserRequestSpy, times(1)).toApplicationUser(
            roles = listOf(Roles.User),
            accountStatus = AccountStatus.Registered,
            registrationDateInSeconds = currentTimeInSeconds,
            id = timeBasedId
        )
        verify(userSqlRepo).save(any())
    }

    @Test
    fun `saveNewUser should save user if user registered before but not activated`() = runBlocking<Unit> {
        val currentTimeInSeconds = Instant.now().epochSecond
        val timeBasedId = 12345L
        val email = "someEmailId"
        val password = "somePassword"
        val registerUserRequest = RegisterUserRequest(email = email, password = password)
        val registerUserRequestSpy = spy(registerUserRequest)
        val userEntityOld = getApplicationUserEntity(email = email, accountStatus = AccountStatus.Registered.name)
        val userEntityNew = getApplicationUserEntity(
            id = timeBasedId.toString(),
            email = email,
            password = password,
            roles = listOf(Roles.User.name),
            accountStatus = AccountStatus.Registered.name,
            registrationDateInSeconds = currentTimeInSeconds
        )
        val expectedApplicationUser = ApplicationUser(
            id = timeBasedId.toString(),
            email = email,
            password = password,
            roles = listOf(Roles.User),
            accountStatus = AccountStatus.Registered,
            registrationDateInSeconds = currentTimeInSeconds,
        )

        // Stub
        given(timeAndIdGenerator.getCurrentTimeInSeconds()).willReturn(currentTimeInSeconds)
        given(timeAndIdGenerator.getTimeBasedId()).willReturn(timeBasedId)
        given(userSqlRepo.findById(email)).willReturn(Optional.of(userEntityOld))
        given(userSqlRepo.save(any())).willReturn(userEntityNew)

        // Call method and assert
        assertEquals(expectedApplicationUser, userRepo.saveNewUser(registerUserRequestSpy))
        verify(userSqlRepo).deleteById(email)
        verify(registerUserRequestSpy, times(1)).toApplicationUser(
            roles = listOf(Roles.User),
            accountStatus = AccountStatus.Registered,
            registrationDateInSeconds = currentTimeInSeconds,
            id = timeBasedId
        )
        verify(userSqlRepo).save(any())
    }

    @Test(expected = UserNotFoundException::class)
    fun `activateRegisteredUser should throw UserNotFoundException if user has not registered yet`() =
        runBlocking<Unit> {
            val currentTimeInSeconds = Instant.now().epochSecond
            val email = "someEmailId"

            // Stub
            given(timeAndIdGenerator.getCurrentTimeInSeconds()).willReturn(currentTimeInSeconds)
            given(userSqlRepo.findById(email)).willReturn(Optional.empty())

            // Call method and assert
            userRepo.activateRegisteredUser(email)
        }

    @Test(expected = UserAlreadyActivatedException::class)
    fun `activateRegisteredUser should throw UserAlreadyActivatedException if user is already activated`() =
        runBlocking<Unit> {
            val currentTimeInSeconds = Instant.now().epochSecond
            val email = "someEmailId"
            val userEntityActivated =
                getApplicationUserEntity(email = email, accountStatus = AccountStatus.Activated.name)

            // Stub
            given(timeAndIdGenerator.getCurrentTimeInSeconds()).willReturn(currentTimeInSeconds)
            given(userSqlRepo.findById(email)).willReturn(Optional.of(userEntityActivated))

            // Call method and assert
            userRepo.activateRegisteredUser(email)
        }

    @Test(expected = UserSuspendedException::class)
    fun `activateRegisteredUser should throw UserSuspendedException if user is Suspended`() = runBlocking<Unit> {
        val currentTimeInSeconds = Instant.now().epochSecond
        val email = "someEmailId"
        val userEntityActivated = getApplicationUserEntity(email = email, accountStatus = AccountStatus.Suspended.name)

        // Stub
        given(timeAndIdGenerator.getCurrentTimeInSeconds()).willReturn(currentTimeInSeconds)
        given(userSqlRepo.findById(email)).willReturn(Optional.of(userEntityActivated))

        // Call method and assert
        userRepo.activateRegisteredUser(email)
    }

    @Test
    fun `activateRegisteredUser should set accountStatus to Activated if user has activated before`() = runBlocking {
        val currentTimeInSeconds = Instant.now().epochSecond
        val email = "someEmailId"
        val userEntityRegistered =
            getApplicationUserEntity(email = email, accountStatus = AccountStatus.Registered.name)
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
        given(timeAndIdGenerator.getCurrentTimeInSeconds()).willReturn(currentTimeInSeconds)
        given(userSqlRepo.findById(email)).willReturn(Optional.of(userEntityRegistered))
        given(userSqlRepo.save(any())).willReturn(userEntityActivated)

        // Call method and assert
        assertEquals(expectedApplicationUser, userRepo.activateRegisteredUser(email))
    }

    @Test(expected = UserNotFoundException::class)
    fun `getUserInfo should throw UserNotFoundException if user has not registered yet`() = runBlocking<Unit> {
        val currentTimeInSeconds = Instant.now().epochSecond
        val email = "someEmailId"

        // Stub
        given(timeAndIdGenerator.getCurrentTimeInSeconds()).willReturn(currentTimeInSeconds)
        given(userSqlRepo.findById(email)).willReturn(Optional.empty())

        // Call method and assert
        userRepo.getUserInfo(email)
    }

    @Test(expected = UserNotActivatedException::class)
    fun `getUserInfo should throw UserNotActivatedException if user is not activated`() = runBlocking<Unit> {
        val currentTimeInSeconds = Instant.now().epochSecond
        val email = "someEmailId"
        val userEntityActivated = getApplicationUserEntity(email = email, accountStatus = AccountStatus.Registered.name)

        // Stub
        given(timeAndIdGenerator.getCurrentTimeInSeconds()).willReturn(currentTimeInSeconds)
        given(userSqlRepo.findById(email)).willReturn(Optional.of(userEntityActivated))

        // Call method and assert
        userRepo.getUserInfo(email)
    }

    @Test(expected = UserSuspendedException::class)
    fun `getUserInfo should throw UserSuspendedException if user is Suspended`() = runBlocking<Unit> {
        val currentTimeInSeconds = Instant.now().epochSecond
        val email = "someEmailId"
        val userEntityActivated = getApplicationUserEntity(email = email, accountStatus = AccountStatus.Suspended.name)

        // Stub
        given(timeAndIdGenerator.getCurrentTimeInSeconds()).willReturn(currentTimeInSeconds)
        given(userSqlRepo.findById(email)).willReturn(Optional.of(userEntityActivated))

        // Call method and assert
        userRepo.getUserInfo(email)
    }

    @Test
    fun `getUserInfo should return userInfo if user is Activated`() = runBlocking<Unit> {
        val currentTimeInSeconds = Instant.now().epochSecond
        val email = "someEmailId"
        val userEntityActivated = getApplicationUserEntity(
            email = email,
            accountStatus = AccountStatus.Activated.name,
            roles = listOf(Roles.User.name),
            activationDateInSeconds = currentTimeInSeconds
        )
        val expectedUserInfo = UserInfo(id = userEntityActivated.id, email = userEntityActivated.email)

        // Stub
        given(timeAndIdGenerator.getCurrentTimeInSeconds()).willReturn(currentTimeInSeconds)
        given(userSqlRepo.findById(email)).willReturn(Optional.of(userEntityActivated))

        // Call method and assert
        assertEquals(expectedUserInfo, userRepo.getUserInfo(email))
    }

    @Test
    fun `getApplicationUser should return null user is not present in DB`() = runBlocking {
        val email = "someEmailId"

        // Stub
        given(userSqlRepo.findById(email)).willReturn(Optional.empty())

        // Call method and assert
        assertNull(userRepo.getApplicationUser(email))
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
        assertEquals(expectedApplicationUser, userRepo.getApplicationUser(email))
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