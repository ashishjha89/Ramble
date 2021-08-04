package com.ramble.identity.service

import com.ramble.identity.common.*
import com.ramble.identity.models.*
import com.ramble.identity.repo.UserRepo
import com.ramble.identity.utils.TimeAndIdGenerator
import com.ramble.token.AuthTokensService
import com.ramble.token.model.AccessTokenIsInvalidException
import com.ramble.token.model.RefreshTokenIsInvalidException
import org.springframework.http.HttpStatus
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
        when (val userRes = findByEmail(email = username)) {
            is Result.Success -> return SpringUser(userRes.data.email, userRes.data.password, userRes.data.grantedAuthorities)
            is Result.Error -> throw UsernameNotFoundException(userRes.errorBody.errorMessage)
        }
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
        /*return try {

        } catch (e: Exception) {
            when (e) {
                is RefreshTokenIsInvalidException -> Result.Error(HttpStatus.FORBIDDEN, refreshTokenInvalid)
                else -> Result.Error(HttpStatus.INTERNAL_SERVER_ERROR, internalServerError)
            }
        }*/
    }

    @Throws(AccessTokenIsInvalidException::class)
    fun logout(accessToken: String) {
        authTokensService.logout(accessToken, now = timeAndIdGenerator.getCurrentTime())
    }

    private fun findByEmail(email: String): Result<ApplicationUser> =
            try {
                when (val user = userRepo.getApplicationUser(email)) {
                    null -> Result.Error(httpStatus = HttpStatus.BAD_REQUEST, errorBody = invalidUserId)
                    else -> Result.Success(data = user)
                }
            } catch (e: Exception) {
                Result.Error(httpStatus = HttpStatus.INTERNAL_SERVER_ERROR, errorBody = internalServerError)
            }

}