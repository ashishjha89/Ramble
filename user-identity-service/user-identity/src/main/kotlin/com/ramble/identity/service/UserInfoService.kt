package com.ramble.identity.service

import com.ramble.identity.common.*
import com.ramble.identity.models.*
import com.ramble.identity.repo.UserRepo
import com.ramble.identity.utils.TimeAndIdGenerator
import com.ramble.token.AuthTokensService
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

    fun getUserInfoResult(principal: Principal): Result<UserInfo> {
        val badRequestError = Result.Error<UserInfo>(httpStatus = HttpStatus.BAD_REQUEST, errorBody = userInfoNotFound)
        val emailId = authTokensService.getAccessTokenClaims(principal)?.email ?: return badRequestError
        return try {
            Result.Success(data = getUserInfo(emailId))
        } catch (e: Exception) {
            when (e) {
                is UserNotFoundException -> badRequestError
                is UserSuspendedException -> Result.Error(HttpStatus.FORBIDDEN, userSuspendedError)
                is UserNotActivatedException -> Result.Error(HttpStatus.FORBIDDEN, userNotActivatedError)
                else -> Result.Error(HttpStatus.INTERNAL_SERVER_ERROR, internalServerError)
            }
        }
    }

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

    fun refreshToken(refreshTokenRequest: RefreshTokenRequest): Result<LoginResponse> {
        val now = timeAndIdGenerator.getCurrentTime()
        return try {
            val userAuthInfo = authTokensService.refreshAuthToken(
                    refreshToken = refreshTokenRequest.refreshToken ?: throw IllegalStateException(),
                    now = now
            ) ?: throw IllegalStateException()
            Result.Success(data = LoginResponse(
                    userId = userAuthInfo.userId,
                    accessToken = userAuthInfo.accessToken,
                    refreshToken = userAuthInfo.refreshToken
            ))
        } catch (e: Exception) {
            when (e) {
                is RefreshTokenIsInvalidException -> Result.Error(HttpStatus.FORBIDDEN, refreshTokenInvalid)
                else -> Result.Error(HttpStatus.INTERNAL_SERVER_ERROR, internalServerError)
            }
        }
    }

    fun logout(accessToken: String): Result<Unit> {
        val now = timeAndIdGenerator.getCurrentTime()
        return try {
            Result.Success(data = authTokensService.logout(accessToken, now))
        } catch (e: Exception) {
            Result.Error(httpStatus = HttpStatus.FORBIDDEN, errorBody = unauthorizedAccess)
        }
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