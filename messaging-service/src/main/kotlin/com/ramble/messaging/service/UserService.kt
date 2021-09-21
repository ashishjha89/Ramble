package com.ramble.messaging.service

import com.ramble.accesstoken.AccessTokenValidatorService
import com.ramble.messaging.model.AccessTokenIsInvalidException
import com.ramble.messaging.model.InternalServerException
import com.ramble.messaging.model.UserNotFoundException
import com.ramble.messaging.model.UserProfile
import com.ramble.messaging.repo.UserRepo
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class UserService(
    private val userRepo: UserRepo,
    private val accessTokenValidatorService: AccessTokenValidatorService
) {

    @Throws(UserNotFoundException::class, AccessTokenIsInvalidException::class, InternalServerException::class)
    suspend fun getUserProfile(accessToken: String, instant: Instant = Instant.now()): UserProfile {
        val emailId = accessTokenValidatorService.getClaimsFromAccessToken(accessToken, instant)?.email
            ?: throw AccessTokenIsInvalidException()
        return userRepo.getUserProfile(emailId, accessToken)
    }
}