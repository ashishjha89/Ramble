package com.ramble.identity.models

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

enum class Gender {
    Female,
    Male,
    Agender,
    Transgender,
    Bigender,
    Cisgender,
    Intersex,
    Undisclosed,
    Other,
}

enum class AccountStatus {
    Registered,
    Activated,
    Suspended,
}

enum class Permission {
    ReadOwnInfo,
    WriteOwnInfo,
    DeleteOwnInfo,
    ReadAll,
    WriteAll,
    DeleteAll
}

enum class Roles {
    Admin,
    User
}

val Roles.permissions: Set<String>
    get() =
        when (this) {
            Roles.User -> listOf(Permission.ReadOwnInfo, Permission.WriteOwnInfo, Permission.DeleteOwnInfo).map { it.toString() }
            Roles.Admin -> Permission.values().map { it.toString() }
        }.toSet()

val Roles.grantedAuthorities: Set<GrantedAuthority>
    get() =
        listOf(SimpleGrantedAuthority("ROLE_$this"))
                .toMutableList()
                .plus(permissions.toList().map { SimpleGrantedAuthority(it) })
                .toSet()

fun RegisterUserRequest.toApplicationUser(
        roles: List<Roles>,
        accountStatus: AccountStatus,
        registrationDateInSeconds: Long,
        id: Long
): ApplicationUser =
        ApplicationUser(
                id = id.toString(),
                email = email,
                password = password,
                roles = roles,
                accountStatus = accountStatus,
                registrationDateInSeconds = registrationDateInSeconds,
                firstName = firstName,
                lastName = lastName,
                nickname = nickname,
                age = age,
                gender = Gender.values().find { it.name == gender } ?: Gender.Undisclosed,
                houseNumber = houseNumber,
                streetName = streetName,
                postCode = postCode,
                city = city,
                country = country
        )

fun ApplicationUser.toUserInfo(): UserInfo =
        UserInfo(
                id = id,
                email = email,
                firstName = firstName,
                lastName = lastName,
                nickname = nickname,
                fullName = fullName,
                age = age,
                gender = gender.name,
                houseNumber = houseNumber,
                streetName = streetName,
                postCode = postCode,
                city = city,
                country = country,
                fullAddress = fullAddress
        )
