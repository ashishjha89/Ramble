package com.ramble.identity.models

data class RegisterUserRequest(
    val email: String,
    val password: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val nickname: String? = null,
    val age: Int? = null,
    val gender: String? = null,
    val houseNumber: String? = null,
    val streetName: String? = null,
    val postCode: String? = null,
    val city: String? = null,
    val country: String? = null
) {

    fun toApplicationUser(
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
}

data class RegisteredUserResponse(val userId: String, val email: String)

data class LoginUserRequest(val email: String? = null, val password: String? = null)

data class LoginResponse(val userId: String, val accessToken: String, val refreshToken: String)

data class RefreshTokenRequest(val refreshToken: String? = null)

data class UserInfo(
    val id: String,
    val email: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val nickname: String? = null,
    val fullName: String? = null,
    val age: Int? = null,
    val gender: String? = null,
    val houseNumber: String? = null,
    val streetName: String? = null,
    val postCode: String? = null,
    val city: String? = null,
    val country: String? = null,
    val fullAddress: String? = null
)