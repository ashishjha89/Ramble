package com.ramble.identity.repo

import com.ramble.identity.models.*
import org.springframework.stereotype.Repository

typealias Email = String

@Repository
class UserRepo {

    private val userMap = mutableMapOf<Email, ApplicationUser>()

    @Throws(EmailNotAvailableException::class)
    fun saveUser(
            email: Email,
            password: String,
            idGenerator: () -> Long = { System.currentTimeMillis() }
    ): String {
        if (userMap.containsKey(email)) throw EmailNotAvailableException()
        val user = ApplicationUser(
                id = (userMap.size.toLong() + idGenerator()).toString(),
                email = email,
                password = password,
                roles = listOf(Roles.Active.toString())
        )
        userMap[email] = user
        return user.id
    }

    @Throws(UserNotFoundException::class)
    fun getUserInfo(email: Email): UserInfo {
        val user = findByEmail(email) ?: throw UserNotFoundException()
        return UserInfo(id = user.id, email = user.email)
    }

    fun findByEmail(email: Email): ApplicationUser? = userMap[email]

}