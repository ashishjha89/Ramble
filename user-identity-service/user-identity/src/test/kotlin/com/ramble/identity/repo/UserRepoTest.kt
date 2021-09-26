package com.ramble.identity.repo

import com.ramble.identity.models.*
import com.ramble.identity.repo.persistence.UserDbImpl
import com.ramble.identity.utils.TimeAndIdGenerator
import com.ramble.identity.utils.UserIdentityCoroutineScopeBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.BDDMockito.*
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UserRepoTest {

    private val userDbImpl = mock(UserDbImpl::class.java)
    private val timeAndIdGenerator = mock(TimeAndIdGenerator::class.java)
    private val coroutineScopeBuilder = mock(UserIdentityCoroutineScopeBuilder::class.java)
    private val scope = mock(CoroutineScope::class.java)

    private val userRepo = UserRepo(userDbImpl, timeAndIdGenerator, coroutineScopeBuilder)

    @Before
    fun setup() {
        given(coroutineScopeBuilder.defaultIoScope).willReturn(scope)
    }

    @Test(expected = UserAlreadyActivatedException::class)
    fun `saveNewUser should throw UserAlreadyActivatedException if user is already activated`() = runBlocking<Unit> {
        val currentTimeInSeconds = Instant.now().epochSecond
        val timeBasedId = 12345L
        val email = "someEmailId"
        val password = "somePassword"
        val registerUserResponse = RegisterUserRequest(email = email, password = password)
        val user1 = getApplicationUser(id = "123", email = email, accountStatus = AccountStatus.Activated)
        val user2 = getApplicationUser(id = "456", email = email, accountStatus = AccountStatus.Registered)

        // Stub
        given(timeAndIdGenerator.getCurrentTimeInSeconds()).willReturn(currentTimeInSeconds)
        given(timeAndIdGenerator.getTimeBasedId()).willReturn(timeBasedId)
        given(userDbImpl.getApplicationUsersWithEmail(email, scope)).willReturn(listOf(user1, user2))

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
        val user1 = getApplicationUser(id = "123", email = email, accountStatus = AccountStatus.Suspended)
        val user2 = getApplicationUser(id = "456", email = email, accountStatus = AccountStatus.Registered)

        // Stub
        given(timeAndIdGenerator.getCurrentTimeInSeconds()).willReturn(currentTimeInSeconds)
        given(timeAndIdGenerator.getTimeBasedId()).willReturn(timeBasedId)
        given(userDbImpl.getApplicationUsersWithEmail(email, scope)).willReturn(listOf(user1, user2))

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
        val gender = Gender.Male
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
            gender = gender.name,
            houseNumber = houseNumber,
            streetName = streetName,
            postCode = postCode,
            city = city,
            country = country
        )
        val registerUserRequestSpy = spy(registerUserRequest)
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
            gender = gender,
            houseNumber = houseNumber,
            streetName = streetName,
            postCode = postCode,
            city = city,
            country = country
        )

        // Stub
        given(timeAndIdGenerator.getCurrentTimeInSeconds()).willReturn(currentTimeInSeconds)
        given(timeAndIdGenerator.getTimeBasedId()).willReturn(timeBasedId)
        given(userDbImpl.getApplicationUsersWithEmail(email, scope)).willReturn(emptyList())
        given(userDbImpl.save(expectedApplicationUser, scope)).willReturn(expectedApplicationUser)

        // Call method and assert
        assertEquals(expectedApplicationUser, userRepo.saveNewUser(registerUserRequestSpy))
        verify(userDbImpl, times(0)).deleteUser(email, scope)
        verify(registerUserRequestSpy, times(1)).toApplicationUser(
            roles = listOf(Roles.User),
            accountStatus = AccountStatus.Registered,
            registrationDateInSeconds = currentTimeInSeconds,
            id = timeBasedId
        )
        verify(userDbImpl).save(expectedApplicationUser, scope)
    }

    @Test
    fun `saveNewUser should save user if user registered before but not activated`() = runBlocking<Unit> {
        val currentTimeInSeconds = Instant.now().epochSecond
        val timeBasedId = 12345L
        val email = "someEmailId"
        val password = "somePassword"
        val registerUserRequest = RegisterUserRequest(email = email, password = password)
        val registerUserRequestSpy = spy(registerUserRequest)
        val userOld1 = getApplicationUser(id = "123", email = email, accountStatus = AccountStatus.Registered)
        val userOld2 = getApplicationUser(id = "456", email = email, accountStatus = AccountStatus.Registered)
        val userNew = ApplicationUser(
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
        given(userDbImpl.getApplicationUsersWithEmail(email, scope)).willReturn(listOf(userOld1, userOld2))
        given(userDbImpl.save(userNew, scope)).willReturn(userNew)

        // Call method and assert
        assertEquals(userNew, userRepo.saveNewUser(registerUserRequestSpy))
        verify(userDbImpl).deleteUser("123", scope)
        verify(userDbImpl).deleteUser("456", scope)
        verify(registerUserRequestSpy).toApplicationUser(
            roles = listOf(Roles.User),
            accountStatus = AccountStatus.Registered,
            registrationDateInSeconds = currentTimeInSeconds,
            id = timeBasedId
        )
        verify(userDbImpl).save(userNew, scope)
    }

    @Test(expected = UserNotFoundException::class)
    fun `activateRegisteredUser should throw UserNotFoundException if user has not registered yet`() =
        runBlocking<Unit> {
            val currentTimeInSeconds = Instant.now().epochSecond
            val email = "someEmailId"

            // Stub
            given(timeAndIdGenerator.getCurrentTimeInSeconds()).willReturn(currentTimeInSeconds)
            given(userDbImpl.getApplicationUsersWithEmail(email, scope)).willReturn(emptyList())

            // Call method and assert
            userRepo.activateRegisteredUser(email)
        }

    @Test(expected = UserAlreadyActivatedException::class)
    fun `activateRegisteredUser should throw UserAlreadyActivatedException if user is already activated`() =
        runBlocking<Unit> {
            val currentTimeInSeconds = Instant.now().epochSecond
            val email = "someEmailId"
            val userActivated = getApplicationUser(email = email, accountStatus = AccountStatus.Activated)

            // Stub
            given(timeAndIdGenerator.getCurrentTimeInSeconds()).willReturn(currentTimeInSeconds)
            given(userDbImpl.getApplicationUsersWithEmail(email, scope)).willReturn(listOf(userActivated))

            // Call method and assert
            userRepo.activateRegisteredUser(email)
        }

    @Test(expected = UserSuspendedException::class)
    fun `activateRegisteredUser should throw UserSuspendedException if user is Suspended`() = runBlocking<Unit> {
        val currentTimeInSeconds = Instant.now().epochSecond
        val email = "someEmailId"
        val userSuspended = getApplicationUser(email = email, accountStatus = AccountStatus.Suspended)

        // Stub
        given(timeAndIdGenerator.getCurrentTimeInSeconds()).willReturn(currentTimeInSeconds)
        given(userDbImpl.getApplicationUsersWithEmail(email, scope)).willReturn(listOf(userSuspended))

        // Call method and assert
        userRepo.activateRegisteredUser(email)
    }

    @Test
    fun `activateRegisteredUser should set accountStatus to Activated if user has activated before`() = runBlocking {
        val currentTimeInSeconds = Instant.now().epochSecond
        val email = "someEmailId"
        val userRegistered = getApplicationUser(email = email, accountStatus = AccountStatus.Registered)
        val userActivated = getApplicationUser(
            email = email,
            accountStatus = AccountStatus.Activated,
            activationDateInSeconds = currentTimeInSeconds
        )

        // Stub
        given(timeAndIdGenerator.getCurrentTimeInSeconds()).willReturn(currentTimeInSeconds)
        given(userDbImpl.getApplicationUsersWithEmail(email, scope)).willReturn(listOf(userRegistered))
        given(userDbImpl.save(userActivated, scope)).willReturn(userActivated)

        // Call method and assert
        assertEquals(userActivated, userRepo.activateRegisteredUser(email))
    }

    @Test(expected = UserNotFoundException::class)
    fun `getActivateUserInfo should throw UserNotFoundException if user not found`() = runBlocking<Unit> {
        val currentTimeInSeconds = Instant.now().epochSecond
        val userId = "someUserId"

        // Stub
        given(timeAndIdGenerator.getCurrentTimeInSeconds()).willReturn(currentTimeInSeconds)
        given(userDbImpl.getApplicationUser(userId, scope)).willReturn(null)

        // Call method and assert
        userRepo.getActiveUserInfo(userId)
    }

    @Test(expected = UserNotActivatedException::class)
    fun `getActivateUserInfo should throw UserNotActivatedException if user is not activated`() = runBlocking<Unit> {
        val currentTimeInSeconds = Instant.now().epochSecond
        val userId = "someUserId"
        val email = "someEmailId"
        val userRegistered = getApplicationUser(id = userId, email = email, accountStatus = AccountStatus.Registered)

        // Stub
        given(timeAndIdGenerator.getCurrentTimeInSeconds()).willReturn(currentTimeInSeconds)
        given(userDbImpl.getApplicationUser(userId, scope)).willReturn(userRegistered)

        // Call method and assert
        userRepo.getActiveUserInfo(userId)
    }

    @Test(expected = UserSuspendedException::class)
    fun `getActivateUserInfo should throw UserSuspendedException if user is Suspended`() = runBlocking<Unit> {
        val currentTimeInSeconds = Instant.now().epochSecond
        val userId = "someUserId"
        val email = "someEmailId"
        val userSuspended = getApplicationUser(id = userId, email = email, accountStatus = AccountStatus.Suspended)

        // Stub
        given(timeAndIdGenerator.getCurrentTimeInSeconds()).willReturn(currentTimeInSeconds)
        given(userDbImpl.getApplicationUser(userId, scope)).willReturn(userSuspended)

        // Call method and assert
        userRepo.getActiveUserInfo(userId)
    }

    @Test
    fun `getActivateUserInfo should return userInfo if user is Activated`() = runBlocking<Unit> {
        val currentTimeInSeconds = Instant.now().epochSecond
        val userId = "someUserId"
        val email = "someEmailId"
        val userActivated = getApplicationUser(
            id = userId,
            email = email,
            accountStatus = AccountStatus.Activated,
            roles = listOf(Roles.User),
            activationDateInSeconds = currentTimeInSeconds
        )
        val expectedUserInfo = UserInfo(id = userActivated.id, email = userActivated.email)

        // Stub
        given(timeAndIdGenerator.getCurrentTimeInSeconds()).willReturn(currentTimeInSeconds)
        given(userDbImpl.getApplicationUser(userId, scope)).willReturn(userActivated)

        // Call method and assert
        assertEquals(expectedUserInfo, userRepo.getActiveUserInfo(userId))
    }

    @Test
    fun `getApplicationUser should return null user is not present in DB`() = runBlocking {
        val userId = "someUserId"

        // Stub
        given(userDbImpl.getApplicationUser(userId, scope)).willReturn(null)

        // Call method and assert
        assertNull(userRepo.getApplicationUser(userId))
    }

    @Test
    fun `getApplicationUser should return user when user is present in DB`() = runBlocking {
        val userId = "someUserId"
        val email = "someEmailId"
        val currentTimeInSeconds = Instant.now().epochSecond
        val userActivated = getApplicationUser(
            id = userId,
            email = email,
            accountStatus = AccountStatus.Activated,
            roles = listOf(Roles.User),
            registrationDateInSeconds = currentTimeInSeconds
        )

        // Stub
        given(userDbImpl.getApplicationUser(userId, scope)).willReturn(userActivated)

        // Call method and assert
        assertEquals(userActivated, userRepo.getApplicationUser(userId))
    }

    @Test
    fun `getApplicationUserWithEmail should return null user is not present in DB`() = runBlocking {
        val email = "someEmailId"

        // Stub
        given(userDbImpl.getApplicationUsersWithEmail(email, scope)).willReturn(emptyList())

        // Call method and assert
        assertNull(userRepo.getApplicationUserWithEmail(email))
    }

    @Test
    fun `getApplicationUserWithEmail should return activated user when activate user is present in DB`() = runBlocking {
        val userId = "someUserId"
        val email = "someEmailId"
        val currentTimeInSeconds = Instant.now().epochSecond
        val userActivated = getApplicationUser(
            id = userId,
            email = email,
            accountStatus = AccountStatus.Activated,
            roles = listOf(Roles.User),
            registrationDateInSeconds = currentTimeInSeconds
        )

        // Stub
        given(userDbImpl.getApplicationUsersWithEmail(email, scope)).willReturn(listOf(userActivated))

        // Call method and assert
        assertEquals(userActivated, userRepo.getApplicationUserWithEmail(email))
    }

    @Test
    fun `getApplicationUserWithEmail should return activated user when both activated & registered user is present with emailId`() =
        runBlocking {
            val userId = "someUserId"
            val email = "someEmailId"
            val currentTimeInSeconds = Instant.now().epochSecond
            val userActivated = getApplicationUser(
                id = userId,
                email = email,
                accountStatus = AccountStatus.Activated,
                roles = listOf(Roles.User),
                registrationDateInSeconds = currentTimeInSeconds,
                activationDateInSeconds = currentTimeInSeconds + 50
            )
            val userRegistered = getApplicationUser(
                id = userId + "123",
                email = email,
                accountStatus = AccountStatus.Registered,
                roles = listOf(Roles.User),
                registrationDateInSeconds = currentTimeInSeconds + 100
            )

            // Stub
            given(userDbImpl.getApplicationUsersWithEmail(email, scope)).willReturn(
                listOf(
                    userActivated,
                    userRegistered
                )
            )

            // Call method and assert
            assertEquals(userActivated, userRepo.getApplicationUserWithEmail(email))
        }

    @Test
    fun `getApplicationUserWithEmail should return latest activated user when multiple activated users are present with emailId`() =
        runBlocking {
            val userId = "someUserId"
            val email = "someEmailId"
            val currentTimeInSeconds = Instant.now().epochSecond
            val userActivated1 = getApplicationUser(
                id = userId,
                email = email,
                accountStatus = AccountStatus.Activated,
                roles = listOf(Roles.User),
                registrationDateInSeconds = currentTimeInSeconds,
                activationDateInSeconds = currentTimeInSeconds + 10
            )
            val userActivated2 = getApplicationUser(
                id = userId + "222",
                email = email,
                accountStatus = AccountStatus.Activated,
                roles = listOf(Roles.User),
                registrationDateInSeconds = currentTimeInSeconds + 50,
                activationDateInSeconds = currentTimeInSeconds + 70 // latest activated user
            )
            val userActivated3 = getApplicationUser(
                id = userId + "333",
                email = email,
                accountStatus = AccountStatus.Activated,
                roles = listOf(Roles.User),
                registrationDateInSeconds = currentTimeInSeconds,
                activationDateInSeconds = currentTimeInSeconds + 5
            )
            val userRegistered = getApplicationUser(
                id = userId + "123",
                email = email,
                accountStatus = AccountStatus.Registered,
                roles = listOf(Roles.User),
                registrationDateInSeconds = currentTimeInSeconds + 100
            )

            // Stub
            given(userDbImpl.getApplicationUsersWithEmail(email, scope))
                .willReturn(listOf(userActivated1, userRegistered, userActivated2, userActivated3))

            // Call method and assert
            assertEquals(userActivated2, userRepo.getApplicationUserWithEmail(email))
        }

    @Test
    fun `getApplicationUserWithEmail should return latest registered user when multiple registered users are present with emailId`() =
        runBlocking {
            val userId = "someUserId"
            val email = "someEmailId"
            val currentTimeInSeconds = Instant.now().epochSecond
            val userRegistered1 = getApplicationUser(
                id = userId,
                email = email,
                accountStatus = AccountStatus.Registered,
                roles = listOf(Roles.User),
                registrationDateInSeconds = currentTimeInSeconds,
            )
            val userRegistered2 = getApplicationUser(
                id = userId + "222",
                email = email,
                accountStatus = AccountStatus.Registered,
                roles = listOf(Roles.User),
                registrationDateInSeconds = currentTimeInSeconds + 50
            )
            val userRegistered3 = getApplicationUser(
                id = userId + "333",
                email = email,
                accountStatus = AccountStatus.Registered,
                roles = listOf(Roles.User),
                registrationDateInSeconds = currentTimeInSeconds,
                activationDateInSeconds = currentTimeInSeconds + 20
            )

            // Stub
            given(userDbImpl.getApplicationUsersWithEmail(email, scope))
                .willReturn(listOf(userRegistered1, userRegistered2, userRegistered3))

            // Call method and assert
            assertEquals(userRegistered2, userRepo.getApplicationUserWithEmail(email))
        }

    @Test
    fun deleteUsersWithEmailAndAccountStatusTest() = runBlocking {
        val email = "someEmailId"

        // Call method
        userRepo.deleteUsersWithEmailAndAccountStatus(email, AccountStatus.Registered)

        // Verify
        verify(userDbImpl).deleteUsersWithEmailAndAccountStatus(email, AccountStatus.Registered, scope)
    }

    private fun getApplicationUser(
        id: String = "someUserId",
        email: String = "someEmailId",
        password: String = "somePassword",
        roles: List<Roles> = emptyList(),
        accountStatus: AccountStatus = AccountStatus.Activated,
        registrationDateInSeconds: Long = -1,
        firstName: String = "",
        lastName: String = "",
        nickname: String = "",
        age: Int = -1,
        gender: Gender = Gender.Undisclosed,
        houseNumber: String = "",
        streetName: String = "",
        postCode: String = "",
        city: String = "",
        country: String = "",
        activationDateInSeconds: Long = -1
    ) =
        ApplicationUser(
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