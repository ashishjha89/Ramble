package com.ramble.identity.service

import com.ramble.identity.common.invalidUserId
import com.ramble.identity.models.*
import com.ramble.identity.repo.UserRepo
import com.ramble.identity.utils.TimeAndIdGenerator
import com.ramble.token.AuthTokensService
import com.ramble.token.model.AccessTokenIsInvalidException
import com.ramble.token.model.RefreshTokenIsInvalidException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.security.Principal
import org.springframework.security.core.userdetails.User as SpringUser

@Service
class UserInfoService(
        private val userRepo: UserRepo,
        private val authTokensService: AuthTokensService,
        private val timeAndIdGenerator: TimeAndIdGenerator
) : UserDetailsService {

    @Throws(UserNotFoundException::class)
    fun getUserInfoResult(principal: Principal): UserInfo =
            getUserInfo(authTokensService.getAccessTokenClaims(principal)?.email ?: throw UserNotFoundException())

    @Throws(UserNotFoundException::class, UserSuspendedException::class, UserNotActivatedException::class)
    fun getUserInfo(email: String): UserInfo = userRepo.getUserInfo(email)

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String?): UserDetails {
        if (username.isNullOrBlank()) throw UsernameNotFoundException(invalidUserId.errorMessage)
        val user = userRepo.getApplicationUser(username) ?: throw UsernameNotFoundException(invalidUserId.errorMessage)
        return SpringUser(user.email, user.password, user.grantedAuthorities)
    }

    @Throws(RefreshTokenIsInvalidException::class)
    fun refreshToken(refreshTokenRequest: RefreshTokenRequest): LoginResponse {
        val now = timeAndIdGenerator.getCurrentTime()
        val userAuthInfo = authTokensService.refreshAuthToken(
                refreshToken = refreshTokenRequest.refreshToken ?: throw RefreshTokenIsInvalidException(),
                now = now
        ) ?: throw RefreshTokenIsInvalidException()
        return LoginResponse(
                userId = userAuthInfo.userId,
                accessToken = userAuthInfo.accessToken,
                refreshToken = userAuthInfo.refreshToken
        )
    }

    @Throws(AccessTokenIsInvalidException::class)
    fun logout(accessToken: String) {
        authTokensService.logout(accessToken, now = timeAndIdGenerator.getCurrentTime())
    }

}