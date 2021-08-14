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
            Roles.User -> listOf(
                Permission.ReadOwnInfo,
                Permission.WriteOwnInfo,
                Permission.DeleteOwnInfo
            ).map { it.toString() }
            Roles.Admin -> Permission.values().map { it.toString() }
        }.toSet()

val Roles.grantedAuthorities: Set<GrantedAuthority>
    get() =
        listOf(SimpleGrantedAuthority("ROLE_$this"))
            .toMutableList()
            .plus(permissions.toList().map { SimpleGrantedAuthority(it) })
            .toSet()

fun ApplicationUser.toUserInfo(): UserInfo =
    UserInfo(
        id = id,
        email = email,
        firstName = firstName?.ifBlank { null },
        lastName = lastName?.ifBlank { null },
        nickname = nickname?.ifBlank { null },
        fullName = fullName.ifBlank { null },
        age = if (age != null && age > 0) age else null,
        gender = if (gender == Gender.Undisclosed) null else gender.name,
        houseNumber = houseNumber?.ifBlank { null },
        streetName = streetName?.ifBlank { null },
        postCode = postCode?.ifBlank { null },
        city = city?.ifBlank { null },
        country = country?.ifBlank { null },
        fullAddress = fullAddress.ifBlank { null }
    )
