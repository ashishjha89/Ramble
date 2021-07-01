package com.ramble.identity.service

import com.ramble.identity.common.ErrorBody
import com.ramble.identity.common.ErrorCode
import com.ramble.identity.common.ErrorMessage
import com.ramble.identity.common.Result
import com.ramble.identity.models.*
import com.ramble.identity.repo.UserRepo
import com.ramble.token.handler.TokensHandler
import org.springframework.http.HttpStatus
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.security.core.userdetails.User as SpringUser

@Service
class UserService(
        private val userRepo: UserRepo,
        private val bCryptPasswordEncoder: BCryptPasswordEncoder,
        private val tokensHandler: TokensHandler
) : UserDetailsService {

    fun saveUser(email: String, password: String): Result<RegisteredUserResponse> {
        val encodedPassword = bCryptPasswordEncoder.encode(password)
        println("UserService saveUser() encodedPassword:$encodedPassword")
        return try {
            Result.Success(data = RegisteredUserResponse(id = userRepo.saveUser(email, encodedPassword)))
        } catch (e: EmailNotAvailableException) {
            Result.Error(
                    httpStatus = HttpStatus.BAD_REQUEST,
                    errorBody = ErrorBody(
                            errorCode = ErrorCode.USER_ALREADY_REGISTERED,
                            errorMessage = ErrorMessage.userAlreadyRegistered
                    )
            )
        }
    }

    fun getUserInfoResult(token: String?): Result<UserInfo> {
        val email = tokensHandler.getAccessTokenClaims(token)?.email
        val badRequestError = Result.Error<UserInfo>(
                httpStatus = HttpStatus.BAD_REQUEST,
                errorBody = ErrorBody(
                        errorCode = ErrorCode.USER_INFO_NOT_FOUND,
                        errorMessage = ErrorMessage.userInfoNotFound
                )
        )
        val unauthorizedError = Result.Error<UserInfo>(
                httpStatus = HttpStatus.UNAUTHORIZED,
                errorBody = ErrorBody(errorCode = ErrorCode.MISSING_AUTHORIZATION_HEADER, errorMessage = ErrorMessage.missingAuthorizationHeader)
        )
        println("UserService getMyInfo() token:$token email:$email")
        return when {
            token.isNullOrBlank() -> unauthorizedError
            email.isNullOrBlank() -> badRequestError
            else ->
                try {
                    Result.Success(data = userRepo.getUserInfo(email))
                } catch (e: UserNotFoundException) {
                    badRequestError
                }
        }
    }

    @Throws(UserNotFoundException::class)
    fun getUserInfo(email: String): UserInfo =
            userRepo.getUserInfo(email)

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String?): UserDetails {
        println("UserService loadUserByUsername() username:$username")
        if (username.isNullOrBlank()) throw UsernameNotFoundException(ErrorMessage.invalidUserId)
        when (val userRes = findByEmail(email = username)) {
            is Result.Success -> return SpringUser(userRes.data.email, userRes.data.password, userRes.data.grantedAuthorities)
            is Result.Error -> throw UsernameNotFoundException(ErrorMessage.invalidUserId)
        }
    }

    private fun findByEmail(email: String): Result<ApplicationUser> =
            try {
                when (val user = userRepo.findByEmail(email)) {
                    null -> Result.Error(
                            httpStatus = HttpStatus.BAD_REQUEST,
                            errorBody = ErrorBody(errorCode = ErrorCode.INVALID_USER_ID, errorMessage = ErrorMessage.invalidUserId))
                    else -> Result.Success(data = user)
                }
            } catch (e: Exception) {
                Result.Error(
                        httpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
                        errorBody = ErrorBody(errorCode = ErrorCode.SOMETHING_WENT_WRONG, errorMessage = ErrorMessage.somethingWentWrong)
                )
            }

}