package com.ramble.messaging.service

import com.ramble.messaging.model.AccessTokenIsInvalidException
import com.ramble.messaging.model.InternalServerException
import com.ramble.messaging.model.UserNotFoundException
import com.ramble.messaging.model.UserProfile
import com.ramble.messaging.repo.UserRepo
//import com.ramble.token.AuthTokensService
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepo: UserRepo
    //private val authTokensService: AuthTokensService
) {

    @Throws(UserNotFoundException::class, AccessTokenIsInvalidException::class, InternalServerException::class)
    suspend fun getUserProfile(accessToken: String): UserProfile {
        // TODO: Fix the error related to importing auth-token-lib. It asks to setup DB.
        /*val emailId = authTokensService.getAccessTokenClaims(accessToken)?.email
            ?: throw AccessTokenIsInvalidException()*/
        val emailId = "ashishjha@ramble.com"
        return userRepo.getUserProfile(emailId, accessToken)
    }
}