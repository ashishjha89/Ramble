package com.ramble.identity.service

import com.ramble.identity.common.invalidUserId
import com.ramble.identity.models.*
import com.ramble.identity.repo.Email
import com.ramble.identity.repo.Id
import com.ramble.identity.repo.UserRepo
import com.ramble.identity.utils.TimeAndIdGenerator
import com.ramble.token.AuthTokensService
import com.ramble.token.model.AccessTokenIsInvalidException
import com.ramble.token.model.InternalTokenStorageException
import com.ramble.token.model.RefreshTokenIsInvalidException
import kotlinx.coroutines.reactor.mono
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.security.Principal
import org.springframework.security.core.userdetails.User as SpringUser

@Service
class UserInfoService(
    private val userRepo: UserRepo,
    private val authTokensService: AuthTokensService,
    private val timeAndIdGenerator: TimeAndIdGenerator
) : ReactiveUserDetailsService {

    @Throws(UserNotFoundException::class, InternalServerException::class, InternalTokenStorageException::class)
    suspend fun getUserInfo(principal: Principal): UserInfo =
        getUserInfo(id = authTokensService.getAccessTokenClaims(principal)?.userId ?: throw UserNotFoundException())

    @Throws(
        UserNotFoundException::class, UserSuspendedException::class,
        UserNotActivatedException::class, InternalServerException::class
    )
    suspend fun getUserInfo(id: Id): UserInfo =
        userRepo.getUserInfo(id)

    @Throws(
        UserNotFoundException::class, UserSuspendedException::class,
        UserNotActivatedException::class, InternalServerException::class
    )
    suspend fun getUserInfoFromEmail(email: Email): UserInfo =
        userRepo.getApplicationUserFromEmail(email)?.toUserInfo() ?: throw UserNotFoundException()

    @Throws(
        UserNotFoundException::class, UserSuspendedException::class,
        UserNotActivatedException::class, InternalServerException::class
    )
    suspend fun getUserProfile(id: Id): UserProfile {
        val userInfo = userRepo.getUserInfo(id)
        return UserProfile(
            email = userInfo.email,
            firstName = userInfo.firstName,
            lastName = userInfo.lastName,
            fullName = userInfo.fullName,
            age = userInfo.age,
            gender = userInfo.gender
        )
    }

    @Throws(UsernameNotFoundException::class, InternalServerException::class)
    override fun findByUsername(username: Email?): Mono<UserDetails> = mono {
        if (username.isNullOrBlank()) throw UsernameNotFoundException(invalidUserId.errorMessage)
        val user = userRepo.getApplicationUserFromEmail(username)
            ?: throw UsernameNotFoundException(invalidUserId.errorMessage)
        return@mono SpringUser(user.email, user.password, user.grantedAuthorities)
    }

    @Throws(RefreshTokenIsInvalidException::class, InternalTokenStorageException::class)
    suspend fun refreshToken(refreshTokenRequest: RefreshTokenRequest): LoginResponse {
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

    @Throws(AccessTokenIsInvalidException::class, InternalTokenStorageException::class)
    suspend fun logout(accessToken: String) {
        authTokensService.logout(accessToken, now = timeAndIdGenerator.getCurrentTime())
    }

}