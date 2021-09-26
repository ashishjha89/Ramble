package com.ramble.messaging.repo

import com.ramble.messaging.model.InternalServerException
import com.ramble.messaging.model.UserNotFoundException
import com.ramble.messaging.model.UserProfile
import com.ramble.messaging.utils.MessagingCoroutineScopeBuilder
import com.ramble.messaging.utils.performDeferredTask
import kotlinx.coroutines.async
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Repository
import org.springframework.web.client.RestTemplate

@Repository
class UserRepo(
    private val restTemplate: RestTemplate,
    private val userApiComponent: UserApiComponent,
    private val coroutineScopeBuilder: MessagingCoroutineScopeBuilder
) {

    companion object {
        private const val IDENTITY_API_TIMEOUT = 500L
    }

    private val logger = LoggerFactory.getLogger(UserRepo::class.java)

    @Throws(UserNotFoundException::class, InternalServerException::class)
    suspend fun getUserProfile(
        userId: String,
        accessToken: String,
        timeoutInMilliseconds: Long = IDENTITY_API_TIMEOUT
    ): UserProfile =
        performDeferredTask(
            deferredTask = coroutineScopeBuilder.defaultIoScope.async {
                restTemplate.exchange(
                    userApiComponent.getUserProfileApiUrl(userId),
                    HttpMethod.GET,
                    userApiComponent.getHttpEntity(accessToken),
                    UserProfile::class.java
                )
            },
            timeoutInMilliseconds = timeoutInMilliseconds,
            logger = logger
        ).body ?: throw InternalServerException()

}