package com.ramble.identity.models

import org.junit.Test
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.time.Instant
import kotlin.test.assertEquals

class ModelsTest {

    @Test
    fun permissionsTest() {
        val userPermissions =
            listOf(Permission.ReadOwnInfo, Permission.WriteOwnInfo, Permission.DeleteOwnInfo)
                .map { it.toString() }
                .toSet()
        val adminPermissions = Permission.values().map { it.toString() }.toSet()
        assertEquals(userPermissions, Roles.User.permissions)
        assertEquals(adminPermissions, Roles.Admin.permissions)
    }

    @Test
    fun grantedAuthoritiesTest() {
        val userAuthorities =
            listOf(
                SimpleGrantedAuthority("ROLE_User"),
                SimpleGrantedAuthority("ReadOwnInfo"),
                SimpleGrantedAuthority("WriteOwnInfo"),
                SimpleGrantedAuthority("DeleteOwnInfo")
            ).toSet()
        val adminAuthorities =
            listOf(
                SimpleGrantedAuthority("ROLE_Admin"),
                SimpleGrantedAuthority("ReadOwnInfo"),
                SimpleGrantedAuthority("WriteOwnInfo"),
                SimpleGrantedAuthority("DeleteOwnInfo"),
                SimpleGrantedAuthority("ReadAll"),
                SimpleGrantedAuthority("WriteAll"),
                SimpleGrantedAuthority("DeleteAll")
            ).toSet()
        assertEquals(userAuthorities, Roles.User.grantedAuthorities)
        assertEquals(adminAuthorities, Roles.Admin.grantedAuthorities)
    }

    @Test
    fun registerUserRequestToApplicationUserTest() {
        val gender = Gender.Male
        val roles = listOf(Roles.User)
        val accountStatus = AccountStatus.Activated
        val registrationDateInSeconds = Instant.now().epochSecond
        val registerUserRequest = RegisterUserRequest(
            email = "email",
            password = "password",
            firstName = "firstName",
            lastName = "lastName",
            nickname = "nickname",
            age = 30,
            gender = gender.name,
            houseNumber = "houseNumber",
            streetName = "streetName",
            postCode = "postCode",
            city = "city",
            country = "country"
        )
        val expectedApplicationUser = ApplicationUser(
            id = registrationDateInSeconds.toString(),
            email = registerUserRequest.email,
            password = registerUserRequest.password,
            firstName = registerUserRequest.firstName,
            lastName = registerUserRequest.lastName,
            nickname = registerUserRequest.nickname,
            age = registerUserRequest.age,
            gender = gender,
            houseNumber = registerUserRequest.houseNumber,
            streetName = registerUserRequest.streetName,
            postCode = registerUserRequest.postCode,
            city = registerUserRequest.city,
            country = registerUserRequest.country,
            roles = roles,
            accountStatus = accountStatus,
            registrationDateInSeconds = registrationDateInSeconds
        )
        assertEquals(
            expectedApplicationUser,
            registerUserRequest.toApplicationUser(
                roles,
                accountStatus,
                registrationDateInSeconds,
                registrationDateInSeconds
            )
        )
    }

    @Test
    fun `registerUserRequestToApplicationUser should set gender undisclosed when unknown gender passed`() {
        val gender = "random"
        val roles = listOf(Roles.User)
        val accountStatus = AccountStatus.Activated
        val registrationDateInSeconds = Instant.now().epochSecond
        val registerUserRequest = RegisterUserRequest(
            email = "email",
            password = "password",
            firstName = "firstName",
            lastName = "lastName",
            nickname = "nickname",
            age = 30,
            gender = gender,
            houseNumber = "houseNumber",
            streetName = "streetName",
            postCode = "postCode",
            city = "city",
            country = "country"
        )
        val expectedApplicationUser = ApplicationUser(
            id = registrationDateInSeconds.toString(),
            email = registerUserRequest.email,
            password = registerUserRequest.password,
            firstName = registerUserRequest.firstName,
            lastName = registerUserRequest.lastName,
            nickname = registerUserRequest.nickname,
            age = registerUserRequest.age,
            gender = Gender.Undisclosed,
            houseNumber = registerUserRequest.houseNumber,
            streetName = registerUserRequest.streetName,
            postCode = registerUserRequest.postCode,
            city = registerUserRequest.city,
            country = registerUserRequest.country,
            roles = roles,
            accountStatus = accountStatus,
            registrationDateInSeconds = registrationDateInSeconds
        )
        assertEquals(
            expectedApplicationUser,
            registerUserRequest.toApplicationUser(
                roles,
                accountStatus,
                registrationDateInSeconds,
                registrationDateInSeconds
            )
        )
    }

    @Test
    fun applicationUserToUserInfoTest() {
        val applicationUser = ApplicationUser(
            id = "id",
            email = "email",
            firstName = "firstName",
            lastName = "lastName",
            nickname = "nickname",
            age = 30,
            gender = Gender.Male,
            houseNumber = "houseNumber",
            streetName = "streetName",
            postCode = "postCode",
            city = "city",
            country = "country",
            password = "password",
            roles = listOf(Roles.User),
            accountStatus = AccountStatus.Activated,
            registrationDateInSeconds = Instant.now().epochSecond,
            activationDateInSeconds = Instant.now().epochSecond
        )
        val expectedUserInfo = UserInfo(
            id = applicationUser.id,
            email = applicationUser.email,
            firstName = applicationUser.firstName,
            lastName = applicationUser.lastName,
            nickname = applicationUser.nickname,
            fullName = applicationUser.fullName,
            age = applicationUser.age,
            gender = applicationUser.gender.name,
            houseNumber = applicationUser.houseNumber,
            streetName = applicationUser.streetName,
            postCode = applicationUser.postCode,
            city = applicationUser.city,
            country = applicationUser.country,
            fullAddress = applicationUser.fullAddress
        )
        assertEquals(expectedUserInfo, applicationUser.toUserInfo())
    }
}