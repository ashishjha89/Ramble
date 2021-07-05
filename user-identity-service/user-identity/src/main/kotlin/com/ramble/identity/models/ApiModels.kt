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
)

data class RegisteredUserResponse(val userId: String, val email: String)

data class LoginUserRequest(val email: String? = null, val password: String? = null)

data class LoginResponse(val id: String, val email: String, val accessToken: String, val refreshToken: String)

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